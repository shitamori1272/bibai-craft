package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.mine.MineService;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;

/**
 * 手掘り（企画書 4-2、#14）。自分の坑道の切羽（3 × 3）を叩くとヒビが 1 段階進み、10 回目で崩れて 1m 進む。
 *
 * <p><b>叩いた判定</b>：アドベンチャーモードではクライアントがブロックの破壊を始めないので、破壊開始の
 * {@link PlayerInteractEvent}（LEFT_CLICK_BLOCK）が届かないことがある。そこで次の 2 つを両方見る。
 * <ul>
 *   <li>{@link PlayerInteractEvent} の LEFT_CLICK_BLOCK（メインハンドだけ）
 *   <li>{@link PlayerArmSwingEvent}（メインハンドだけ）。視線の先のブロックを自分で調べる
 * </ul>
 * 1 回の左クリックで両方が来ることがあるので、1 tick に数えるのは 1 回まで（毎秒 20 回までの上限も兼ねる）。
 *
 * <p>叩いた回数はメモリだけに持ち、切羽が動いたとき（自動の掘進など）・坑道ワールドを出たとき・ログアウトで消す。
 * サーバーのメインスレッドから使うこと。
 */
public final class ManualDigListener implements Listener {

    /** 切羽が崩れるまでに叩く回数。 */
    public static final int HITS_PER_METER = 10;
    /** 視線の先を調べる距離の既定値（アドベンチャー・サバイバルのブロック操作距離）。 */
    private static final double DEFAULT_REACH = 4.5;
    /** ヒビの表示に使う番号。本物のエンティティの番号と重ならないよう、int の上限近くを使う。 */
    private static final int CRACK_SOURCE_BASE = Integer.MAX_VALUE - 16;

    private final DataStore dataStore;
    private final MineService mineService;
    private final Map<UUID, Hits> hits = new HashMap<>();

    /** 叩いている切羽と回数。切羽は区画・階層・進み具合の整数 m で見分ける。 */
    private static final class Hits {
        final int section;
        final long level;
        final long wholeMeters;
        final int faceX;
        int count;
        int lastTick = Integer.MIN_VALUE;

        Hits(int section, long level, long wholeMeters, int faceX) {
            this.section = section;
            this.level = level;
            this.wholeMeters = wholeMeters;
            this.faceX = faceX;
        }

        boolean sameFace(int section, long level, long wholeMeters) {
            return this.section == section && this.level == level && this.wholeMeters == wholeMeters;
        }
    }

    public ManualDigListener(DataStore dataStore, MineService mineService) {
        this.dataStore = dataStore;
        this.mineService = mineService;
    }

