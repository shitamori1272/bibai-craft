package io.github.shitamori1272.coalclicker.offline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.dig.DigEngine;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OfflineProgressTest {

    private static final long NOW = 1_764_000_000L;

    /** 発破 1 個 → 掘進力 D = 1（0.1m/秒）、階層 0 で毎秒 10 個。 */
    private static PlayerData digger(long lastSeen) {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setToolCount("blasting", 1);
        data.setLastSeen(lastSeen);
        return data;
    }

    // ---- 経過時間 ----

    @Test
    void 新規プレイヤーには付与しない() {
        assertEquals(0, OfflineProgress.grantSeconds(0, NOW));
        PlayerData data = digger(0);
        assertTrue(OfflineProgress.grant(data, new ServerData(), NOW).isEmpty());
        assertTrue(data.coal().isZero());
        assertEquals(NOW, data.lastSeen());
    }

    @Test
    void 時計が戻って負になっても付与しない() {
        assertEquals(0, OfflineProgress.grantSeconds(NOW + 3600, NOW));
        PlayerData data = digger(NOW + 3600);
        assertTrue(OfflineProgress.grant(data, new ServerData(), NOW).isEmpty());
        assertTrue(data.coal().isZero());
        assertEquals(NOW, data.lastSeen());
    }

    @Test
    void 八時間を超えたら八時間で切る() {
        assertEquals(OfflineProgress.MAX_SECONDS, OfflineProgress.grantSeconds(NOW - 100_000, NOW));
        assertEquals(8 * 3600, OfflineProgress.grantSeconds(NOW - 8 * 3600, NOW));
        assertTrue(OfflineProgress.capped(NOW - 100_000, NOW));
        assertFalse(OfflineProgress.capped(NOW - 8 * 3600, NOW));

        PlayerData data = digger(NOW - 100_000);
        Optional<String> message = OfflineProgress.grant(data, new ServerData(), NOW);
        PlayerData expected = digger(0);
        DigEngine.autoTick(expected, new ServerData(), 8 * 3600);
        assertEquals(expected.coal(), data.coal());
        assertEquals(expected.mine().level(), data.mine().level());
        assertTrue(message.orElseThrow().endsWith("（留守の分は 8 時間まで）"));
    }

    @Test
    void 五秒未満は無視する() {
        assertEquals(0, OfflineProgress.grantSeconds(NOW - 4, NOW));
        assertEquals(5, OfflineProgress.grantSeconds(NOW - 5, NOW));
        PlayerData data = digger(NOW - 4);
        assertTrue(OfflineProgress.grant(data, new ServerData(), NOW).isEmpty());
        assertTrue(data.coal().isZero());
        assertEquals(NOW, data.lastSeen());
    }

    // ---- 反映と通知 ----

    @Test
    void 階層の途中までなら掘った長さと石炭だけ知らせる() {
        PlayerData data = digger(NOW - 600);
        ServerData server = new ServerData();
        Optional<String> message = OfflineProgress.grant(data, server, NOW);
        // 600 秒 × 0.1m = 60m、毎秒 10 個 × 600 秒 = 6,000 個
        assertEquals(Optional.of("留守の間に 60m 掘り進み、6,000個の石炭を掘りました。"), message);
        assertEquals(6000, data.coal().toDouble(), 1e-6);
        assertEquals(6000, server.serverTotalMined().toDouble(), 1e-6);
        assertEquals(0, data.mine().level());
        assertEquals(NOW, data.lastSeen());
    }

    @Test
    void 階層をまたいだら着いた坑道も知らせる() {
        PlayerData data = digger(NOW - 3600);
        String message = OfflineProgress.grant(data, new ServerData(), NOW).orElseThrow();
        // 64 + 80 + 96 + 112 = 352m で 3,520 秒、残り 80 秒で 8m → 第5坑道
        assertEquals(4, data.mine().level());
        assertTrue(message.startsWith("留守の間に 360m 掘り進み、"), message);
        assertTrue(message.endsWith("第5坑道まで進みました。"), message);
    }

    @Test
    void 掘進力が0なら何も起きず通知もしない() {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setLastSeen(NOW - 3600);
        assertTrue(OfflineProgress.grant(data, new ServerData(), NOW).isEmpty());
        assertTrue(data.coal().isZero());
        assertEquals(NOW, data.lastSeen());
    }

    @Test
    void 続けて呼んでも二重には付与しない() {
        PlayerData data = digger(NOW - 600);
        ServerData server = new ServerData();
        OfflineProgress.grant(data, server, NOW);
        assertTrue(OfflineProgress.grant(data, server, NOW + 1).isEmpty());
        assertEquals(6000, data.coal().toDouble(), 1e-6);
    }
}
