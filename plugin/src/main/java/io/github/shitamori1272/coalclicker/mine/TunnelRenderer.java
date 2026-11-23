package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.world.LevelStrata;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import io.papermc.paper.entity.TeleportFlag;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Bell;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;

/**
 * 1 人分の区画のブロックを置き直す係（企画書 5-3・5-4）。座標はすべて {@link MineLayout} の決まりに従う。
 *
 * <p>ブロックは物理更新なしで置き、すでに同じブロックなら触らない（何度呼んでも同じ結果になり、
 * ループの作り直しでも変わる部分だけが書き換わる）。サーバーのメインスレッドから呼ぶこと。
 */
public final class TunnelRenderer {

    /** 区画のある高さの地層。X・Y・Z からブロックの種類を決める。 */
    @FunctionalInterface
    public interface Strata {
        Material at(int x, int y, int z);
    }

    /** プレイヤーをテレポートさせる手段。坑道ワールドへのテレポートの検査を通すため、呼ぶ側が渡す。 */
    @FunctionalInterface
    public interface Teleporter {
        /** 移せたら true。 */
        boolean teleport(Player player, Location to, TeleportFlag... flags);
    }

    /** ケージ（坑底の奥）の X。ケージへの誘導（#22）はこの列を目指す。右クリックの判定はケージ全体（{@link #isCageBlock}）。 */
    public static final int CAGE_X = MineLayout.SHAFT_MIN_X;
    /** 入坑したときに立つブロックの X（ケージの中。ケージで降りてきた形になる）。 */
    static final int ARRIVAL_X = MineLayout.SHAFT_MIN_X + 2;
    /** 人車の乗り場のレールを敷く西端。 */
    static final int RAIL_MIN_X = MineLayout.SHAFT_MIN_X + 4;

    private static final BlockData AIR = Material.AIR.createBlockData();
    private static final BlockData FENCE = Material.OAK_FENCE.createBlockData();
    private static final BlockData PLANKS = Material.OAK_PLANKS.createBlockData();
    private static final BlockData IRON_BLOCK = Material.IRON_BLOCK.createBlockData();
    private static final BlockData CHAIN = Material.IRON_CHAIN.createBlockData();
    /** ケージの屋根の鉄板（閉じた上付きの鉄のトラップドア）。 */
    private static final BlockData ROOF_PLATE = Material.IRON_TRAPDOOR.createBlockData(data -> {
        TrapDoor trapDoor = (TrapDoor) data;
        trapDoor.setHalf(Bisected.Half.TOP);
        trapDoor.setOpen(false);
        trapDoor.setFacing(BlockFace.EAST);
    });
    /** 巻き上げの合図の鐘（天井から吊る）。 */
    private static final BlockData SIGNAL_BELL = Material.BELL.createBlockData(data -> {
        Bell bell = (Bell) data;
        bell.setAttachment(Bell.Attachment.CEILING);
        bell.setFacing(BlockFace.EAST);
    });
    private static final BlockData LIGHT = Material.LIGHT.createBlockData(data -> ((Light) data).setLevel(12));
    private static final BlockData LANTERN = Material.LANTERN.createBlockData(data -> ((Lantern) data).setHanging(true));
    private static final BlockData RAIL = Material.RAIL.createBlockData(
            data -> ((Rail) data).setShape(Rail.Shape.EAST_WEST));
    private static final BlockData CAGE_SIGN = Material.OAK_WALL_SIGN.createBlockData(
            data -> ((WallSign) data).setFacing(BlockFace.EAST));
    /** 掘り切った切羽の看板。西（坑底の方）を向け、東の切羽に掛ける。 */
    private static final BlockData END_SIGN = Material.OAK_WALL_SIGN.createBlockData(
            data -> ((WallSign) data).setFacing(BlockFace.WEST));

    private final World world;
    private final Teleporter teleporter;

    public TunnelRenderer(World world, Teleporter teleporter) {
        this.world = Objects.requireNonNull(world);
        this.teleporter = Objects.requireNonNull(teleporter);
    }

    /**
     * 階層 level の地層（企画書 5-5）。岩の決め方は {@link LevelStrata} にある。
     *
     * <p>ループ表示で巻き戻したことに気づかれないよう、X 方向には {@link MineLayout#LOOP_METERS} の周期になっている。
     */
    public Strata strataFor(long level) {
        return (x, y, z) -> material(LevelStrata.at(level, x, y, z));
    }

