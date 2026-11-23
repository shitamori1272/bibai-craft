package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.boost.BoostService;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.mine.MineService;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;

/**
 * 手掘り（企画書 4-2）。自分の坑道の切羽（3 × 3）を 1 回叩くごとに手掘り 1 回分の石炭
 * （{@link io.github.shitamori1272.coalclicker.game.Formulas#clickCoal}）がもらえ、手掘りの回数が 1 増える
 * （ツルハシの改良の出現条件）。良質な炭層の倍率（{@link BoostService}）があれば掛ける。
 * 石炭は累計採掘量とサーバー全体にも足し、坑道はそのぶん進む。
 *
 * <p>ヒビは見た目だけで、叩くたびに 1 段階進み 10 回で一周する（坑道の進み方には関係しない）。
 * 叩いたときは効果音と、切羽の手前に浮かぶ {@code +◯個}（{@link HitPopup}。アクションバーやタイトルとぶつからない）を出す。
 *
 * <p>手掘りできるのは、置いてある坑道の階層が数値上の階層と同じで、掘り切っていないときだけ（{@link DigEngine#face}）。
 * また、入坑で渡したツルハシ（{@link io.github.shitamori1272.coalclicker.mine.MinerPickaxe}）をメインハンドに
 * 持っているときだけ数える。持たずに切羽を叩いたら「ツルハシを持って掘ろう」と出し、持ち物になければ渡し直す。
 * 掘り切ってケージを待っているときに坑道の奥（看板・奥の壁）を叩くと、石炭は出ず「ケージで降りよう」と出す。
 *
 * <p><b>叩いた判定</b>：アドベンチャーモードではクライアントがブロックの破壊を始めないので、破壊開始の
 * {@link PlayerInteractEvent}（LEFT_CLICK_BLOCK）が届かないことがある。そこで次の 2 つを両方見る。
 * <ul>
 *   <li>{@link PlayerInteractEvent} の LEFT_CLICK_BLOCK（メインハンドだけ）
 *   <li>{@link PlayerArmSwingEvent}（メインハンドだけ）。視線の先をブロックとエンティティまとめて調べ、
 *       いちばん手前が切羽のブロックのときだけ数える。エンティティを攻撃した tick の振りは数えない
 * </ul>
 * 1 回の左クリックで両方が来ることがあるので、1 人が 1 tick に数えるのは 1 回まで（毎秒 20 回までの上限も兼ねる）。
 *
 * <p>ヒビの段階はメモリだけに持ち、切羽が動いたとき・坑道ワールドを出たとき・ログアウトで消す。
 * サーバーのメインスレッドから使うこと。
 */
public final class ManualDigListener implements Listener {

    /** ヒビが一周する回数。 */
    public static final int CRACK_STAGES = 10;
    /** ケージへの一言を出す間隔（tick）。 */
    private static final int HINT_INTERVAL_TICKS = 20;
    /** 視線の先を調べる距離の既定値（アドベンチャー・サバイバルのブロック操作距離）。 */
    private static final double DEFAULT_REACH = 4.5;
    /** ヒビの表示に使う番号。本物のエンティティの番号と重ならないよう、int の上限近くを使う。 */
    private static final int CRACK_SOURCE_BASE = Integer.MAX_VALUE - 16;

    private final DataStore dataStore;
    private final MineService mineService;
    private final BoostService boosts;
    private final HitPopup popup;
    private final Map<UUID, Cracks> cracks = new HashMap<>();
    /** 最後に叩いたと数えた tick（1 tick に 1 回の上限）。 */
    private final Map<UUID, Integer> hitTicks = new HashMap<>();
    /** 最後にケージへの一言を出した tick。 */
    private final Map<UUID, Integer> hintTicks = new HashMap<>();
    /** 最後にエンティティを叩いた tick。 */
    private final Map<UUID, Integer> attackTicks = new HashMap<>();

    /** ヒビを出している切羽（区画・X）と段階（0〜9）。 */
    private static final class Cracks {
        int section;
        int faceX;
        int stage;

        Cracks(int section, int faceX) {
            this.section = section;
            this.faceX = faceX;
        }

        boolean sameFace(int section, int faceX) {
            return this.section == section && this.faceX == faceX;
        }
    }

