import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.Set;

class World {
    // Run from server-data so the server keeps its existing JAR, settings and saves.
    static final Path data = Path.of("").toAbsolutePath();
    static final Path template = data.resolve("../maps/bibai").normalize();
    static final Set<String> personal = Set.of("players", "playerdata", "advancements", "stats",
            "session.lock", "level.dat_old", ".DS_Store");
    // Paper stores plugin worlds inside the main world; the CoalClicker mine is regenerated, never published.
    static final Set<Path> generated = Set.of(Path.of("dimensions", "coalclicker"));

    static void copyWorld(Path from, Path to) throws IOException {
        if (!Files.isRegularFile(from.resolve("level.dat")))
            throw new IOException("level.dat を含むワールドがありません: " + from);
        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = from.relativize(dir);
                if (!dir.equals(from) && (personal.contains(relative.toString()) || generated.contains(relative)))
                    return FileVisitResult.SKIP_SUBTREE;
                Files.createDirectories(to.resolve(from.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isRegularFile()) throw new IOException("通常ファイルではありません: " + file);
                if (!personal.contains(file.getFileName().toString()))
                    Files.copy(file, to.resolve(from.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // Each top-level directory is a complete, independently owned data pack.
    static void installPreviewPacks(Path world) throws IOException {
        Path packs = data.resolve("../world-overlays").normalize();
        Path target = world.resolve("datapacks");
        Files.createDirectories(target);
        try (var entries = Files.newDirectoryStream(packs, Files::isDirectory)) {
            for (Path pack : entries) {
                if (!Files.isRegularFile(pack.resolve("pack.mcmeta")))
                    throw new IOException("pack.mcmeta がありません: " + pack);
                Path destination = target.resolve(pack.getFileName());
                // Fail on name collisions with packs supplied by the base map.
                Files.createDirectory(destination);
                try (var paths = Files.walk(pack)) {
                    for (var it = paths.iterator(); it.hasNext();) {
                        Path source = it.next();
                        if (source.equals(pack)) continue;
                        if (Files.isSymbolicLink(source))
                            throw new IOException("データパックにシンボリックリンクがあります: " + source);
                        Files.copy(source, destination.resolve(pack.relativize(source)));
                    }
                }
            }
        }
    }

    static void removeTree(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult postVisitDirectory(Path path, IOException error) throws IOException {
                if (error != null) throw error;
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void publish(Path local) throws IOException {
        if (!Files.isRegularFile(local.resolve("level.dat")))
            throw new IOException("制作ワールドがありません: " + local);
        // Minecraft holds an exclusive OS lock on this file while the world is open.
        try (var channel = FileChannel.open(local.resolve("session.lock"),
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var lock = channel.tryLock()) {
            if (lock == null) throw new IOException("制作サーバーで stop を実行し、終了後に再実行してください。");
            Path staging = Files.createTempDirectory(data, ".publish-");
            Path backup = staging.resolve("previous");
            try {
                copyWorld(local, staging.resolve("new"));
                Files.createDirectories(template.getParent());
                if (Files.exists(template)) Files.move(template, backup, StandardCopyOption.ATOMIC_MOVE);
                try {
                    Files.move(staging.resolve("new"), template, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    if (Files.exists(backup)) Files.move(backup, template, StandardCopyOption.ATOMIC_MOVE);
                    throw e;
                }
            } finally {
                if (Files.exists(template) || !Files.exists(backup)) removeTree(staging);
                else System.err.println("旧原本の退避先: " + backup);
            }
        }
        System.out.println("maps/bibai/ を更新しました。git status で確認してコミットしてください。");
    }

    static void run(String mode) throws Exception {
        if (!Set.of("local", "preview", "publish").contains(mode))
            throw new IllegalArgumentException("使い方: local | preview | publish");
        Properties settings = new Properties();
        try (var input = Files.newInputStream(data.resolve("../server/server.properties"))) {
            settings.load(input);
        }
        Path local = data.resolve(settings.getProperty("level-name", "world")).normalize();
        if (!data.equals(local.getParent())) throw new IOException("level-name は server-data 内のフォルダ名にしてください。");
        Path preview = null;
        try {
            // Serialize copying and template swaps across commands from this checkout.
            try (var channel = FileChannel.open(data.resolve(".maps.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 var lock = channel.lock()) {
                if (mode.equals("publish")) {
                    publish(local);
                    return;
                }
                if (mode.equals("preview")) {
                    preview = Files.createTempDirectory(data, "preview-");
                    copyWorld(template, preview);
                    installPreviewPacks(preview);
                } else if (!Files.exists(local)) {
                    Path staging = Files.createTempDirectory(data, ".seed-");
                    try {
                        copyWorld(template, staging);
                        Files.move(staging, local, StandardCopyOption.ATOMIC_MOVE);
                    } finally {
                        if (Files.exists(staging)) removeTree(staging);
                    }
                }
            }
            Path world = preview == null ? local : preview;
            System.out.println("使用ワールド: " + world.getFileName());
            String java = ProcessHandle.current().info().command().orElseThrow();
            var server = new ProcessBuilder(java, "-Xms512M", "-Xmx2G", "-jar",
                    "minecraft_server.26.3.jar", "--world", world.getFileName().toString(), "nogui")
                    .inheritIO().start();
            int status = server.waitFor();
            if (status != 0) throw new IOException("サーバーの終了コード: " + status);
        } finally {
            if (preview != null) removeTree(preview);
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length > 1) throw new IllegalArgumentException("使い方: local | preview | publish");
            run(args.length == 0 ? "local" : args[0]);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