    // ---- 叩いた判定 ----

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND
                || event.getClickedBlock() == null) {
            return;
        }
        hit(event.getPlayer(), event.getClickedBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwing(PlayerArmSwingEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        if (mineService.sectionOf(player).isEmpty()) {
            return;
        }
        RayTraceResult result = player.rayTraceBlocks(reach(player), FluidCollisionMode.NEVER);
        if (result != null && result.getHitBlock() != null) {
            hit(player, result.getHitBlock());
        }
    }

    /** 切羽を 1 回叩く。手掘りできる切羽でなければ何もしない。 */
    private void hit(Player player, Block block) {
        Optional<Integer> sectionOpt = mineService.sectionOf(player);
        Optional<Long> levelOpt = mineService.renderedLevel(player);
        Optional<Double> metersOpt = mineService.renderedMeters(player);
        if (sectionOpt.isEmpty() || levelOpt.isEmpty() || metersOpt.isEmpty()) {
            return;
        }
        int section = sectionOpt.get();
        long level = levelOpt.get();
        double meters = metersOpt.get();
        PlayerData data = dataStore.get(player);
        // 数値上の階層が進んでいる（ケージ待ち #22）・掘り切った坑道には切羽がない
        if (data.mine().level() != level || meters >= Formulas.tunnelLength(level)) {
            return;
        }
        if (!mineService.renderer().isFaceBlock(section, meters, block)) {
            return;
        }
        UUID id = player.getUniqueId();
        long whole = (long) Math.floor(meters);
        Hits state = hits.get(id);
        if (state == null || !state.sameFace(section, level, whole)) {
            if (state != null) {
                clearCracks(player, state);
            }
            state = new Hits(section, level, whole, MineLayout.faceX(meters));
            hits.put(id, state);
        }
        int tick = Bukkit.getCurrentTick();
        if (state.lastTick == tick) {
            return;
        }
        state.lastTick = tick;
        state.count++;
        if (state.count < HITS_PER_METER) {
            showCracks(player, state, (float) state.count / HITS_PER_METER);
            return;
        }
        // 10 回目：切羽が崩れて 1m 進む
        clearCracks(player, state);
        hits.remove(id);
        BlockData broken = block.getBlockData();
        DigEngine.Change change = DigEngine.manualMeter(data, dataStore.server());
        Digging.showProgress(mineService, id, change);
        playBreak(player, state, broken, change);
    }

    // ---- ほかの係から ----

    /** 切羽が動いていたら、叩いた回数とヒビを消す。自動の掘進のあとに呼ぶ。 */
    public void refresh(Player player) {
        Hits state = hits.get(player.getUniqueId());
        if (state == null) {
            return;
        }
        Optional<Integer> section = mineService.sectionOf(player);
        Optional<Long> level = mineService.renderedLevel(player);
        Optional<Double> meters = mineService.renderedMeters(player);
        if (section.isEmpty() || level.isEmpty() || meters.isEmpty()
                || !state.sameFace(section.get(), level.get(), (long) Math.floor(meters.get()))) {
            clearCracks(player, state);
            hits.remove(player.getUniqueId());
        }
    }

    /** 全員の叩いた回数とヒビを消す。プラグインを止めるときに呼ぶ。 */
    public void clearAll() {
        for (Map.Entry<UUID, Hits> entry : hits.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                clearCracks(player, entry.getValue());
            }
        }
        hits.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        hits.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        hits.remove(event.getPlayer().getUniqueId());
    }

    // ---- 表示 ----

    private void showCracks(Player player, Hits state, float progress) {
        World world = player.getWorld();
        int i = 0;
        for (int y = MineLayout.TUNNEL_MIN_Y; y <= MineLayout.TUNNEL_MAX_Y; y++) {
            for (int z = MineLayout.tunnelMinZ(state.section); z <= MineLayout.tunnelMaxZ(state.section); z++) {
                // 同じ番号のヒビは 1 か所にしか出ないので、9 ブロックそれぞれに別の番号を使う
                player.sendBlockDamage(new Location(world, state.faceX, y, z), progress, CRACK_SOURCE_BASE + i);
                i++;
            }
        }
    }

    private void clearCracks(Player player, Hits state) {
        if (player.isOnline()) {
            showCracks(player, state, 0f);
        }
    }

    private static void playBreak(Player player, Hits state, BlockData broken, DigEngine.Change change) {
        Location center = new Location(player.getWorld(), state.faceX + 0.5,
                (MineLayout.TUNNEL_MIN_Y + MineLayout.TUNNEL_MAX_Y + 1) / 2.0, MineLayout.sectionZ(state.section) + 0.5);
        player.playSound(center, Sound.BLOCK_STONE_BREAK, 1f, 0.8f);
        player.spawnParticle(Particle.BLOCK, center, 40, 0.8, 0.8, 0.8, 0, broken);
        if (change.coal().signum() > 0) {
            player.showTitle(Title.title(Component.empty(),
                    Component.text("+" + JapaneseFormat.format(change.coal()) + "個", NamedTextColor.GOLD),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(300))));
        }
    }

    private static double reach(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        return attribute != null ? attribute.getValue() : DEFAULT_REACH;
    }
}
