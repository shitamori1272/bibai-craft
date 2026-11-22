package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.world.MineChunkGenerator;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import io.papermc.paper.entity.TeleportFlag;
import java.util.Collection;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Lantern;
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

    /** ケージ（坑底の奥）の X。右クリックで降りる処理（#22）はこの列を見る。 */
    public static final int CAGE_X = MineLayout.SHAFT_MIN_X;
    /** 入坑したときに立つブロックの X（ケージの少し手前）。 */
    static final int ARRIVAL_X = MineLayout.SHAFT_MIN_X + 2;
    /** 人車の乗り場のレールを敷く西端。 */
    static final int RAIL_MIN_X = MineLayout.SHAFT_MIN_X + 4;

    private static final BlockData AIR = Material.AIR.createBlockData();
    private static final BlockData FENCE = Material.OAK_FENCE.createBlockData();
    private static final BlockData PLANKS = Material.OAK_PLANKS.createBlockData();
    private static final BlockData IRON_BARS = Material.IRON_BARS.createBlockData();
    private static final BlockData LODESTONE = Material.LODESTONE.createBlockData();
    private static final BlockData LIGHT = Material.LIGHT.createBlockData(data -> ((Light) data).setLevel(12));
    private static final BlockData LANTERN = Material.LANTERN.createBlockData(data -> ((Lantern) data).setHanging(true));
    private static final BlockData RAIL = Material.RAIL.createBlockData(
            data -> ((Rail) data).setShape(Rail.Shape.EAST_WEST));
    private static final BlockData CAGE_SIGN = Material.OAK_WALL_SIGN.createBlockData(
            data -> ((WallSign) data).setFacing(BlockFace.EAST));

    private final World world;
    private final Teleporter teleporter;

    public TunnelRenderer(World world, Teleporter teleporter) {
        this.world = Objects.requireNonNull(world);
        this.teleporter = Objects.requireNonNull(teleporter);
    }

    /**
     * 階層 level の地層。階層ごとの地層（企画書 5-5、#23）はここを差し替える。
     *
     * <p>ループ表示で巻き戻したことに気づかれないよう、X 方向には {@link MineLayout#LOOP_METERS} の周期にすること。
     */
    public Strata strataFor(long level) {
        return (x, y, z) -> MineChunkGenerator.material(MineLayout.stratumAt(y));
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

    /** block が区画 section の坑底のケージ（奥の列の断面）か。ケージで降りる処理（#22）に使う。 */
    public boolean isCageBlock(int section, Block block) {
        return block.getWorld().equals(world)
                && block.getX() == CAGE_X
                && MineLayout.inTunnelCrossSection(section, block.getY(), block.getZ());
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
        // ケージ：奥の列の両脇を鉄格子、中央の床に磁石石（仮の目印）、その上に看板
        for (int y = MineLayout.TUNNEL_MIN_Y; y <= MineLayout.TUNNEL_MAX_Y; y++) {
            set(world.getBlockAt(CAGE_X, y, minZ), IRON_BARS);
            set(world.getBlockAt(CAGE_X, y, maxZ), IRON_BARS);
        }
        set(world.getBlockAt(CAGE_X, MineLayout.TUNNEL_MIN_Y, centerZ), LODESTONE);
        placeCageSign(world.getBlockAt(CAGE_X, MineLayout.TUNNEL_MIN_Y + 1, centerZ));
        // 人車の乗り場：中央にレール
        for (int x = RAIL_MIN_X; x <= MineLayout.SHAFT_MAX_X; x++) {
            set(world.getBlockAt(x, MineLayout.TUNNEL_MIN_Y, centerZ), RAIL);
        }
        set(world.getBlockAt(MineLayout.SHAFT_MIN_X + 3, MineLayout.TUNNEL_MAX_Y, centerZ), LANTERN);
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
