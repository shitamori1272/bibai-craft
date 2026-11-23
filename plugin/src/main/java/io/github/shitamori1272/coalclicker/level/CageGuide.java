package io.github.shitamori1272.coalclicker.level;

import io.github.shitamori1272.coalclicker.CoalClicker;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.effect.SoundFade;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.mine.MineService;
import io.github.shitamori1272.coalclicker.mine.TunnelRenderer;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 * ケージへの誘導と降りる演出（企画書 5-5）、着いたときの到達ボーナス（企画書 4-4）。
 *
 * <p>自分の坑道にいて、数値上の階層が置いてある坑道より進んでいる人に、1 秒ごとに次を出す。
 * <ul>
 *   <li>ボスバー「第 ◯ 坑道まで掘り進んでいます。ケージで降りよう」
 *   <li>掘り切った切羽の看板（区画・置いてある階層・行き先が変わったら書き直す。作り直しで消える）
 *   <li>足元から坑底のケージの方へ並ぶパーティクル（本人にだけ見える）
 * </ul>
 *
 * <p>坑底のケージを右クリックすると、次の順で降りる。
 * <ol>
 *   <li>暗転（盲目）と鉄の扉を閉める音（ケージの扉）
 *   <li>巻揚機の音：トロッコの走行音を低く流し、鎖を置く音を一定間隔で鳴らす（ワイヤーが巻き出される音）。
 *       止まる前にフェードアウトさせる（{@link SoundFade}）
 *   <li>ピストンの音でケージが止まる
 *   <li>区画を最新の階層に作り直して坑底へテレポートし、タイトル「第 ◯ 坑道」「深さ ◯◯m」
 * </ol>
 * サーバーのメインスレッドから使うこと。
 */
public final class CageGuide implements Listener {

    /** 誘導を見直す間隔（tick）。 */
    private static final long PERIOD_TICKS = 20;
    /** 降りる演出の長さ（tick）。この tick に作り直してテレポートする。 */
    static final int DESCENT_TICKS = 40;
    /** 鎖の音を鳴らす間隔（tick）。 */
    private static final int CHAIN_INTERVAL = 6;
    /** 巻揚機の音（トロッコの走行音を低く）の音量と高さ。 */
    private static final float RIDING_VOLUME = 0.6f;
    private static final float RIDING_PITCH = 0.5f;
    /** 巻揚機の音のフェードアウトの段の数と、1 段の長さ（tick）。 */
    private static final int FADE_STEPS = 4;
    private static final int FADE_INTERVAL = 3;
    /** 着いてから到達ボーナスのタイトルを出すまで（tick）。坑道のタイトルと重ならないようにする。 */
    private static final long BONUS_TITLE_DELAY = 70;

    private final CoalClicker plugin;
    private final DataStore dataStore;
    private final MineService mineService;
    private final Map<UUID, Guide> guides = new HashMap<>();
    private final Map<UUID, BukkitTask> descents = new HashMap<>();
    private final BukkitTask task;

    /** 1 人分の誘導の表示。 */
    private static final class Guide {
        final BossBar bar;
        long barLevel;
        /** 看板を置いた区画・置いてある階層・看板に書いた行き先の階層。まだなら sign = false。 */
        boolean sign;
        int signSection;
        long signLevel;
        long signTarget;

        Guide(long level) {
            this.barLevel = level;
            this.bar = BossBar.bossBar(Component.text(LevelGuide.bossBarText(level), NamedTextColor.GOLD),
                    1f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        }
    }

    private CageGuide(CoalClicker plugin) {
        this.plugin = plugin;
        this.dataStore = plugin.dataStore();
        this.mineService = plugin.mineService();
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, PERIOD_TICKS, PERIOD_TICKS);
    }

    /** リスナーと見回りのタスクを登録し、入坑・ケージで着いたときの到達ボーナスを設定する。onEnable から呼ぶ。 */
    public static CageGuide register(CoalClicker plugin) {
        CageGuide guide = new CageGuide(plugin);
        plugin.getServer().getPluginManager().registerEvents(guide, plugin);
        plugin.mineService().onArrival(guide::onArrived);
        return guide;
    }

    /** タスクと降りる演出を止め、全員のボスバーを消す。MineService の shutdown より前に呼ぶ。 */
    public void close() {
        task.cancel();
        for (BukkitTask descent : descents.values()) {
            descent.cancel();
        }
        descents.clear();
        for (UUID id : List.copyOf(guides.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                hide(player);
            }
        }
        guides.clear();
    }

    // ---- 誘導 ----

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    private void update(Player player) {
        UUID id = player.getUniqueId();
        Optional<Integer> section = mineService.sectionOf(player);
        Optional<Long> rendered = mineService.renderedLevel(player);
        Optional<Double> meters = mineService.renderedMeters(player);
        long numeric = Formulas.levelOf(dataStore.get(player).totalMined());
        if (section.isEmpty() || rendered.isEmpty() || meters.isEmpty() || descents.containsKey(id)
                || !LevelGuide.awaitingCage(numeric, rendered.get())) {
            hide(player);
            return;
        }
        Guide guide = guides.get(id);
        if (guide == null) {
            guide = new Guide(numeric);
            guides.put(id, guide);
            player.showBossBar(guide.bar);
        } else if (guide.barLevel != numeric) {
            guide.barLevel = numeric;
            guide.bar.name(Component.text(LevelGuide.bossBarText(numeric), NamedTextColor.GOLD));
        }
        // 置いてある坑道を掘り切っていれば、切羽に看板を出す（区画・階層・行き先が変わるまで 1 回だけ）
        // 数値上の階層がさらに進んだら、看板の行き先も書き直す
        boolean sameSign = guide.sign && guide.signSection == section.get() && guide.signLevel == rendered.get()
                && guide.signTarget == numeric;
        if (!sameSign && meters.get() >= Formulas.tunnelLength(rendered.get())) {
            mineService.renderer().placeEndSign(section.get(), meters.get(), LevelGuide.endSignLines(numeric));
            guide.sign = true;
            guide.signSection = section.get();
            guide.signLevel = rendered.get();
            guide.signTarget = numeric;
        }
        showTrail(player, section.get());
    }