    public ManualDigListener(Plugin plugin, DataStore dataStore, MineService mineService, BoostService boosts) {
        this.dataStore = dataStore;
        this.mineService = mineService;
        this.boosts = boosts;
        this.popup = new HitPopup(plugin);
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
        // エンティティを叩いた振りは数えない（攻撃は振りより先に届く）
        Integer attacked = attackTicks.get(player.getUniqueId());
        if (attacked != null && attacked == Bukkit.getCurrentTick()) {
            return;
        }
        // ブロックとエンティティをまとめて調べ、いちばん手前が切羽のブロックのときだけ数える（浮かぶ文字は除く）
        Location eye = player.getEyeLocation();
        RayTraceResult result = player.getWorld().rayTrace(eye, eye.getDirection(), reach(player),
                FluidCollisionMode.NEVER, true, 0.0,
                entity -> !entity.equals(player) && !(entity instanceof Display));
        if (result != null && result.getHitEntity() == null && result.getHitBlock() != null) {
            hit(player, result.getHitBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttack(PrePlayerAttackEntityEvent event) {
        attackTicks.put(event.getPlayer().getUniqueId(), Bukkit.getCurrentTick());
    }

    /** 切羽を 1 回叩く。手掘りできる切羽でなければ何もしない（ケージ待ちなら一言だけ出す）。 */
    private void hit(Player player, Block block) {
        Optional<Integer> sectionOpt = mineService.sectionOf(player);
        Optional<Long> levelOpt = mineService.renderedLevel(player);
        Optional<Double> metersOpt = mineService.renderedMeters(player);
        if (sectionOpt.isEmpty() || levelOpt.isEmpty() || metersOpt.isEmpty()) {
            return;
        }
        int section = sectionOpt.get();
        long rendered = levelOpt.get();
        double meters = metersOpt.get();
        PlayerData data = dataStore.get(player);
        UUID id = player.getUniqueId();
        int tick = Bukkit.getCurrentTick();
        if (DigEngine.face(DigEngine.level(data), rendered, meters) == DigEngine.Face.AWAITING_CAGE) {
            // 掘り切った坑道には切羽がない。奥を叩いたら、ケージで降りるよう一言出す
            if (isEndOfTunnel(section, meters, block) && throttle(hintTicks, id, tick, HINT_INTERVAL_TICKS)) {
                popup.show(player, center(block), Component.text("ケージで降りよう", NamedTextColor.GRAY));
            }
            return;
        }
        if (!mineService.renderer().isFaceBlock(section, meters, block)) {
            return;
        }
        if (!mineService.pickaxe().holds(player)) {
            if (throttle(hintTicks, id, tick, HINT_INTERVAL_TICKS)) {
                popup.show(player, center(block), Component.text("ツルハシを持って掘ろう", NamedTextColor.GRAY));
                if (!mineService.pickaxe().has(player)) {
                    mineService.pickaxe().give(player);
                }
            }
            return;
        }
        if (!throttle(hitTicks, id, tick, 1)) {
            return;
        }
        BlockData broken = block.getBlockData();
        DigEngine.Change change = DigEngine.click(data, dataStore.server(), boosts.productionMultiplier(id),
                boosts.clickMultiplier(id));

        Cracks state = cracks.computeIfAbsent(id, ignored -> new Cracks(section, MineLayout.faceX(meters)));
        state.stage = (state.stage + 1) % CRACK_STAGES;
        playHit(player, block, broken, change, state.stage == 0);

        // 累計採掘量が増えた分、坑道を進める。切羽が動いたら、ヒビの段階はそのまま新しい切羽へ移す
        mineService.syncProgress(player);
        double now = mineService.renderedMeters(player).orElse(meters);
        int faceX = MineLayout.faceX(now);
        if (!state.sameFace(section, faceX)) {
            clearCracks(player, state);
            state.section = section;
            state.faceX = faceX;
        }
        if (DigEngine.face(DigEngine.level(data), rendered, now) == DigEngine.Face.OPEN) {
            showCracks(player, state, (float) state.stage / CRACK_STAGES);
        } else {
            // この 1 回で掘り切った（ケージ待ちになった）
            cracks.remove(id);
        }
    }

    /** 掘り切った坑道の奥（看板を掛ける列か、その先の壁）の断面のブロックか。 */
    private boolean isEndOfTunnel(int section, double meters, Block block) {
        return (block.getX() == MineLayout.tunnelEndX(meters) || block.getX() == MineLayout.faceX(meters))
                && MineLayout.inTunnelCrossSection(section, block.getY(), block.getZ())
                && block.getWorld().equals(mineService.renderer().arrival(section).getWorld());
    }

    // ---- ほかの係から ----

    /** 切羽が動いていたら、ヒビを消す。毎秒の石炭で坑道を進めたあとに呼ぶ。 */
    public void refresh(Player player) {
        Cracks state = cracks.get(player.getUniqueId());
        if (state == null) {
            return;
        }
        Optional<Integer> section = mineService.sectionOf(player);
        Optional<Double> meters = mineService.renderedMeters(player);
        if (section.isEmpty() || meters.isEmpty()
                || !state.sameFace(section.get(), MineLayout.faceX(meters.get()))) {
            clearCracks(player, state);
            cracks.remove(player.getUniqueId());
        }
    }

    /** 全員のヒビと浮かぶ文字を消す。プラグインを止めるときに呼ぶ。 */
    public void clearAll() {
        for (Map.Entry<UUID, Cracks> entry : cracks.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                clearCracks(player, entry.getValue());
            }
        }
        cracks.clear();
        popup.clearAll();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        cracks.remove(id);
        hitTicks.remove(id);
        hintTicks.remove(id);
        attackTicks.remove(id);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        cracks.remove(event.getPlayer().getUniqueId());
    }

    // ---- 表示 ----

    private void showCracks(Player player, Cracks state, float progress) {
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

    private void clearCracks(Player player, Cracks state) {
        if (player.isOnline()) {
            showCracks(player, state, 0f);
        }
    }

    private void playHit(Player player, Block block, BlockData broken, DigEngine.Change change, boolean cycled) {
        Location center = center(block);
        player.playSound(center, cycled ? Sound.BLOCK_STONE_BREAK : Sound.BLOCK_STONE_HIT, 1f, 0.8f);
        player.spawnParticle(Particle.BLOCK, center, cycled ? 40 : 8, 0.5, 0.5, 0.5, 0, broken);
        if (change.coal().signum() > 0) {
            popup.show(player, center, Component.text("+" + JapaneseFormat.format(change.coal()) + "個",
                    NamedTextColor.GOLD));
        }
    }

    /** {@code last} に覚えた tick から interval tick 以上たっていれば（または時計が戻っていれば）、今の tick を覚えて true。 */
    private static boolean throttle(Map<UUID, Integer> last, UUID id, int tick, int interval) {
        Integer previous = last.get(id);
        if (previous != null && tick >= previous && tick - previous < interval) {
            return false;
        }
        last.put(id, tick);
        return true;
    }

    private static Location center(Block block) {
        return block.getLocation().add(0.5, 0.5, 0.5);
    }

    private static double reach(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        return attribute != null ? attribute.getValue() : DEFAULT_REACH;
    }
}
