package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.MineState;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.world.MineWorld;
import io.papermc.paper.entity.TeleportFlag;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * 入坑・地上へ戻る・坑内の保護と、坑道のブロックを直すタイミングの係（企画書 5-2〜5-4、7 章）。
 *
 * <p>入坑中のプレイヤーは {@link Session} で覚える（メモリだけ。保存はしない）。
 * サーバーの再起動などで覚えていない状態で坑道ワールドにいるプレイヤーは、主ワールドの初期地点へ戻し、
 * ゲームモードをサーバーの既定に戻す。坑道ワールドへのテレポートは、この係が行うものと、
 * 本人の区画の掘ってある範囲の中へのものだけを通す。サーバーのメインスレッドから使うこと。
 */
public final class MineService implements Listener {

    /** 坑道ワールドにいる人の見回りの間隔（tick）。 */
    private static final long PATROL_INTERVAL_TICKS = 20;

    private final Plugin plugin;
    private final DataStore dataStore;
    private final MineWorld mineWorld;
    private final TunnelRenderer renderer;
    /** 入坑中（またはログアウト中で、次のログインで地上へ戻す）のプレイヤー。 */
    private final Map<UUID, Session> sessions = new HashMap<>();
    /** 入坑の準備（チャンクの読み込み）を待っているプレイヤー。 */
    private final Set<UUID> preparing = new HashSet<>();
    /** この係がテレポートさせている最中のプレイヤー。テレポートの検査を素通りさせる。 */
    private final Set<UUID> teleporting = new HashSet<>();
    private final BukkitTask patrolTask;
    /** 坑道の最新の階層に着いたとき（入坑・ケージで降りたとき）に呼ぶ処理。到達ボーナス（#24）に使う。 */
    private Consumer<Player> arrivalHandler = player -> {
    };

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
        this.renderer = new TunnelRenderer(mineWorld.world(), this::teleport);
        this.patrolTask = Bukkit.getScheduler().runTaskTimer(plugin, this::patrol,
                PATROL_INTERVAL_TICKS, PATROL_INTERVAL_TICKS);
    }

    /** 坑道のブロックを置く係。 */
    public TunnelRenderer renderer() {
        return renderer;
    }

    // ---- 入坑・地上へ戻る ----

    /**
     * player を自分の坑道へ入坑させる（{@code /coal mine}）。区画がなければ割り当て、区画のチャンクを非同期で読み込んでから、
     * 区画を今の階層・進み具合に作り直して、坑底へ東向きにテレポートし、アドベンチャーモードにする。
     */
    public void enter(Player player) {
        UUID id = player.getUniqueId();
        if (preparing.contains(id)) {
            player.sendMessage(Component.text("入坑の準備中です。少し待ってください。", NamedTextColor.GRAY));
            return;
        }
        MineState mine = dataStore.get(player).mine();
        if (!mine.hasSection()) {
            // 割り当てでサーバーのデータ、setSection でプレイヤーのデータがそれぞれ保存対象になる
            mine.setSection(dataStore.server().allocateSection());
        }
        player.sendMessage(Component.text("入坑の準備中…", NamedTextColor.GRAY));
        preparing.add(id);
        World world = mineWorld.world();
        CompletableFuture<?>[] loads = SectionBounds.chunks(mine.section()).stream()
                .map(chunk -> world.getChunkAtAsync(chunk.x(), chunk.z()))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(loads).whenComplete((ignored, error) -> onMainThread(() -> finishEnter(id, error)));
    }

    private void finishEnter(UUID id, Throwable error) {
        preparing.remove(id);
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline() || !plugin.isEnabled()) {
            return;
        }
        if (error != null) {
            plugin.getLogger().log(Level.WARNING, "坑道のチャンクを読み込めませんでした: " + player.getName(), error);
            player.sendMessage(Component.text("入坑できませんでした。", NamedTextColor.RED));
            return;
        }
        MineState mine = dataStore.get(player).mine();
        Session session = sessions.get(id);
        boolean fresh = session == null || !inMineWorld(player);
        if (fresh) {
            session = new Session(player.getLocation(), player.getGameMode());
            sessions.put(id, session);
        }
        render(session, mine);
        if (!teleport(player, renderer.arrival(session.section))) {
            if (fresh) {
                sessions.remove(id);
            }
            player.sendMessage(Component.text("入坑できませんでした。", NamedTextColor.RED));
            return;
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(Component.text("第" + (mine.level() + 1) + "坑道に入坑しました。", NamedTextColor.GOLD)
                .append(Component.text(" /coal surface で地上へ戻れます", NamedTextColor.GRAY)));
        // 入坑も「着いた」に数える（企画書 4-3）
        arrivalHandler.accept(player);
    }

    /** player を地上へ戻す（{@code /coal surface}）。入坑前の場所とゲームモードに戻す。 */
    public void leave(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null && !inMineWorld(player)) {
            player.sendMessage(Component.text("坑内にいません。", NamedTextColor.RED));
            return;
        }
        if (restore(player, session)) {
            player.sendMessage(Component.text("地上へ戻りました。", NamedTextColor.GOLD));
        } else {
            player.sendMessage(Component.text("地上へ戻れませんでした。もう一度試してください。", NamedTextColor.RED));
        }
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

    /** player が今自分の坑道にいれば、ワールド上に置いてある坑道の階層。数値上の階層より遅れていることがある（#22）。 */
    public Optional<Long> renderedLevel(Player player) {
        return sectionOf(player).map(section -> sessions.get(player.getUniqueId()).renderedLevel);
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

    /**
     * ケージで降りる（#22）。player の区画を保存データの今の階層・進み具合に作り直し、坑底へテレポートさせて、
     * 着いたときの処理（到達ボーナス）を呼ぶ。テレポートに失敗しても区画は作り直してあり、坑底は必ず掘ってあるので
     * 着いたものとして扱う（範囲の外にいれば坑底へ戻す）。
     *
     * @return 自分の坑道にいて降りられたら true
     */
    public boolean descend(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null || !inMineWorld(player)) {
            return false;
        }
        render(session, dataStore.get(player).mine());
        if (!teleport(player, renderer.arrival(session.section))) {
            keepInside(player, session);
        }
        arrivalHandler.accept(player);
        return true;
    }

    /** 着いたときの処理を設定する。onEnable から 1 回だけ呼ぶ。 */
    public void onArrival(Consumer<Player> handler) {
        this.arrivalHandler = Objects.requireNonNull(handler);
    }

    /** player が坑内（入坑中、または坑道ワールド）にいるか。 */
    public boolean inMine(Player player) {
        return sessions.containsKey(player.getUniqueId()) || inMineWorld(player);
    }

    /**
     * 坑内にいれば地上へ戻す（メッセージは出さない）。管理者のリセット（#16）などで使う。
     *
     * @return もともと坑内にいないか、地上へ戻せたら true
     */
    public boolean returnToSurface(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null && !inMineWorld(player)) {
            return true;
        }
        return restore(player, session);
    }

    /** 坑内の全員を地上へ戻す。プラグインを止めるときに呼ぶ。 */
    public void shutdown() {
        patrolTask.cancel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Session session = sessions.get(player.getUniqueId());
            if (session != null || inMineWorld(player)) {
                restore(player, session);
            }
        }
        sessions.clear();
        preparing.clear();
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
            teleport(player, renderer.arrival(session.section));
        }
    }

    /**
     * 地上へ戻し、ゲームモードを戻す。session がなければ主ワールドの初期地点とサーバーの既定のゲームモード。
     * テレポートできたときだけセッションを消してゲームモードを戻し、true を返す。
     */
    private boolean restore(Player player, Session session) {
        Location target = session != null && session.returnTo.isWorldLoaded()
                && !mineWorld.world().equals(session.returnTo.getWorld())
                ? session.returnTo : surfaceSpawn();
        if (!teleport(player, target)) {
            return false;
        }
        sessions.remove(player.getUniqueId());
        player.setGameMode(session != null ? session.returnMode : defaultGameMode());
        return true;
    }

    /** この係によるテレポート。坑道ワールドへのテレポートの検査を素通りする。 */
    private boolean teleport(Player player, Location to, TeleportFlag... flags) {
        UUID id = player.getUniqueId();
        teleporting.add(id);
        try {
            return player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN, flags);
        } finally {
            teleporting.remove(id);
        }
    }

    /**
     * 坑道ワールドの見回り。セッションのない人は地上へ戻し、セッションのある人が自分の坑道の外にいたら坑底へ戻す。
     */
    private void patrol() {
        for (Player player : mineWorld.world().getPlayers()) {
            if (player.isDead() || preparing.contains(player.getUniqueId())) {
                continue;
            }
            Session session = sessions.get(player.getUniqueId());
            if (session == null) {
                if (restore(player, null)) {
                    player.sendMessage(Component.text("坑道へは /coal mine で入ってください。", NamedTextColor.GRAY));
                }
            } else {
                Location location = player.getLocation();
                if (!SectionBounds.inSectionTunnel(session.section, location.getX(), location.getZ())) {
                    teleport(player, renderer.arrival(session.section));
                }
            }
        }
    }

    private void onMainThread(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, task);
        }
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
        if (!sessions.containsKey(player.getUniqueId()) && !inMineWorld(player)) {
            return;
        }
        // ログイン処理が終わってから動かす
        Bukkit.getScheduler().runTask(plugin, () -> {
            Session session = sessions.get(player.getUniqueId());
            if (!player.isOnline() || (session == null && !inMineWorld(player))) {
                return;
            }
            if (restore(player, session)) {
                player.sendMessage(Component.text("坑内で接続が切れたため、地上へ戻しました。", NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("地上へ戻れませんでした。/coal surface で戻ってください。",
                        NamedTextColor.RED));
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

    /**
     * 坑道ワールドへのテレポートの検査。この係のテレポートと、本人の区画の掘ってある範囲の中へのものだけを通す。
     * 坑内でのエンダーパール・コーラスフルーツ（CONSUMABLE_EFFECT）は止める。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (teleporting.contains(player.getUniqueId())) {
            return;
        }
        if (isMine(event.getFrom().getWorld())) {
            switch (event.getCause()) {
                case ENDER_PEARL, CONSUMABLE_EFFECT -> {
                    event.setCancelled(true);
                    return;
                }
                default -> {
                }
            }
        }
        Location to = event.getTo();
        if (!isMine(to.getWorld())) {
            return;
        }
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            event.setCancelled(true);
            player.sendMessage(Component.text("坑道へは /coal mine で入ってください。", NamedTextColor.RED));
        } else if (!SectionBounds.inOpenArea(session.section, session.renderedMeters, to.getX(), to.getZ())) {
            event.setTo(renderer.arrival(session.section));
        }
    }

    private void cancelInMine(World world, Cancellable event) {
        if (isMine(world)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEntity(PlayerBucketEntityEvent event) {
        cancelInMine(event.getEntity().getWorld(), event);
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
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onForm(BlockFormEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event) {
        cancelInMine(event.getBlock().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        cancelInMine(event.getEntity().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        cancelInMine(event.getEntity().getWorld(), event);
    }

    /** トロッコ・ボート・防具立て・エンドクリスタルなどを置かせない。 */
    @EventHandler(ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        cancelInMine(event.getEntity().getWorld(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        cancelInMine(event.getVehicle().getWorld(), event);
    }

    /** スポーンエッグ・ディスペンサー・卵などによるものを含め、坑内では生き物を一切出さない。 */
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        cancelInMine(event.getEntity().getWorld(), event);
    }

    /** 矢・雪玉・ロケット花火などを撃たせない。 */
    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        cancelInMine(event.getEntity().getWorld(), event);
    }

    /** 防具立ての操作を含め、エンティティへの右クリックを止める。 */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        cancelInMine(event.getRightClicked().getWorld(), event);
    }

    /** 坑道にアイテムを散らかさない。 */
    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        cancelInMine(event.getPlayer().getWorld(), event);
    }
}
