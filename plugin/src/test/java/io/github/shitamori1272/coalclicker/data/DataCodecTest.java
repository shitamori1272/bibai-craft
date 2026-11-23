package io.github.shitamori1272.coalclicker.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DataCodecTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void 新しいプレイヤーは初期値() {
        PlayerData data = new PlayerData(ID);
        assertTrue(data.coal().isZero());
        assertTrue(data.totalMined().isZero());
        assertTrue(data.tools().isEmpty());
        assertEquals(0, data.getToolCount("lamp"));
        assertEquals(0, data.clicks());
        assertTrue(data.upgrades().isEmpty());
        assertTrue(data.explained().isEmpty());
        assertEquals(MineState.NO_SECTION, data.mine().section());
        assertFalse(data.mine().hasSection());
        assertEquals(0, data.mine().maxReachedLevel());
        assertFalse(data.isDirty());
    }

    @Test
    void プレイヤーデータを往復できる() {
        PlayerData data = new PlayerData(ID);
        data.setCoal(BigNum.parse("1.2345e3"));
        data.setTotalMined(BigNum.parse("5.6789e4000"));
        data.setClicks(320);
        data.setToolCount("minecart", 3);
        data.addTool("lamp", 10);
        data.addTool("lamp", 1);
        data.addUpgrade("lamp_1");
        data.addUpgrade("pickaxe_1");
        data.markExplained("minecart");
        data.markExplained("lamp_1");
        data.mine().setSection(2);
        data.mine().setMaxReachedLevel(1);
        data.setLastSeen(1764000000L);

        Map<String, Object> map = DataCodec.encodePlayer(data);
        assertEquals("1.2345e3", map.get("coal"));
        assertEquals(320L, map.get("clicks"));
        assertEquals(Map.of("minecart", 3L, "lamp", 11L), map.get("tools"));
        assertEquals(List.of("lamp_1", "pickaxe_1"), map.get("upgrades"));
        assertEquals(List.of("minecart", "lamp_1"), map.get("explained"));
        // 階層と進み具合は保存しない（企画書 8 章）
        assertEquals(Map.of("section", 2, "maxReachedLevel", 1L), map.get("mine"));

        PlayerData back = DataCodec.decodePlayer(ID, map);
        assertEquals(data.coal(), back.coal());
        assertEquals(data.totalMined(), back.totalMined());
        assertEquals(320, back.clicks());
        assertEquals(data.tools(), back.tools());
        assertEquals(data.upgrades(), back.upgrades());
        assertEquals(data.explained(), back.explained());
        assertEquals(2, back.mine().section());
        assertEquals(1, back.mine().maxReachedLevel());
        assertEquals(1764000000L, back.lastSeen());
        assertFalse(back.isDirty());
    }

    @Test
    void YAMLで読んだときの型でも読める() {
        // YAML からは数値が Integer・Double で来たり、BigNum が数値のまま来たりする
        Map<String, Object> mine = new LinkedHashMap<>();
        mine.put("section", 2);
        mine.put("level", 1);
        mine.put("progress", 0);
        mine.put("maxReachedLevel", "1");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("coal", 1234);
        map.put("totalMined", "5.6789e4");
        map.put("clicks", "12");
        map.put("tools", Map.of("minecart", 3, "lamp", 10));
        map.put("upgrades", List.of("lamp_1", "trophy_4", "trophy_99", "unknown_1"));
        map.put("explained", List.of("lamp"));
        map.put("mine", mine);
        map.put("lastSeen", 1764000000);

        PlayerData data = DataCodec.decodePlayer(ID, map);
        assertEquals(BigNum.of(1234), data.coal());
        assertEquals(BigNum.of(56789), data.totalMined());
        assertEquals(12, data.clicks());
        assertEquals(3, data.getToolCount("minecart"));
        assertEquals(10, data.getToolCount("lamp"));
        // トロフィーは残し、範囲の外のトロフィーと知らない改良は捨てる
        assertEquals(Set.of("lamp_1", "trophy_4"), data.upgrades());
        assertEquals(Set.of("lamp"), data.explained());
        assertEquals(2, data.mine().section());
        assertEquals(1, data.mine().maxReachedLevel());
        assertEquals(1764000000L, data.lastSeen());
    }

    @Test
    void 古い形式のファイルも読める() {
        // ルール改定前：mine.level・mine.progress があり、道具に pickaxe・drill がある
        Map<String, Object> mine = new LinkedHashMap<>();
        mine.put("section", 3);
        mine.put("level", 12);
        mine.put("progress", 0.75);
        mine.put("maxReachedLevel", 10);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("coal", "1.5e4");
        map.put("totalMined", "2e5");
        map.put("tools", Map.of("pickaxe", 3, "drill", 2, "lamp", 4, "blasting", 1));
        map.put("explained", List.of("pickaxe", "lamp"));
        map.put("mine", mine);
        map.put("lastSeen", 1764000000L);

        PlayerData data = DataCodec.decodePlayer(ID, map);
        assertEquals(BigNum.of(15000), data.coal());
        assertEquals(Map.of("lamp", 4L, "blasting", 1L), data.tools());
        assertEquals(0, data.clicks());
        assertTrue(data.upgrades().isEmpty());
        assertEquals(3, data.mine().section());
        assertEquals(10, data.mine().maxReachedLevel());
        assertFalse(data.isDirty());

        // 書き出すと新しい形になる
        Map<String, Object> out = DataCodec.encodePlayer(data);
        assertEquals(Map.of("section", 3, "maxReachedLevel", 10L), out.get("mine"));
        assertEquals(Map.of("lamp", 4L, "blasting", 1L), out.get("tools"));
    }

    @Test
    void 知らない改良は捨てる() {
        PlayerData data = DataCodec.decodePlayer(ID, Map.of("upgrades", List.of("lamp_1", "nope", "skill_5")));
        assertEquals(Set.of("lamp_1", "skill_5"), data.upgrades());
    }

    @Test
    void 項目がなければ初期値() {
        PlayerData data = DataCodec.decodePlayer(ID, Map.of());
        assertTrue(data.coal().isZero());
        assertEquals(MineState.NO_SECTION, data.mine().section());
        ServerData server = DataCodec.decodeServer(Map.of());
        assertEquals(0, server.nextSection());
        assertTrue(server.serverTotalMined().isZero());
    }

    @Test
    void 値を変えると保存対象になる() {
        PlayerData data = new PlayerData(ID);
        data.mine().setMaxReachedLevel(1);
        assertTrue(data.isDirty());
        data.clearDirty();
        data.incrementClicks();
        assertTrue(data.isDirty());
        data.clearDirty();
        assertTrue(data.addUpgrade("lamp_1"));
        assertTrue(data.isDirty());
        data.clearDirty();
        assertFalse(data.addUpgrade("lamp_1"));
        assertFalse(data.isDirty());
        data.clearDirty();
        data.markExplained("lamp");
        assertTrue(data.isDirty());
        data.clearDirty();
        assertFalse(data.markExplained("lamp"));
        assertFalse(data.isDirty());
    }

    @Test
    void 所持数を0にすると記録から消える() {
        PlayerData data = new PlayerData(ID);
        data.addTool("lamp", 2);
        data.setToolCount("lamp", 0);
        assertFalse(data.tools().containsKey("lamp"));
        assertEquals(0, data.getToolCount("lamp"));
    }

    @Test
    void サーバーデータを往復できる() {
        ServerData server = new ServerData();
        assertEquals(0, server.allocateSection());
        assertEquals(1, server.allocateSection());
        server.addServerTotalMined(BigNum.parse("3e500"));
        assertTrue(server.isDirty());

        Map<String, Object> map = DataCodec.encodeServer(server);
        assertEquals(2, map.get("nextSection"));
        assertEquals(server.serverTotalMined().toString(), map.get("serverTotalMined"));

        ServerData back = DataCodec.decodeServer(map);
        assertEquals(2, back.nextSection());
        assertEquals(server.serverTotalMined(), back.serverTotalMined());
        assertFalse(back.isDirty());
        assertEquals(2, back.allocateSection());
    }

    @Test
    void 不正な値は例外になる() {
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("coal", "nope")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("coal", "-1e3")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("coal", -5)));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("totalMined", "-2.5e100")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodeServer(Map.of("serverTotalMined", "-1")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("tools", Map.of("lamp", 1.5))));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("tools", Map.of("lamp", -1))));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("tools", "lamp")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("explained", "lamp")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("mine", Map.of("section", 4294967296L))));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("mine", Map.of("maxReachedLevel", -1))));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("clicks", -1)));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("clicks", 1.5)));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("upgrades", "lamp_1")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodePlayer(ID, Map.of("lastSeen", "99999999999999999999")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodeServer(Map.of("nextSection", 4294967296L)));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodeServer(Map.of("nextSection", "1.5")));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodeServer(Map.of("nextSection", -1)));
        assertThrows(IllegalArgumentException.class, () -> DataCodec.decodeServer(Map.of("serverTotalMined", Double.NaN)));
    }

    @Test
    void 整数として正確な値は読める() {
        assertEquals(3, DataCodec.decodePlayer(ID, Map.of("tools", Map.of("lamp", 3.0))).getToolCount("lamp"));
        assertEquals(4, DataCodec.decodeServer(Map.of("nextSection", "4")).nextSection());
    }

    @Test
    void 写しは元と独立している() {
        PlayerData data = new PlayerData(ID);
        data.addTool("lamp", 2);
        data.markExplained("lamp");
        data.addUpgrade("lamp_1");
        data.setClicks(7);
        data.mine().setMaxReachedLevel(5);
        PlayerData copy = data.copy();
        assertNotSame(data.mine(), copy.mine());
        assertFalse(copy.isDirty());
        assertEquals(7, copy.clicks());
        assertEquals(Set.of("lamp_1"), copy.upgrades());
        copy.addTool("lamp", 1);
        copy.markExplained("minecart");
        copy.addUpgrade("lamp_2");
        copy.mine().setMaxReachedLevel(6);
        copy.setCoal(BigNum.of(10));
        assertEquals(2, data.getToolCount("lamp"));
        assertFalse(data.isExplained("minecart"));
        assertFalse(data.hasUpgrade("lamp_2"));
        assertEquals(5, data.mine().maxReachedLevel());
        assertTrue(data.coal().isZero());
        assertEquals(5, data.copy().mine().maxReachedLevel());
    }
}