    /** 足元から坑底のケージの方へ、床の上にパーティクルを並べる（本人にだけ見える）。 */
    private static void showTrail(Player player, int section) {
        double y = MineLayout.FLOOR_Y + 1.15;
        double z = MineLayout.sectionZ(section) + 0.5;
        for (double x : LevelGuide.guidePointXs(player.getLocation().getX(), TunnelRenderer.CAGE_X)) {
            player.spawnParticle(Particle.END_ROD, new Location(player.getWorld(), x, y, z), 1, 0, 0, 0, 0);
        }
    }

    private void hide(Player player) {
        Guide guide = guides.remove(player.getUniqueId());
        if (guide != null) {
            player.hideBossBar(guide.bar);
        }
    }

    // ---- ケージで降りる ----

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND
                || event.getClickedBlock() == null) {
            return;
        }
        Player player = event.getPlayer();
        Optional<Integer> section = mineService.sectionOf(player);
        if (section.isEmpty() || !mineService.renderer().isCageBlock(section.get(), event.getClickedBlock())) {
            return;
        }
        event.setCancelled(true);
        startDescent(player);
    }

    private void startDescent(Player player) {
        UUID id = player.getUniqueId();
        if (descents.containsKey(id)) {
            return;
        }
        PlayerData data = dataStore.get(player);
        Optional<Long> rendered = mineService.renderedLevel(player);
        if (rendered.isEmpty()) {
            return;
        }
        long numeric = Formulas.levelOf(data.totalMined());
        if (!LevelGuide.awaitingCage(numeric, rendered.get())) {
            player.sendMessage(Component.text("ここが最新の" + LevelGuide.descentTitle(numeric)
                    + "です。掘り切ると、ケージでもっと下へ降りられます。", NamedTextColor.GRAY));
            return;
        }
        hide(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, DESCENT_TICKS + 15, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, DESCENT_TICKS, 4, false, false, false));
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 0.8f);
        SoundFade.play(player, Sound.ENTITY_MINECART_RIDING, RIDING_VOLUME, RIDING_PITCH);
        int[] elapsed = {0};
        BukkitTask descent = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            elapsed[0]++;
            stepDescent(player, elapsed[0]);
        }, 1, 1);
        descents.put(id, descent);
    }

    private void stepDescent(Player player, int tick) {
        if (!player.isOnline() || mineService.sectionOf(player).isEmpty()) {
            // 途中で抜けた・坑道を出た
            finishDescent(player);
            return;
        }
        if (tick < DESCENT_TICKS - 4 && tick % CHAIN_INTERVAL == 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 0.8f, 0.5f);
        }
        if (tick == DESCENT_TICKS - 4 - FADE_STEPS * FADE_INTERVAL) {
            // ケージが止まるまでに、巻揚機の音をぶつっと切らずに小さくする
            SoundFade.fadeOut(plugin, player, Sound.ENTITY_MINECART_RIDING, RIDING_VOLUME, RIDING_PITCH,
                    FADE_STEPS, FADE_INTERVAL);
        }
        if (tick == DESCENT_TICKS - 4) {
            player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.8f, 0.6f);
        }
        if (tick < DESCENT_TICKS) {
            return;
        }
        finishDescent(player);
        if (!mineService.descend(player)) {
            return;
        }
        long level = Formulas.levelOf(dataStore.get(player).totalMined());
        player.showTitle(Title.title(
                Component.text(LevelGuide.descentTitle(level), NamedTextColor.GOLD),
                Component.text(LevelGuide.descentSubtitle(level), NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2500), Duration.ofMillis(1000))));
        player.sendMessage(Component.text(LevelGuide.descentTitle(level) + "（" + LevelGuide.descentSubtitle(level)
                + "）に着きました。", NamedTextColor.GOLD));
    }

    private void finishDescent(Player player) {
        BukkitTask descent = descents.remove(player.getUniqueId());
        if (descent != null) {
            descent.cancel();
        }
        if (player.isOnline()) {
            player.stopSound(Sound.ENTITY_MINECART_RIDING);
        }
    }

    // ---- 到達ボーナス ----

    /** 入坑・ケージで最新の階層に着いたとき（MineService から呼ばれる）。はじめての階層なら到達ボーナスを渡す。 */
    private void onArrived(Player player) {
        hide(player);
        PlayerData data = dataStore.get(player);
        long arrived = mineService.renderedLevel(player).orElseGet(() -> Formulas.levelOf(data.totalMined()));
        Optional<ArrivalBonus.Result> bonus = ArrivalBonus.claim(data, arrived);
        // ボーナスで累計採掘量が増えた分、坑道を進める（階層が進んだら掘り切った状態にしてケージへ誘導する）
        mineService.syncProgress(player);
        bonus.ifPresent(result -> {
            player.sendMessage(Component.text(LevelGuide.bonusMessage(result), NamedTextColor.GOLD));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1f);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.showTitle(Title.title(
                            Component.text("到達ボーナス", NamedTextColor.GOLD),
                            Component.text("+" + JapaneseFormat.format(result.coal()) + "個", NamedTextColor.YELLOW),
                            Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(700))));
                }
            }, BONUS_TITLE_DELAY);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        finishDescent(player);
        hide(player);
    }
}