    /** 岩の種類に対応するブロック。 */
    static Material material(LevelStrata.Rock rock) {
        return switch (rock) {
            case STONE -> Material.STONE;
            case COAL_ORE -> Material.COAL_ORE;
            case SANDSTONE -> Material.SANDSTONE;
            case TUFF -> Material.TUFF;
            case DEEPSLATE -> Material.DEEPSLATE;
            case DEEPSLATE_COAL_ORE -> Material.DEEPSLATE_COAL_ORE;
        };
    }

    /**
     * 区画 section を、階層 level・進み具合 meters の状態に作り直す（企画書 5-4）。
     * 地層で埋め直し、坑底を掘り、5-3 のルールで見せる範囲の坑道と坑木を置く。
     */
    public void rebuild(int section, long level, double meters) {
        Strata strata = strataFor(level);
        int centerZ = MineLayout.sectionZ(section);
        for (int x = SectionBounds.REFILL_MIN_X; x <= SectionBounds.REFILL_MAX_X; x++) {
            for (int z = centerZ - SectionBounds.REFILL_HALF_WIDTH; z <= centerZ + SectionBounds.REFILL_HALF_WIDTH;
                    z++) {
                for (int y = SectionBounds.REFILL_MIN_Y; y <= SectionBounds.REFILL_MAX_Y; y++) {
                    set(world.getBlockAt(x, y, z), strata.at(x, y, z));
                }
            }
        }
        carveShaftBottom(section);
        int endX = MineLayout.tunnelEndX(meters);
        for (int x = MineLayout.TUNNEL_START_X; x <= endX; x++) {
            carveSlice(section, x);
        }
    }

    /**
     * 進み具合が oldMeters から newMeters に増えたときに、差分だけブロックを直す（企画書 5-3）。
     * 切羽が X = 128 に達したら、切羽が X = 64 の状態に作り直して、players のうち坑道の奥半分にいる人を
     * 64 ブロック手前へ同じ向き・同じ速度のまま移す。
     *
     * @return 実際に行った計画
     */
    public TunnelPlan advanceTo(int section, long level, double oldMeters, double newMeters,
            Collection<? extends Player> players) {
        TunnelPlan plan = TunnelPlan.plan(oldMeters, newMeters);
        switch (plan.kind()) {
            case NONE -> {
            }
            case CARVE -> {
                for (int x = plan.fromX(); x <= plan.toX(); x++) {
                    carveSlice(section, x);
                }
            }
            case REBUILD -> {
                // 先に手前へ移す（移した先の X < 64 は作り直しの前後とも掘ってある）
                if (plan.shiftPlayers()) {
                    for (Player player : players) {
                        shiftBack(player);
                    }
                }
                rebuild(section, level, newMeters);
            }
        }
        return plan;
    }

    /**
     * block が、区画 section・進み具合 meters の坑道の切羽（3 × 3）か。手掘り（#14）の判定に使う。
     * 掘り切った坑道に切羽がないことの判定は呼ぶ側で行う。
     */
    public boolean isFaceBlock(int section, double meters, Block block) {
        return block.getWorld().equals(world)
                && block.getX() == MineLayout.faceX(meters)
                && MineLayout.inTunnelCrossSection(section, block.getY(), block.getZ());
    }

    /**
     * block が区画 section の坑底のケージ（鉄格子・床・屋根・看板・入口の枠。{@link MineLayout#inCage}）か。
     * ケージで降りる処理（#22）に使う。
     */
    public boolean isCageBlock(int section, Block block) {
        return block.getWorld().equals(world)
                && MineLayout.inCage(section, block.getX(), block.getY(), block.getZ());
    }

    /**
     * 掘り切った坑道（進み具合 meters）の切羽に、西向きの看板を掛ける（#22）。
     * 看板は坑道の最も東の列（空気）の中央・腰の高さに置く。作り直し（{@link #rebuild}）で埋め直されて消える。
     */
    public void placeEndSign(int section, double meters, List<String> lines) {
        Block block = world.getBlockAt(MineLayout.tunnelEndX(meters), MineLayout.TUNNEL_MIN_Y + 1,
                MineLayout.sectionZ(section));
        set(block, END_SIGN);
        if (block.getState() instanceof Sign sign) {
            for (int i = 0; i < Math.min(4, lines.size()); i++) {
                sign.getSide(Side.FRONT).line(i, Component.text(lines.get(i), NamedTextColor.DARK_GRAY));
            }
            sign.setWaxed(true);
            sign.update(true, false);
        }
    }

