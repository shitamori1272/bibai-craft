package io.github.shitamori1272.coalclicker.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * オンライン中のプレイヤーのデータをメモリ上に持ち、決まったタイミングで保存する係。
 *
 * <ul>
 *   <li>ログイン時に読み込み、ログアウト時に保存してメモリから外す（保存に失敗したら外さず、自動保存でやり直す）
 *   <li>60 秒ごとに自動保存し、サーバー停止時（{@link #saveAll()}）にすべて保存する
 * </ul>
 *
 * <p>ファイルは小さいので、読み書きはすべてメインスレッドで同期的に行う。ファイルの扱いは {@link DataRepository} を参照。
 */
public final class DataStore implements Listener {

    private static final long AUTOSAVE_TICKS = 60 * 20L;

    private final JavaPlugin plugin;
    private final DataRepository repository;
    /** オンライン中のプレイヤーと、ログアウト時の保存に失敗してやり直し待ちのプレイヤー。 */
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    /** ログアウト済みで、保存できたらメモリから外すプレイヤー。 */
    private final Set<UUID> pendingEvict = new HashSet<>();
    private ServerData server = new ServerData();
    private BukkitTask autosaveTask;

    public DataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.repository = new DataRepository(plugin.getDataFolder().toPath(), new YamlFormat(), plugin.getLogger());
    }

    /** データを読み込み、イベントと自動保存を登録する。onEnable から呼ぶ。 */
    public void start() {
        repository.init();
        server = repository.loadServer();
        // /reload などで、すでにログインしているプレイヤーがいる場合
        for (Player player : Bukkit.getOnlinePlayers()) {
            get(player);
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        autosaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::flush, AUTOSAVE_TICKS, AUTOSAVE_TICKS);
    }

    /** 自動保存を止め、すべてのデータを保存する。onDisable から呼ぶ。 */
    public void saveAll() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        flush();
        if (!pendingEvict.isEmpty()) {
            plugin.getLogger().severe("保存できなかったプレイヤーデータがあります: " + pendingEvict);
        }
    }

    /** オンライン中のプレイヤーのデータ。まだ読み込んでいなければ読み込む。 */
    public PlayerData get(Player player) {
        UUID uuid = player.getUniqueId();
        pendingEvict.remove(uuid);
        return cache.computeIfAbsent(uuid, repository::loadPlayer);
    }

    /** オンライン中のプレイヤーのデータ。オフラインのプレイヤーには {@link #loadOffline(UUID)} を使う。 */
    public PlayerData get(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new IllegalStateException("オンラインではないプレイヤーです: " + uuid);
        }
        return get(player);
    }

    /**
     * 任意のプレイヤーのデータの写しを読む（ランキングなど用）。メモリ上にあればその写し、なければファイルから読む。
     * 写しを変えても保存データには影響しない。保存データがなければ空。
     */
    public Optional<PlayerData> loadOffline(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            return Optional.of(data.copy());
        }
        return repository.readPlayer(uuid);
    }

    /** 保存データがあるすべてのプレイヤーの UUID（メモリ上にあってまだ保存していない人も含む）。 */
    public List<UUID> savedPlayerIds() {
        Set<UUID> ids = new LinkedHashSet<>(cache.keySet());
        ids.addAll(repository.savedPlayerIds());
        return List.copyOf(ids);
    }

    /** サーバー全体のデータ。 */
    public ServerData server() {
        return server;
    }

    // ほかの機能のリスナーより先に読み込んでおく
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        get(event.getPlayer());
    }

    // ほかの機能のリスナーがデータを更新し終えてから保存する
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return;
        }
        data.setLastSeen(Instant.now().getEpochSecond());
        if (repository.savePlayer(data)) {
            cache.remove(uuid);
        } else {
            pendingEvict.add(uuid);
            plugin.getLogger().warning("ログアウト時の保存に失敗したため、メモリに残して自動保存でやり直します: " + uuid);
        }
    }

    /** 変更があるデータを保存し、ログアウト済みで保存できたものをメモリから外す。 */
    private void flush() {
        long now = Instant.now().getEpochSecond();
        Iterator<Map.Entry<UUID, PlayerData>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PlayerData> entry = it.next();
            PlayerData data = entry.getValue();
            boolean loggedOut = pendingEvict.contains(entry.getKey());
            if (!loggedOut) {
                // 落ちたときにログアウト中の時間を多く数えすぎないよう、オンライン中も最後にいた時刻を進める
                data.setLastSeen(now);
            }
            if (data.isDirty() && !repository.savePlayer(data)) {
                continue;
            }
            if (loggedOut) {
                it.remove();
                pendingEvict.remove(entry.getKey());
            }
        }
        if (server.isDirty()) {
            repository.saveServer(server);
        }
    }

    /** Bukkit の YamlConfiguration を使った読み書き。 */
    private static final class YamlFormat implements DataRepository.Format {

        @Override
        public Map<String, Object> parse(String text) throws Exception {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(text);
            return toPlainMap(yaml);
        }

        @Override
        public String dump(Map<String, Object> content) {
            YamlConfiguration yaml = new YamlConfiguration();
            content.forEach(yaml::set);
            return yaml.saveToString();
        }

        /** 入れ子の ConfigurationSection も含めて素の Map にする。 */
        private static Map<String, Object> toPlainMap(ConfigurationSection section) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String key : section.getKeys(false)) {
                Object value = section.get(key);
                map.put(key, value instanceof ConfigurationSection child ? toPlainMap(child) : value);
            }
            return map;
        }
    }
}
