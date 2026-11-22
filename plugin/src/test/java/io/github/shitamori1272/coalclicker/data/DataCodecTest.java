package io.github.shitamori1272.coalclicker.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertEquals(0, data.getToolCount("pickaxe"));
        assertTrue(data.explained().isEmpty());
        assertEquals(MineState.NO_SECTION, data.mine().section());
        assertFalse(data.mine().hasSection());
        assertEquals(0, data.mine().level());
        assertEquals(0.0, data.mine().progress());
        assertEquals(0, data.mine().maxReachedLevel());
        assertFalse(data.isDirty());
    }

    @Test
    void プレイヤーデータを往復できる() {
        PlayerData data = new PlayerData(ID);
        data.setCoal(BigNum.parse("1.2345e3"));
        data.setTotalMined(BigNum.parse("5.6789e4000"));
        data.setToolCount("pickaxe", 3);
        data.addTool("lamp", 10);
        data.addTool("lamp", 1);
        data.markExplained("pickaxe");
        data.markExplained("lamp");
        data.mine().setSection(2);
        data.mine().setLevel(9_000_000_000L);
        data.mine().setProgress(0.584);
        data.mine().setMaxReachedLevel(1);
        data.setLastSeen(1764000000L);

        Map<String, Object> map = DataCodec.encodePlayer(data);
        assertEquals("1.2345e3", map.get("coal"));
        assertEquals(Map.of("pickaxe", 3L, "lamp", 11L), map.get("tools"));
        assertEquals(List.of("pickaxe", "lamp"), map.get("explained"));

        PlayerData back = DataCodec.decodePlayer(ID, map);
        assertEquals(data.coal(), back.coal());
        assertEquals(data.totalMined(), back.totalMined());
        assertEquals(data.tools(), back.tools());
        assertEquals(data.explained(), back.explained());
        assertEquals(2, back.mine().section());
        assertEquals(9_000_000_000L, back.mine().level());
        assertEquals(0.584, back.mine().progress());
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
        map.put("tools", Map.of("pickaxe", 3, "lamp", 10));
        map.put("explained", List.of("pickaxe"));
        map.put("mine", mine);
        map.put("lastSeen", 1764000000);

        PlayerData data = DataCodec.decodePlayer(ID, map);
        assertEquals(BigNum.of(1234), data.coal());
        assertEquals(BigNum.of(56789), data.totalMined());
        assertEquals(3, data.getToolCount("pickaxe"));
        assertEquals(10, data.getToolCount("lamp"));
        assertEquals(Set.of("pickaxe"), data.explained());
        assertEquals(2, data.mine().section());
        assertEquals(1, data.mine().level());
        assertEquals(1, data.mine().maxReachedLevel());
        assertEquals(1764000000L, data.lastSeen());
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
        data.mine().setProgress(0.5);
        assertTrue(data.isDirty());
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
        data.addTool("drill", 2);
        data.setToolCount("drill", 0);
        assertFalse(data.tools().containsKey("drill"));
        assertEquals(0, data.getToolCount("drill"));
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
}