    /** 区画 section の坑底で、入坑したときに立つ位置（東向き）。 */
    public Location arrival(int section) {
        return new Location(world, ARRIVAL_X + 0.5, MineLayout.FLOOR_Y + 1, MineLayout.sectionZ(section) + 0.5,
                -90f, 0f);
    }

    /** location に立つプレイヤーの当たり判定が、区画 section の今掘ってある範囲（坑底〜X = tunnelEndX）に収まるか。 */
    public boolean inOpenArea(int section, double meters, Location location) {
        return world.equals(location.getWorld())
                && SectionBounds.inOpenArea(section, meters, location.getX(), location.getZ());
    }

    /** 坑底の乗り場（X = -8〜0）を掘り、ケージ・人車のレール・明かりを置く。 */
    private void carveShaftBottom(int section) {
        int centerZ = MineLayout.sectionZ(section);
        int minZ = MineLayout.tunnelMinZ(section);
        int maxZ = MineLayout.tunnelMaxZ(section);
        for (int x = MineLayout.SHAFT_MIN_X; x <= MineLayout.SHAFT_MAX_X; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = MineLayout.TUNNEL_MIN_Y; y <= MineLayout.TUNNEL_MAX_Y; y++) {
                    set(world.getBlockAt(x, y, z), AIR);
                }
            }
        }
        placeCage(section);
        // 人車の乗り場：中央にレール
        for (int x = RAIL_MIN_X; x <= MineLayout.SHAFT_MAX_X; x++) {
            set(world.getBlockAt(x, MineLayout.TUNNEL_MIN_Y, centerZ), RAIL);
        }
        // 乗り場の明かりと、ケージの手前の天井に吊った合図の鐘
        set(world.getBlockAt(RAIL_MIN_X + 1, MineLayout.TUNNEL_MAX_Y, centerZ), LANTERN);
        set(world.getBlockAt(MineLayout.CAGE_MAX_X + 1, MineLayout.TUNNEL_MAX_Y, maxZ), SIGNAL_BELL);
    }

    /**
     * 坑底の奥（X = -8〜-5）に、立坑に吊られた鉄のケージを置く。
     *
     * <pre>
     *  中央の列の断面（南から見る）     上から（Y41）
     *  Y46  | | | R   ← 立坑と鎖       Z+1  # # # #
     *  Y45  | | | R                     Z    # . P .   ← 入口（X=-5）は開いている
     *  Y44  | | | R   ← 屋根の中央      Z-1  # # # #
     *  Y43  # L . #   ← 入口の上の枠        X=-8    -5
     *  Y42  # s . .
     *  Y41  # . P .
     *  Y40  I I I I   ← 鉄の床
     *      X=-8    -5
     *  #＝鉄格子 |＝鎖 L＝吊りランタン s＝看板 P＝入坑して立つ位置 I＝鉄ブロック R＝岩
     * </pre>
     * 屋根（Y44）の両脇の列は閉じた鉄のトラップドア（鉄板）、中央の列は鎖。ケージの上（Y45〜46）は掘って立坑にする。
     * 鉄格子は両脇（Z = 中心 ± 1）と奥（X = -8）を囲み、入口（X = -5）は両脇の柱と上の枠だけで開けておく
     * （アドベンチャーモードでも通れる）。鎖は立坑の上の岩（Y47）から吊る。
     * 立っていられる範囲（Z = 中心）と天井の高さは今までと同じ。
     */
    private void placeCage(int section) {
        int centerZ = MineLayout.sectionZ(section);
        int minZ = MineLayout.tunnelMinZ(section);
        int maxZ = MineLayout.tunnelMaxZ(section);
        int west = MineLayout.CAGE_MIN_X;
        int east = MineLayout.CAGE_MAX_X;
        // 立坑：ケージの上（屋根の上〜上端）を掘る
        for (int x = west; x < east; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = MineLayout.CAGE_ROOF_Y + 1; y <= MineLayout.CAGE_SHAFT_TOP_Y; y++) {
                    set(world.getBlockAt(x, y, z), AIR);
                }
            }
        }
        // 鉄の床と屋根（屋根は両脇の列が鉄板、中央の列は鎖）
        for (int x = west; x <= east; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                set(world.getBlockAt(x, MineLayout.CAGE_FLOOR_Y, z), IRON_BLOCK);
            }
        }
        for (int x = west; x < east; x++) {
            set(world.getBlockAt(x, MineLayout.CAGE_ROOF_Y, minZ), ROOF_PLATE);
            set(world.getBlockAt(x, MineLayout.CAGE_ROOF_Y, maxZ), ROOF_PLATE);
            for (int y = MineLayout.CAGE_ROOF_Y; y <= MineLayout.CAGE_SHAFT_TOP_Y; y++) {
                set(world.getBlockAt(x, y, centerZ), CHAIN);
            }
        }
        // 鉄格子：両脇・奥・入口の柱と上の枠
        Set<Block> bars = new HashSet<>();
        for (int y = MineLayout.TUNNEL_MIN_Y; y <= MineLayout.TUNNEL_MAX_Y; y++) {
            for (int x = west; x <= east; x++) {
                bars.add(world.getBlockAt(x, y, minZ));
                bars.add(world.getBlockAt(x, y, maxZ));
            }
            bars.add(world.getBlockAt(west, y, centerZ));
        }
        bars.add(world.getBlockAt(east, MineLayout.TUNNEL_MAX_Y, centerZ));
        for (Block block : bars) {
            set(block, ironBars(block, bars, west, minZ, maxZ));
        }
        // 中：吊りランタン（屋根の鎖から下げる）と、奥の鉄格子に掛けた看板
        set(world.getBlockAt(west + 1, MineLayout.TUNNEL_MAX_Y, centerZ), LANTERN);
        placeCageSign(world.getBlockAt(west + 1, MineLayout.TUNNEL_MIN_Y + 1, centerZ));
    }

    /** つながる向き（ほかの鉄格子と、外側の岩の壁）を決めた鉄格子。物理更新なしで置くので自分で決める。 */
    private static BlockData ironBars(Block block, Set<Block> bars, int west, int minZ, int maxZ) {
        Set<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
        for (BlockFace face : List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            if (bars.contains(block.getRelative(face))) {
                faces.add(face);
            }
        }
        if (block.getX() == west) {
            faces.add(BlockFace.WEST);
        }
        if (block.getZ() == minZ) {
            faces.add(BlockFace.NORTH);
        }
        if (block.getZ() == maxZ) {
            faces.add(BlockFace.SOUTH);
        }
        return Material.IRON_BARS.createBlockData(data -> {
            MultipleFacing facing = (MultipleFacing) data;
            for (BlockFace face : faces) {
                facing.setFace(face, true);
            }
        });
    }

    private void placeCageSign(Block block) {
        set(block, CAGE_SIGN);
        BlockState state = block.getState();
        if (state instanceof Sign sign) {
            sign.getSide(Side.FRONT).line(1, Component.text("ケージ", NamedTextColor.DARK_GRAY));
            sign.getSide(Side.FRONT).line(2, Component.text("（右クリック）", NamedTextColor.GRAY));
            sign.setWaxed(true);
            sign.update(true, false);
        }
    }

    /** X = x の列（坑道の断面）を空気にし、決まった位置なら坑木・明かりを置く。 */
    private void carveSlice(int section, int x) {
        int centerZ = MineLayout.sectionZ(section);
        int minZ = MineLayout.tunnelMinZ(section);
        int maxZ = MineLayout.tunnelMaxZ(section);
        boolean timber = TunnelPlan.isTimberX(x);
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = MineLayout.TUNNEL_MIN_Y; y <= MineLayout.TUNNEL_MAX_Y; y++) {
                BlockData data = AIR;
                if (timber && y == MineLayout.TUNNEL_MAX_Y) {
                    // 天井の板材
                    data = PLANKS;
                } else if (timber && z != centerZ) {
                    // 両脇の柱（柵）
                    data = FENCE;
                } else if (!timber && y == MineLayout.TUNNEL_MAX_Y && z == centerZ && TunnelPlan.isLightX(x)) {
                    data = LIGHT;
                }
                set(world.getBlockAt(x, y, z), data);
            }
        }
    }

    /** 坑道の奥半分にいるプレイヤーを、同じ向き・同じ速度のまま 64 ブロック手前へ移す。 */
    private void shiftBack(Player player) {
        Location location = player.getLocation();
        if (!location.getWorld().equals(world)) {
            return;
        }
        if (!SectionBounds.needsLoopShift(location.getX())) {
            return;
        }
        // 位置は絶対座標で指定し、速度だけ相対フラグで引き継ぐ（向きは今の値をそのまま渡す）
        teleporter.teleport(player, location.add(-MineLayout.LOOP_METERS, 0, 0),
                TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y,
                TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION);
    }

    private static void set(Block block, Material material) {
        if (block.getType() != material) {
            block.setType(material, false);
        }
    }

    private static void set(Block block, BlockData data) {
        if (!block.getBlockData().equals(data)) {
            block.setBlockData(data, false);
        }
    }
}
