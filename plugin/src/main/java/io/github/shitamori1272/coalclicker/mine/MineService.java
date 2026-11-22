package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.MineState;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.world.MineWorld;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

/**
 * 入坑・地上へ戻る・坑内の保護と、坑道のブロックを直すタイミングの係（企画書 5-2〜5-4、7 章）。
 *
 * <p>入坑中のプレイヤーは {@link Session} で覚える（メモリだけ。保存はしない）。
 * サーバーの再起動などで覚えていない状態で坑道ワールドにログインしたプレイヤーは、主ワールドの初期地点へ戻し、
 * ゲームモードをサーバーの既定に戻す。サーバーのメインスレッドから使うこと。
 */
public final class MineService implements Listener {

    private final Plugin plugin;
    private final DataStore dataStore;
    private final MineWorld mineWorld;
    private final TunnelRenderer renderer;
    /** 入坑中（またはログアウト中で、次のログインで地上へ戻す）のプレイヤー。 */
    private final Map<UUID, Session> sessions = new HashMap<>();

    /** 入坑中の状態。renderedLevel / renderedMeters はワールド上に今置いてある坑道の状態。 */
    private static final class Session {
        final Location returnTo;
        final GameMode returnMode;
        int section;
        long renderedLevel;
        double renderedMeters;

        Session(Location returnTo, GameMode returnMode) {
            this.returnTo = returnTo;
            this.returnMode = returnMode;
        }
    }

