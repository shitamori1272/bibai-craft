package io.github.shitamori1272.coalclicker.offline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.dig.DigEngine;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OfflineProgressTest {

    private static final long NOW = 1_764_000_000L;

    /** 炭車 1 台 → 階層 0 で毎秒 10 個。 */
    private static PlayerData digger(long lastSeen) {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setToolCount("minecart", 1);
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
        assertEquals(expected.totalMined(), data.totalMined());
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
    void 階層が変わらなければ石炭だけ知らせる() {
        PlayerData data = digger(NOW - 60);
        ServerData server = new ServerData();
        Optional<String> message = OfflineProgress.grant(data, server, NOW);
        // 毎秒 10 個 × 60 秒 = 600 個（階層 0 のまま）
        assertEquals(Optional.of("留守の間に 600個の石炭を掘りました。"), message);
        assertEquals(600, data.coal().toDouble(), 1e-6);
        assertEquals(600, server.serverTotalMined().toDouble(), 1e-6);
        assertEquals(0, Formulas.levelOf(data.totalMined()));
        assertEquals(NOW, data.lastSeen());
    }

    @Test
    void 階層をまたいだら途中から深さ倍率が上がり進んだ階層も知らせる() {
        PlayerData data = digger(NOW - 3600);
        String message = OfflineProgress.grant(data, new ServerData(), NOW).orElseThrow();
        // 階層ごとに区切って足す：毎秒 10 × 1.1^L で、境目 6,400・19,200 を越えていく
        double expected = simulate(10, 3600);
        assertEquals(1.0, data.totalMined().toDouble() / expected, 1e-4);
        // ログアウトしたときの階層のまま（36,000 個）より多い
        assertTrue(data.totalMined().toDouble() > 36_000);
        long level = Formulas.levelOf(data.totalMined());
        assertEquals(2, level);
        assertEquals("留守の間に " + JapaneseFormat.format(data.coal()) + "個の石炭を掘りました。第3坑道まで進みました（2階層）。",
                message);
    }

    @Test
    void 留守の分は一秒ずつのオンラインの分と同じ() {
        PlayerData offline = digger(NOW - 5000);
        OfflineProgress.grant(offline, new ServerData(), NOW);
        PlayerData online = digger(0);
        for (int i = 0; i < 5000; i++) {
            DigEngine.autoTick(online, new ServerData(), 1);
        }
        assertEquals(1.0, offline.totalMined().toDouble() / online.totalMined().toDouble(), 1e-9);
    }

    /** 小刻み（0.01 秒）に、その時点の階層の毎秒の石炭を足していく。 */
    private static double simulate(double base, double seconds) {
        double total = 0;
        double dt = 0.01;
        for (long i = 0; i < Math.round(seconds / dt); i++) {
            total += base * Math.pow(1.1, Formulas.levelOf(BigNum.of(total))) * dt;
        }
        return total;
    }

    @Test
    void 道具がなければ何も起きず通知もしない() {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setLastSeen(NOW - 3600);
        assertTrue(OfflineProgress.grant(data, new ServerData(), NOW).isEmpty());
        assertTrue(data.coal().isZero());
        assertEquals(NOW, data.lastSeen());
    }

    @Test
    void 続けて呼んでも二重には付与しない() {
        PlayerData data = digger(NOW - 60);
        ServerData server = new ServerData();
        OfflineProgress.grant(data, server, NOW);
        assertTrue(OfflineProgress.grant(data, server, NOW + 1).isEmpty());
        assertEquals(600, data.coal().toDouble(), 1e-6);
    }
}
