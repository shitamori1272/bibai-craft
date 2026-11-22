package io.github.shitamori1272.coalclicker.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
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
 * 保存データの読み書きと、オンライン中のプレイヤーのデータをメモリ上に持つ係。
 *
 * <ul>
 *   <li>ログイン時に {@code players/<UUID>.yml} を読み込み、ログアウト時に保存してメモリから外す
 *   <li>60 秒ごとに自動保存し、サーバー停止時（{@link #saveAll()}）にすべて保存する
 *   <li>書き込みは一時ファイルに書いてから置き換えるので、途中で落ちても元のファイルは壊れない
 * </ul>
 *
 * <p>ファイルは小さいので、読み書きはすべてメインスレッドで同期的に行う。
 */
public final class DataStore implements Listener {

    private static final long AUTOSAVE_TICKS = 60 * 20L;

    private final JavaPlugin plugin;
    private final Path playersDir;
    private final Path serverFile;
    private final Map<UUID, PlayerData> online = new HashMap<>();
    private ServerData server = new ServerData();
    private BukkitTask autosaveTask;

    public DataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        Path dataDir = plugin.getDataFolder().toPath();
        this.playersDir = dataDir.resolve("players");
        this.serverFile = dataDir.resolve("mine.yml");
    }

    /** データを読み込み、イベントと自動保存を登録する。onEnable から呼ぶ。 */
    public void start() {
        try {
            Files.createDirectories(playersDir);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "データフォルダを作れません: " + playersDir, e);
        }
        server = readMap(serverFile).map(DataCodec::decodeServer).orElseGet(ServerData::new);
        // /reload などで、すでにログインしているプレイヤーがいる場合
        for (Player player : Bukkit.getOnlinePlayers()) {
            get(player);
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        autosaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::autosave, AUTOSAVE_TICKS, AUTOSAVE_TICKS);
    }

    /** 自動保存を止め、すべてのデータを保存する。onDisable から呼ぶ。 */
    public void saveAll() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        long now = Instant.now().getEpochSecond();
        for (PlayerData data : online.values()) {
            data.setLastSeen(now);
            savePlayer(data);
        }
        saveServer();
    }

    /** オンライン中のプレイヤーのデータ。まだ読み込んでいなければ読み込む。 */
    public PlayerData get(Player player) {
        return online.computeIfAbsent(player.getUniqueId(), this::loadOrCreate);
    }

    /** オンライン中のプレイヤーのデータ。オフラインのプレイヤーには {@link #loadOffline(UUID)} を使う。 */
    public PlayerData get(UUID uuid) {
        PlayerData data = online.get(uuid);
        if (data != null) {
            return data;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new IllegalStateException("オンラインではないプレイヤーです: " + uuid);
        }
        return get(player);
    }

    /**
     * 任意のプレイヤーのデータを読む（ランキングなど用）。オンラインならメモリ上のもの、そうでなければファイルから読んだ写しを返す。
     * 読み取り専用として扱い、変更しないこと。保存データがなければ空。
     */
    public Optional<PlayerData> loadOffline(UUID uuid) {
        PlayerData data = online.get(uuid);
        if (data != null) {
            return Optional.of(data);
        }
        return readMap(playerFile(uuid)).map(map -> DataCodec.decodePlayer(uuid, map));
    }

    /** 保存データがあるすべてのプレイヤーの UUID（オンライン中でまだ保存していない人も含む）。 */
    public List<UUID> savedPlayerIds() {
        List<UUID> ids = new ArrayList<>(online.keySet());
        try (DirectoryStream<Path> files = Files.newDirectoryStream(playersDir, "*.yml")) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                try {
                    UUID uuid = UUID.fromString(name.substring(0, name.length() - ".yml".length()));
                    if (!online.containsKey(uuid)) {
                        ids.add(uuid);
                    }
                } catch (IllegalArgumentException ignored) {
                    // UUID 以外の名前のファイルは無視する
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "プレイヤーデータの一覧を読めません", e);
        }
        return ids;
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
        PlayerData data = online.remove(event.getPlayer().getUniqueId());
        if (data != null) {
            data.setLastSeen(Instant.now().getEpochSecond());
            savePlayer(data);
        }
    }

    private void autosave() {
        long now = Instant.now().getEpochSecond();
        for (PlayerData data : online.values()) {
            // 落ちたときにログアウト中の時間を多く数えすぎないよう、オンライン中も最後にいた時刻を進める
            data.setLastSeen(now);
            if (data.isDirty()) {
                savePlayer(data);
            }
        }
        if (server.isDirty()) {
            saveServer();
        }
    }

    private PlayerData loadOrCreate(UUID uuid) {
        return readMap(playerFile(uuid))
                .map(map -> DataCodec.decodePlayer(uuid, map))
                .orElseGet(() -> new PlayerData(uuid));
    }

    private void savePlayer(PlayerData data) {
        if (write(playerFile(data.uuid()), DataCodec.encodePlayer(data))) {
            data.clearDirty();
        }
    }

    private void saveServer() {
        if (write(serverFile, DataCodec.encodeServer(server))) {
            server.clearDirty();
        }
    }

    private Path playerFile(UUID uuid) {
        return playersDir.resolve(uuid + ".yml");
    }

    /**
     * YAML ファイルを読む。ファイルがなければ空。壊れていれば別名で退避してから空を返す（初期値で始め直す）。
     */
    private Optional<Map<String, Object>> readMap(Path file) {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
            return Optional.of(toPlainMap(yaml));
        } catch (IOException | InvalidConfigurationException | RuntimeException e) {
            Path backup = file.resolveSibling(file.getFileName() + ".broken-" + Instant.now().getEpochSecond());
            plugin.getLogger().log(Level.SEVERE, "データを読めないため " + backup.getFileName() + " に退避し、初期値で始めます", e);
            try {
                Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveError) {
                plugin.getLogger().log(Level.SEVERE, "退避に失敗しました: " + file, moveError);
            }
            return Optional.empty();
        }
    }

    /** 一時ファイルに書いてから置き換える。成功したら true。 */
    private boolean write(Path file, Map<String, Object> content) {
        YamlConfiguration yaml = new YamlConfiguration();
        content.forEach(yaml::set);
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(tmp, yaml.saveToString(), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "データを保存できません: " + file, e);
            return false;
        }
    }

    /** 読み込んだ YAML を、入れ子の ConfigurationSection も含めて素の Map にする。 */
    private static Map<String, Object> toPlainMap(ConfigurationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            map.put(key, value instanceof ConfigurationSection child ? toPlainMap(child) : value);
        }
        return map;
    }
}
