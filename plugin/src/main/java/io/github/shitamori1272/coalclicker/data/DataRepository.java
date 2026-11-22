package io.github.shitamori1272.coalclicker.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 保存ファイルの読み書き。YAML の文字列化は {@link Format} に任せるので、Bukkit なしでテストできる。
 *
 * <ul>
 *   <li>書き込みは一時ファイルに書いてから置き換えるので、途中で落ちても元のファイルは壊れない
 *   <li>読めない・値が不正なファイルは {@code *.broken-<エポック秒>} に退避し、初期値で始める
 *   <li>退避できなかった（または読み込み自体に失敗した）ファイルは、元のデータを上書きしないよう保存を止める
 * </ul>
 */
final class DataRepository {

    /** Map と文字列（YAML）の相互変換。 */
    interface Format {
        Map<String, Object> parse(String text) throws Exception;

        String dump(Map<String, Object> content);
    }

    private final Path playersDir;
    private final Path serverFile;
    private final Format format;
    private final Logger logger;
    /** 元のデータを守るため保存しないファイル。 */
    private final Set<Path> blocked = new HashSet<>();

    DataRepository(Path dataDir, Format format, Logger logger) {
        this.playersDir = dataDir.resolve("players");
        this.serverFile = dataDir.resolve("mine.yml");
        this.format = format;
        this.logger = logger;
    }

    void init() {
        try {
            Files.createDirectories(playersDir);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "データフォルダを作れません: " + playersDir, e);
        }
    }

    /** {@code mine.yml} を読む。ないか読めなければ初期値。 */
    ServerData loadServer() {
        return read(serverFile, DataCodec::decodeServer).orElseGet(ServerData::new);
    }

    /** プレイヤーのデータを読む。ないか読めなければ初期値。 */
    PlayerData loadPlayer(UUID uuid) {
        return readPlayer(uuid).orElseGet(() -> new PlayerData(uuid));
    }

    /** プレイヤーのデータを読む。ないか読めなければ空。 */
    Optional<PlayerData> readPlayer(UUID uuid) {
        return read(playerFile(uuid), map -> DataCodec.decodePlayer(uuid, map));
    }

    /** 保存して dirty を消す。成功したら true。 */
    boolean savePlayer(PlayerData data) {
        if (write(playerFile(data.uuid()), DataCodec.encodePlayer(data))) {
            data.clearDirty();
            return true;
        }
        return false;
    }

    /** 保存して dirty を消す。成功したら true。 */
    boolean saveServer(ServerData data) {
        if (write(serverFile, DataCodec.encodeServer(data))) {
            data.clearDirty();
            return true;
        }
        return false;
    }

    /** 元のデータを守るため、このプレイヤーの保存を止めているか。 */
    boolean isPlayerBlocked(UUID uuid) {
        return blocked.contains(playerFile(uuid));
    }

    boolean isServerBlocked() {
        return blocked.contains(serverFile);
    }

    /** 保存ファイルがあるプレイヤーの UUID。 */
    List<UUID> savedPlayerIds() {
        List<UUID> ids = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(playersDir, "*.yml")) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                try {
                    ids.add(UUID.fromString(name.substring(0, name.length() - ".yml".length())));
                } catch (IllegalArgumentException ignored) {
                    // UUID 以外の名前のファイルは無視する
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "プレイヤーデータの一覧を読めません", e);
        }
        return ids;
    }

    Path playerFile(UUID uuid) {
        return playersDir.resolve(uuid + ".yml");
    }

    Path serverFile() {
        return serverFile;
    }

    private <T> Optional<T> read(Path file, Function<Map<String, Object>, T> decoder) {
        if (blocked.contains(file) || !Files.exists(file)) {
            return Optional.empty();
        }
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 中身は壊れていないかもしれないので退避はせず、上書きだけ止める
            block(file, "読み込みに失敗しました", e);
            return Optional.empty();
        }
        try {
            return Optional.of(decoder.apply(format.parse(text)));
        } catch (Exception e) {
            Path backup = file.resolveSibling(file.getFileName() + ".broken-" + Instant.now().getEpochSecond());
            try {
                Files.move(file, backup);
                logger.log(Level.SEVERE, "データを読めないため " + backup.getFileName() + " に退避し、初期値で始めます", e);
            } catch (IOException moveError) {
                moveError.addSuppressed(e);
                block(file, "壊れたデータを退避できませんでした", moveError);
            }
            return Optional.empty();
        }
    }

    private void block(Path file, String reason, Exception e) {
        blocked.add(file);
        logger.log(Level.SEVERE, "!!! " + reason + ": " + file
                + " — 元のデータを守るため、このファイルへの保存を止めます。手動で確認してください。", e);
    }

    private boolean write(Path file, Map<String, Object> content) {
        if (blocked.contains(file)) {
            logger.severe("元のデータを守るため保存しません（要確認）: " + file);
            return false;
        }
        try {
            writeAtomically(file, format.dump(content));
            return true;
        } catch (IOException | RuntimeException e) {
            logger.log(Level.SEVERE, "データを保存できません: " + file, e);
            return false;
        }
    }

    /** 一時ファイルに書いてから置き換える。 */
    static void writeAtomically(Path file, String text) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(tmp, text, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException | AccessDeniedException e) {
                // Windows などで ATOMIC_MOVE と REPLACE_EXISTING を同時に使えない場合は、普通の置き換えでやり直す
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }
}