    public MineService(Plugin plugin, DataStore dataStore, MineWorld mineWorld) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.mineWorld = mineWorld;
        this.renderer = new TunnelRenderer(mineWorld.world());
    }

    /** 坑道のブロックを置く係。 */
    public TunnelRenderer renderer() {
        return renderer;
    }

    // ---- 入坑・地上へ戻る ----

    /**
     * player を自分の坑道へ入坑させる（{@code /coal mine}）。区画がなければ割り当て、
     * 区画を今の階層・進み具合に作り直して、坑底へ東向きにテレポートし、アドベンチャーモードにする。
     */
    public void enter(Player player) {
        MineState mine = dataStore.get(player).mine();
        if (!mine.hasSection()) {
            mine.setSection(dataStore.server().allocateSection());
        }
        Session session = sessions.get(player.getUniqueId());
        boolean fresh = session == null || !inMineWorld(player);
        if (fresh) {
            session = new Session(player.getLocation(), player.getGameMode());
            sessions.put(player.getUniqueId(), session);
        }
        render(session, mine);
        if (!player.teleport(renderer.arrival(session.section), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            if (fresh) {
                sessions.remove(player.getUniqueId());
            }
            player.sendMessage(Component.text("入坑できませんでした。", NamedTextColor.RED));
            return;
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(Component.text("第" + (mine.level() + 1) + "坑道に入坑しました。", NamedTextColor.GOLD)
                .append(Component.text(" /coal surface で地上へ戻れます", NamedTextColor.GRAY)));
    }

    /** player を地上へ戻す（{@code /coal surface}）。入坑前の場所とゲームモードに戻す。 */
    public void leave(Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session == null && !inMineWorld(player)) {
            player.sendMessage(Component.text("坑内にいません。", NamedTextColor.RED));
            return;
        }
        restore(player, session);
        player.sendMessage(Component.text("地上へ戻りました。", NamedTextColor.GOLD));
    }

    // ---- ほかの機能から使う API ----

    /** player が今自分の坑道にいれば、その区画番号。 */
    public Optional<Integer> sectionOf(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null || !player.isOnline() || !inMineWorld(player)) {
            return Optional.empty();
        }
        return Optional.of(session.section);
    }

    /** player が今自分の坑道にいれば、ワールド上に置いてある坑道の進み具合（m）。 */
    public Optional<Double> renderedMeters(Player player) {
        return sectionOf(player).map(section -> sessions.get(player.getUniqueId()).renderedMeters);
    }

    /**
     * 進み具合が変わったことを知らせる（手掘り #14・自動の掘進 #15 から呼ぶ）。
     * 持ち主が自分の坑道にいて、置いてある坑道と同じ階層のときだけブロックを直す（企画書 5-3）。
     * oldMeters は参考で、実際には前回置いたときの進み具合からの差分を直す。
     */
    public void onProgress(UUID playerId, long level, double oldMeters, double newMeters) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        Session session = sessions.get(playerId);
        if (session == null || !inMineWorld(player) || session.renderedLevel != level) {
            return;
        }
        renderer.advanceTo(session.section, level, session.renderedMeters, newMeters, List.of(player));
        session.renderedMeters = newMeters;
        keepInside(player, session);
    }

    /** {@link #onProgress(UUID, long, double, double)} のプレイヤー版。 */
    public void onProgress(Player player, long level, double oldMeters, double newMeters) {
        onProgress(player.getUniqueId(), level, oldMeters, newMeters);
    }

    /**
     * player の区画を、保存データの今の階層・進み具合に作り直す（ケージで降りたとき #22 など）。
     * 坑道にいなければ何もしない。掘ってある範囲の外にいたら坑底へ戻す。
     */
    public void rebuildFor(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null || !inMineWorld(player)) {
            return;
        }
        render(session, dataStore.get(player).mine());
        keepInside(player, session);
    }

    /** 坑内の全員を地上へ戻す。プラグインを止めるときに呼ぶ。 */
    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Session session = sessions.remove(player.getUniqueId());
            if (session != null || inMineWorld(player)) {
                restore(player, session);
            }
        }
        sessions.clear();
    }

    // ---- 内部 ----

    private void render(Session session, MineState mine) {
        long level = mine.level();
        double meters = TunnelPlan.meters(mine.progress(), Formulas.tunnelLength(level));
        session.section = mine.section();
        session.renderedLevel = level;
        session.renderedMeters = meters;
        renderer.rebuild(session.section, level, meters);
    }

    /** 掘ってある範囲の外（岩の中など）にいたら坑底へ戻す。 */
    private void keepInside(Player player, Session session) {
        if (!renderer.inOpenArea(session.section, session.renderedMeters, player.getLocation())) {
            player.teleport(renderer.arrival(session.section), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    /** 地上へ戻し、ゲームモードを戻す。session がなければ主ワールドの初期地点とサーバーの既定のゲームモード。 */
    private void restore(Player player, Session session) {
        Location target = session != null && session.returnTo.isWorldLoaded()
                && !mineWorld.world().equals(session.returnTo.getWorld())
                ? session.returnTo : surfaceSpawn();
        player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setGameMode(session != null ? session.returnMode : defaultGameMode());
    }

    private boolean inMineWorld(Player player) {
        return mineWorld.world().equals(player.getWorld());
    }

    private boolean isMine(World world) {
        return mineWorld.world().equals(world);
    }

    private static Location surfaceSpawn() {
        return Bukkit.getWorlds().getFirst().getSpawnLocation();
    }

    /** 地上でのゲームモード。サーバーの既定がアドベンチャー・観戦ならサバイバルにする。 */
    private static GameMode defaultGameMode() {
        GameMode mode = Bukkit.getDefaultGameMode();
        return mode == GameMode.CREATIVE ? GameMode.CREATIVE : GameMode.SURVIVAL;
    }

    // ---- 入退出 ----

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 坑内でログアウトした人のセッションは残してあるので、ここで地上へ戻す
        Session session = sessions.remove(player.getUniqueId());
        if (session == null && !inMineWorld(player)) {
            return;
        }
        // ログイン処理が終わってから動かす
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && (session != null || inMineWorld(player))) {
                restore(player, session);
                player.sendMessage(Component.text("坑内で接続が切れたため、地上へ戻しました。", NamedTextColor.GRAY));
            }
        });
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        // ほかのプラグインやコマンドで坑道ワールドから出たら、入坑前のゲームモードに戻す
        if (isMine(event.getFrom())) {
            Session session = sessions.remove(event.getPlayer().getUniqueId());
            if (session != null) {
                event.getPlayer().setGameMode(session.returnMode);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session != null) {
            event.setRespawnLocation(renderer.arrival(session.section));
        }
    }

    // ---- 坑内の保護 ----

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!isMine(event.getFrom().getWorld())) {
            return;
        }
        switch (event.getCause()) {
            // コーラスフルーツは CONSUMABLE_EFFECT
            case ENDER_PEARL, CONSUMABLE_EFFECT -> event.setCancelled(true);
            default -> {
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (isMine(event.getEntity().getWorld())) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        if (isMine(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (isMine(event.getEntity().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (isMine(event.getEntity().getWorld())) {
            event.setCancelled(true);
        }
    }
}
