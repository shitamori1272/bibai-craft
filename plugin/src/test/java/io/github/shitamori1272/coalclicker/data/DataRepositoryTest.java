package io.github.shitamori1272.coalclicker.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataRepositoryTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    /** テスト用の形式。Map を覚えておき、ファイルにはその番号だけを書く。知らない中身は読めない（壊れている）とみなす。 */
    private static final class FakeFormat implements DataRepository.Format {
        private final Map<String, Map<String, Object>> stored = new HashMap<>();

        @Override
        public Map<String, Object> parse(String text) {
            Map<String, Object> map = stored.get(text);
            if (map == null) {
                throw new IllegalStateException("読めません: " + text);
            }
            return map;
        }

        @Override
        public String dump(Map<String, Object> content) {
            String token = "#" + stored.size();
            stored.put(token, new LinkedHashMap<>(content));
            return token;
        }
    }

    @TempDir
    Path dir;

    private final FakeFormat format = new FakeFormat();

    private DataRepository repository() {
        DataRepository repo = new DataRepository(dir, format, Logger.getAnonymousLogger());
        repo.init();
        return repo;
    }

    private List<Path> brokenFiles() throws IOException {
        try (Stream<Path> files = Files.list(dir.resolve("players"))) {
            return files.filter(p -> p.getFileName().toString().contains(".broken-")).toList();
        }
    }

    @Test
    void 保存して読み直せる() {
        DataRepository repo = repository();
        PlayerData data = new PlayerData(ID);
        data.setCoal(BigNum.of(123));
        data.addTool("lamp", 4);
        assertTrue(repo.savePlayer(data));
        assertFalse(data.isDirty());
        assertFalse(Files.exists(repo.playerFile(ID).resolveSibling(ID + ".yml.tmp")));

        PlayerData back = repository().loadPlayer(ID);
        assertEquals(BigNum.of(123), back.coal());
        assertEquals(4, back.getToolCount("lamp"));
        assertEquals(List.of(ID), repo.savedPlayerIds());

        ServerData server = new ServerData();
        server.allocateSection();
        assertTrue(repo.saveServer(server));
        assertEquals(1, repository().loadServer().nextSection());
    }

    @Test
    void ファイルがなければ初期値() {
        DataRepository repo = repository();
        assertTrue(repo.readPlayer(ID).isEmpty());
        assertEquals(MineState.NO_SECTION, repo.loadPlayer(ID).mine().section());
        assertEquals(0, repo.loadServer().nextSection());
    }

    @Test
    void 読めないファイルは退避して初期値で始める() throws IOException {
        DataRepository repo = repository();
        Files.writeString(repo.playerFile(ID), "garbage");
        PlayerData data = repo.loadPlayer(ID);
        assertTrue(data.coal().isZero());
        assertFalse(Files.exists(repo.playerFile(ID)));
        assertEquals(1, brokenFiles().size());
        assertEquals("garbage", Files.readString(brokenFiles().get(0)));
        // 退避できたので、その後は普通に保存できる
        assertFalse(repo.isPlayerBlocked(ID));
        assertTrue(repo.savePlayer(data));
    }

    @Test
    void 値が不正なファイルも退避する() throws IOException {
        DataRepository repo = repository();
        Files.writeString(repo.playerFile(ID), format.dump(Map.of("coal", "nope")));
        assertTrue(repo.readPlayer(ID).isEmpty());
        assertEquals(1, brokenFiles().size());

        Files.writeString(repo.serverFile(), format.dump(Map.of("nextSection", 4294967296L)));
        assertEquals(0, repo.loadServer().nextSection());
        assertFalse(Files.exists(repo.serverFile()));
    }

    @Test
    void 退避できなければ元のファイルを上書きしない() throws IOException {
        DataRepository repo = repository();
        Path playersDir = dir.resolve("players");
        Path file = repo.playerFile(ID);
        Files.writeString(file, "precious but unreadable");
        assumeTrue(Files.getFileAttributeView(playersDir, java.nio.file.attribute.PosixFileAttributeView.class) != null);
        // フォルダを書き込み禁止にして、退避（名前の変更）を失敗させる
        Files.setPosixFilePermissions(playersDir, PosixFilePermissions.fromString("r-xr-xr-x"));
        PlayerData data;
        try {
            data = repo.loadPlayer(ID);
        } finally {
            Files.setPosixFilePermissions(playersDir, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        assertTrue(data.coal().isZero());
        assertTrue(repo.isPlayerBlocked(ID));
        // 書き込めるようになっても、このファイルには保存しない
        data.setCoal(BigNum.of(1));
        assertFalse(repo.savePlayer(data));
        assertTrue(data.isDirty());
        assertEquals("precious but unreadable", Files.readString(file));
    }

    @Test
    void 保存に失敗したら_dirty_のまま() throws IOException {
        DataRepository repo = repository();
        Path playersDir = dir.resolve("players");
        assumeTrue(Files.getFileAttributeView(playersDir, java.nio.file.attribute.PosixFileAttributeView.class) != null);
        PlayerData data = new PlayerData(ID);
        data.setCoal(BigNum.of(5));
        Files.setPosixFilePermissions(playersDir, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            assertFalse(repo.savePlayer(data));
        } finally {
            Files.setPosixFilePermissions(playersDir, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        assertTrue(data.isDirty());
        assertTrue(repo.savePlayer(data));
        assertEquals(BigNum.of(5), repository().loadPlayer(ID).coal());
    }

    private void savePlayerWithSection(DataRepository repo, UUID uuid, int section) {
        PlayerData data = new PlayerData(uuid);
        data.mine().setSection(section);
        assertTrue(repo.savePlayer(data));
    }

    @Test
    void mine_ymlがなくても区画を二重に割り当てない() {
        DataRepository repo = repository();
        savePlayerWithSection(repo, ID, 3);
        savePlayerWithSection(repo, UUID.randomUUID(), 7);
        savePlayerWithSection(repo, UUID.randomUUID(), MineState.NO_SECTION);
        ServerData server = repo.loadServer();
        assertEquals(8, server.nextSection());
        assertTrue(server.isDirty());
        assertEquals(8, server.allocateSection());
    }

    @Test
    void 壊れた_mine_ymlでも区画を二重に割り当てない() throws IOException {
        DataRepository repo = repository();
        savePlayerWithSection(repo, ID, 4);
        Files.writeString(repo.serverFile(), "garbage");
        assertEquals(5, repo.loadServer().nextSection());
    }

    @Test
    void nextSectionが古くても使用中の区画を飛ばす() {
        DataRepository repo = repository();
        ServerData saved = new ServerData();
        saved.setNextSection(2);
        assertTrue(repo.saveServer(saved));
        savePlayerWithSection(repo, ID, 5);
        ServerData server = repo.loadServer();
        assertEquals(6, server.allocateSection());
    }

    @Test
    void allocateSectionは使用中の区画を返さない() {
        ServerData server = new ServerData();
        server.setNextSection(10);
        server.reserveSection(3);
        assertEquals(10, server.nextSection());
        server.setNextSection(3);
        server.reserveSection(4);
        assertEquals(5, server.allocateSection());
        server.setNextSection(0);
        server.reserveSection(0);
        server.reserveSection(1);
        assertEquals(2, server.allocateSection());
        // 手で戻されても、使用中（0〜5）は返さない
        server.setNextSection(0);
        assertEquals(6, server.allocateSection());
    }

    @Test
    void 書き込みは置き換えになる() throws IOException {
        Path file = dir.resolve("sub").resolve("a.yml");
        DataRepository.writeAtomically(file, "one");
        DataRepository.writeAtomically(file, "two");
        assertEquals("two", Files.readString(file));
        assertFalse(Files.exists(dir.resolve("sub").resolve("a.yml.tmp")));
    }
}
