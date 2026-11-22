package io.github.shitamori1272.coalclicker.boost;

import io.github.shitamori1272.coalclicker.CoalClicker;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.dig.DigEngine;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.mine.MineService;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * 良質な炭層（#63）の表示と、叩いたときの処理。クッキークリッカーのゴールデンクッキーに当たるもの。
 *
 * <p><b>出方</b>：自分の坑道にいる間、{@link RichSeamRules} の間隔（2〜5 分）ごとに、掘ってある範囲（X = 1〜切羽の手前）
 * のうちプレイヤーの前後 4 ブロック以内の、坑道の左右の壁（炭層の高さ Y41〜42）のブロックを 1 つ光らせる。
 * 光る炭層は、壁のブロックにぴったり重ねた {@link BlockDisplay}（金の混じった黒い石＝きんの縁取りの発光）で、
 * 本人にだけ見せる（{@link Player#showEntity}）。0.5 秒ごとに本人にだけパーティクルを出す。
 * 13 秒で消える。坑道を出る・ログアウトすると消え、待ち時間も最初から数え直す。
 *
 * <p><b>叩いた判定</b>：手掘り（{@code dig.ManualDigListener}）と同じく、LEFT_CLICK_BLOCK と腕振りの両方を見る。
 * 腕振りは視線の先のブロックを調べる。表示エンティティには当たり判定がないので、裏の壁のブロックで判定する。
 *
 * <p><b>効果</b>（{@link BoostEffect} の重みで抽選）：豊炭（毎秒 ×7、77 秒）・大当たり（その場で石炭）・
 * 手掘り祭り（手掘り ×777、13 秒）。タイトルと効果音で知らせ、続く効果は残り時間をボスバーに出す。
 * 倍率の値は {@link BoostService} が持ち、実際に石炭へ掛けるのは掘進の係（dig）の仕事。
 *
 * <p>サーバーのメインスレッドから使うこと。
 */
public final class RichSeam implements Listener {

    /** 見回りの間隔（tick）。 */
    private static final long PERIOD_TICKS = 10;
    /** プレイヤーの X から前後何ブロックまでに出すか。 */
    private static final int NEAR_BLOCKS = 4;
    /** 置き場所を探す回数。 */
    private static final int PLACE_ATTEMPTS = 12;
    /** 視線の先を調べる距離の既定値。 */
    private static final double DEFAULT_REACH = 4.5;
    /** 発光の縁取りの色（金）。 */
    private static final Color GLOW = Color.fromRGB(0xFFC83D);
    /** 表示を壁のブロックより少しだけ大きくして、ちらつかないようにする。 */
    private static final float OVERSIZE = 0.02f;

    private final CoalClicker plugin;
    private final DataStore dataStore;
    private final MineService mineService;
    private final BoostService boosts;
    private final Random random = new Random();
    /** 次の炭層を出す時刻（ミリ秒）。自分の坑道にいる人だけ。 */
    private final Map<UUID, Long> nextAt = new HashMap<>();
    /** 今光っている炭層。 */
    private final Map<UUID, Seam> seams = new HashMap<>();
    private final Map<UUID, Map<BoostEffect, BossBar>> bars = new HashMap<>();
    private final BukkitTask task;

    private record Seam(Block block, BlockDisplay display, long expiresAt) {
    }

    private RichSeam(CoalClicker plugin, BoostService boosts) {
        this.plugin = plugin;
        this.dataStore = plugin.dataStore();
        this.mineService = plugin.mineService();
        this.boosts = boosts;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, PERIOD_TICKS, PERIOD_TICKS);
    }

    /** リスナーと見回りのタスクを登録する。onEnable から、mineService と dataStore を作ったあとに呼ぶ。 */
    public static RichSeam register(CoalClicker plugin, BoostService boosts) {
        RichSeam richSeam = new RichSeam(plugin, boosts);
        plugin.getServer().getPluginManager().registerEvents(richSeam, plugin);
        return richSeam;
    }

    /** タスクを止め、光っている炭層とボスバーを消す。 */
    public void close() {
        task.cancel();
        HandlerList.unregisterAll(this);
        for (UUID id : Map.copyOf(seams).keySet()) {
            removeSeam(id);
        }
        for (Map.Entry<UUID, Map<BoostEffect, BossBar>> entry : bars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                entry.getValue().values().forEach(player::hideBossBar);
            }
        }
        bars.clear();
        nextAt.clear();
    }

    // ---- 見回り ----

    private void tick() {
        long now = boosts.now();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            updateBars(player);
            Optional<Integer> section = mineService.sectionOf(player);
            if (section.isEmpty()) {
                removeSeam(id);
                nextAt.remove(id);
                continue;
            }
            Seam seam = seams.get(id);
            if (seam != null) {
                if (now >= seam.expiresAt() || !seam.display().isValid()) {
                    removeSeam(id);
                    player.playSound(center(seam.block()), Sound.BLOCK_AMETHYST_BLOCK_STEP, 0.6f, 0.6f);
                    schedule(id, now);
                } else {
                    sparkle(player, seam.block());
                }
                continue;
            }
            Long at = nextAt.get(id);
            if (at == null) {
                schedule(id, now);
            } else if (now >= at && !trySpawn(player, section.get(), now)) {
                // 置ける壁がなかった（坑底にいて、まだ掘っていない など）。少ししてからもう一度
                nextAt.put(id, now + 5_000L);
            }
        }
        boosts.purgeExpired();
    }

    private void schedule(UUID id, long now) {
        nextAt.put(id, now + RichSeamRules.nextIntervalMillis(random));
    }

    private boolean trySpawn(Player player, int section, long now) {
        Optional<Double> meters = mineService.renderedMeters(player);
        if (meters.isEmpty()) {
            return false;
        }
        int endX = MineLayout.tunnelEndX(meters.get());
        if (endX < 1) {
            return false;
        }
        int px = player.getLocation().getBlockX();
        int lo = Math.clamp(px - NEAR_BLOCKS, 1, endX);
        int hi = Math.clamp(px + NEAR_BLOCKS, 1, endX);
        World world = player.getWorld();
        for (int i = 0; i < PLACE_ATTEMPTS; i++) {
            int x = random.nextInt(lo, hi + 1);
            int y = random.nextInt(MineLayout.SEAM_MIN_Y, MineLayout.SEAM_MAX_Y + 1);
            int z = random.nextBoolean() ? MineLayout.tunnelMinZ(section) - 1 : MineLayout.tunnelMaxZ(section) + 1;
            Block block = world.getBlockAt(x, y, z);
            if (block.getType().isSolid()) {
                spawn(player, block, now);
                return true;
            }
        }
        return false;
    }

    private void spawn(Player player, Block block, long now) {
        BlockDisplay display = block.getWorld().spawn(block.getLocation(), BlockDisplay.class, d -> {
            d.setVisibleByDefault(false);
            d.setPersistent(false);
            d.setBlock(Material.GILDED_BLACKSTONE.createBlockData());
            d.setGlowing(true);
            d.setGlowColorOverride(GLOW);
            d.setBrightness(new Display.Brightness(15, 15));
            d.setTransformation(new Transformation(
                    new Vector3f(-OVERSIZE / 2, -OVERSIZE / 2, -OVERSIZE / 2), new AxisAngle4f(),
                    new Vector3f(1 + OVERSIZE, 1 + OVERSIZE, 1 + OVERSIZE), new AxisAngle4f()));
        });
        player.showEntity(plugin, display);
        seams.put(player.getUniqueId(), new Seam(block, display, now + RichSeamRules.LIFETIME_MILLIS));
        nextAt.remove(player.getUniqueId());
        player.playSound(center(block), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        sparkle(player, block);
    }

    private void removeSeam(UUID id) {
        Seam seam = seams.remove(id);
        if (seam != null && seam.display().isValid()) {
            seam.display().remove();
        }
    }

    private static void sparkle(Player player, Block block) {
        player.spawnParticle(Particle.WAX_ON, center(block), 3, 0.45, 0.45, 0.45, 0);
    }

    private static Location center(Block block) {
        return block.getLocation().add(0.5, 0.5, 0.5);
    }

    // ---- 叩いた判定 ----

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND
                && event.getClickedBlock() != null) {
            hit(event.getPlayer(), event.getClickedBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwing(PlayerArmSwingEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND || !seams.containsKey(player.getUniqueId())) {
            return;
        }
        RayTraceResult result = player.rayTraceBlocks(reach(player), FluidCollisionMode.NEVER);
        if (result != null && result.getHitBlock() != null) {
            hit(player, result.getHitBlock());
        }
    }

    private void hit(Player player, Block block) {
        UUID id = player.getUniqueId();
        Seam seam = seams.get(id);
        if (seam == null || !seam.block().equals(block)) {
            return;
        }
        removeSeam(id);
        schedule(id, boosts.now());
        BoostEffect effect = BoostEffect.pick(random);
        Location at = center(block);
        player.spawnParticle(Particle.END_ROD, at, 20, 0.3, 0.3, 0.3, 0.08);
        player.spawnParticle(Particle.WAX_ON, at, 30, 0.6, 0.6, 0.6, 0);
        if (effect.timed()) {
            boosts.activate(id, effect);
            String detail = effect == BoostEffect.RICH_OUTPUT
                    ? "毎秒の石炭 ×" + format(effect.productionMultiplier())
                    : "手掘りの石炭 ×" + format(effect.clickMultiplier());
            title(player, effect, detail + "（" + effect.durationMillis() / 1000 + "秒）");
            player.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 1f, effect == BoostEffect.HAND_FESTIVAL ? 1.5f : 1.0f);
            updateBars(player);
        } else {
            BigNum coal = grantJackpot(player);
            title(player, effect, "+" + JapaneseFormat.format(coal) + "個");
            player.playSound(at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.7f);
            player.playSound(at, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.4f);
        }
    }

    /**
     * 大当たりの石炭を、今の石炭・累計採掘量・サーバー全体の累計採掘量に足し、進んだ分の坑道を直す。
     * 毎秒の石炭は豊炭の倍率を掛けない値で計算する。
     */
    private BigNum grantJackpot(Player player) {
        PlayerData data = dataStore.get(player);
        BigNum coal = RichSeamRules.jackpotCoal(data.coal(), Formulas.coalPerSecond(data));
        long oldLevel = DigEngine.level(data);
        double oldMeters = DigEngine.meters(data);
        data.setCoal(data.coal().add(coal));
        data.setTotalMined(data.totalMined().add(coal));
        dataStore.server().addServerTotalMined(coal);
        long newLevel = DigEngine.level(data);
        double newMeters = DigEngine.meters(data);
        // dig.Digging#showProgress と同じ知らせ方（あちらはパッケージ内だけで使えるため）
        if (newLevel > oldLevel) {
            mineService.onProgress(player, oldLevel, oldMeters, Formulas.tunnelLength(oldLevel));
            mineService.onProgress(player, newLevel, 0, newMeters);
        } else if (newMeters != oldMeters) {
            mineService.onProgress(player, newLevel, oldMeters, newMeters);
        }
        return coal;
    }

    private static void title(Player player, BoostEffect effect, String subtitle) {
        player.showTitle(Title.title(
                Component.text(effect.displayName() + "！", NamedTextColor.GOLD),
                Component.text(subtitle, NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(150), Duration.ofMillis(1800), Duration.ofMillis(500))));
    }

    // ---- ボスバー ----

    private void updateBars(Player player) {
        UUID id = player.getUniqueId();
        for (BoostEffect effect : BoostEffect.values()) {
            if (!effect.timed()) {
                continue;
            }
            long remaining = boosts.remainingMillis(id, effect);
            Map<BoostEffect, BossBar> mine = bars.get(id);
            BossBar bar = mine == null ? null : mine.get(effect);
            if (remaining <= 0) {
                if (bar != null) {
                    player.hideBossBar(bar);
                    mine.remove(effect);
                    if (mine.isEmpty()) {
                        bars.remove(id);
                    }
                }
                continue;
            }
            Component name = barName(effect, remaining);
            float progress = Math.clamp((float) remaining / effect.durationMillis(), 0f, 1f);
            if (bar == null) {
                bar = BossBar.bossBar(name, progress,
                        effect == BoostEffect.HAND_FESTIVAL ? BossBar.Color.RED : BossBar.Color.YELLOW,
                        BossBar.Overlay.PROGRESS);
                bars.computeIfAbsent(id, k -> new EnumMap<>(BoostEffect.class)).put(effect, bar);
            } else {
                bar.name(name);
                bar.progress(progress);
            }
            // ログインし直した人にも出す（同じボスバーを何度 show しても 1 本のまま）
            player.showBossBar(bar);
        }
    }

    private static Component barName(BoostEffect effect, long remainingMillis) {
        String detail = effect == BoostEffect.RICH_OUTPUT
                ? "毎秒の石炭 ×" + format(effect.productionMultiplier())
                : "手掘りの石炭 ×" + format(effect.clickMultiplier());
        long seconds = (remainingMillis + 999) / 1000;
        return Component.text(effect.displayName() + "　" + detail + "　残り " + seconds + " 秒",
                NamedTextColor.GOLD);
    }

    private static String format(double multiplier) {
        return multiplier == Math.rint(multiplier) ? Long.toString((long) multiplier) : Double.toString(multiplier);
    }

    // ---- 片付け ----

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        removeSeam(id);
        nextAt.remove(id);
        // ボスバーはプレイヤーと一緒に消える。ログインし直したら作り直す
        bars.remove(id);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        removeSeam(event.getPlayer().getUniqueId());
    }

    private static double reach(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        return attribute != null ? attribute.getValue() : DEFAULT_REACH;
    }
}
