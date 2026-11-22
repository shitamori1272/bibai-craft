package io.github.shitamori1272.coalclicker.dig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DigEngineTest {

    private static void assertClose(double expected, BigNum actual) {
        if (expected == 0) {
            assertTrue(actual.isZero(), () -> "0 のはず: " + actual);
            return;
        }
        assertEquals(1.0, actual.divide(BigNum.of(expected)).toDouble(), 1e-9,
                () -> "期待 " + expected + " / 実際 " + actual);
    }

    private static PlayerData player(long level, double meters) {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.mine().setLevel(level);
        data.mine().setProgress(meters / (64 + 16 * level));
        return data;
    }

    // ---- 自動の掘進 ----

    @Test
    void 掘進力が0なら進まず石炭も増えない() {
        PlayerData data = player(0, 10);
        ServerData server = new ServerData();
        DigEngine.Change c = DigEngine.autoTick(data, server, 1);
        assertFalse(c.moved());
        assertTrue(c.coal().isZero());
        assertEquals(10, c.newMeters(), 1e-9);
        assertTrue(data.coal().isZero());
        assertTrue(server.serverTotalMined().isZero());
    }

    @Test
    void 発破100個なら1秒で1m進み石炭100個() {
        PlayerData data = player(0, 0);
        data.setToolCount(Tool.BLASTING.id(), 100);
        ServerData server = new ServerData();
        DigEngine.Change c = DigEngine.autoTick(data, server, 1);
        assertTrue(c.moved());
        assertEquals(0, c.oldMeters());
        assertEquals(1, c.newMeters(), 1e-9);
        assertEquals(1, c.meters(), 1e-9);
        assertEquals(1.0 / 64, data.mine().progress(), 1e-12);
        assertClose(100, c.coal());
        assertClose(100, data.coal());
        assertClose(100, data.totalMined());
        assertClose(100, server.serverTotalMined());
    }

    @Test
    void 発破1つなら10秒で1m() {
        PlayerData data = player(0, 0);
        data.setToolCount(Tool.BLASTING.id(), 1);
        ServerData server = new ServerData();
        for (int i = 0; i < 10; i++) {
            DigEngine.autoTick(data, server, 1);
        }
        assertEquals(1, DigEngine.meters(data.mine()), 1e-9);
        assertClose(100, data.coal());
    }

    @Test
    void 深い階層と採炭力で1mあたりの石炭が増える() {
        // 第 4 坑道（深さ倍率 1.1^3）・E = 100 + 10 × 10 = 200・速度 1m/秒
        PlayerData data = player(3, 5);
        data.setToolCount(Tool.BLASTING.id(), 100);
        data.setToolCount(Tool.LAMP.id(), 10);
        DigEngine.Change c = DigEngine.autoTick(data, new ServerData(), 1);
        assertEquals(6, c.newMeters(), 1e-9);
        assertClose(200 * 1.331, c.coal());
    }

    @Test
    void 一階層は最短10秒() {
        // D = 10^6 なら速度 100m/秒だが、第 1 坑道（64m）は 10 秒かけるので 6.4m/秒
        PlayerData data = player(0, 0);
        data.setToolCount(Tool.BLASTING.id(), 1_000_000);
        DigEngine.Change c = DigEngine.autoTick(data, new ServerData(), 1);
        assertEquals(6.4, c.newMeters(), 1e-9);
        assertClose(640, c.coal());
    }

    @Test
    void 自動で掘り切ったら次の階層へ進み超えた分は捨てる() {
        PlayerData data = player(0, 63.5);
        data.setToolCount(Tool.BLASTING.id(), 100);
        ServerData server = new ServerData();
        DigEngine.Change c = DigEngine.autoTick(data, server, 1);
        assertTrue(c.completedOldLevel());
        assertEquals(1, c.levelsCompleted());
        assertEquals(0, c.oldLevel());
        assertEquals(63.5, c.oldMeters(), 1e-9);
        assertEquals(64, c.oldLevelLength());
        assertEquals(1, c.newLevel());
        assertEquals(0, c.newMeters());
        assertEquals(1, data.mine().level());
        assertEquals(0, data.mine().progress());
        // 0.5m 分だけもらえ、残りの 0.5 秒は捨てる
        assertClose(50, c.coal());
        assertClose(50, server.serverTotalMined());
    }

    @Test
    void まとめて進めると複数の階層を掘り切り深さ倍率が反映される() {
        // 削岩機をとても多く持つと 1 階層は最短の 10 秒。100 秒で第 1〜10 坑道を掘り切る
        PlayerData data = player(0, 0);
        data.setToolCount(Tool.DRILL.id(), Long.MAX_VALUE);
        ServerData server = new ServerData();
        DigEngine.Change c = DigEngine.autoTick(data, server, 100);
        assertEquals(10, c.levelsCompleted());
        assertEquals(10, c.newLevel());
        assertEquals(0, c.newMeters());
        assertEquals(10, data.mine().level());
        double expected = 0;
        for (int level = 0; level < 10; level++) {
            expected += 100 * (64 + 16 * level) * Math.pow(1.1, level);
        }
        assertClose(expected, c.coal());
        assertClose(expected, server.serverTotalMined());
        // 到達ボーナスの最深は自動の掘進では変わらない（着いたときに付く）
        assertEquals(0, data.mine().maxReachedLevel());
    }

    @Test
    void 石炭は今の値と累計に足される() {
        PlayerData data = player(0, 0);
        data.setCoal(BigNum.of(30));
        data.setTotalMined(BigNum.of(1_000));
        data.setToolCount(Tool.BLASTING.id(), 100);
        ServerData server = new ServerData();
        server.setServerTotalMined(BigNum.of(5_000));
        DigEngine.autoTick(data, server, 1);
        assertClose(130, data.coal());
        assertClose(1_100, data.totalMined());
        assertClose(5_100, server.serverTotalMined());
    }

    // ---- 手掘り ----

    @Test
    void 手掘りは初期状態で1m進み石炭100個() {
        PlayerData data = player(0, 0);
        ServerData server = new ServerData();
        DigEngine.Change c = DigEngine.manualMeter(data, server);
        assertEquals(1, c.newMeters(), 1e-9);
        assertEquals(1.0 / 64, data.mine().progress(), 1e-12);
        assertClose(100, c.coal());
        assertClose(100, data.coal());
        assertClose(100, data.totalMined());
        assertClose(100, server.serverTotalMined());
    }

    @Test
    void 手掘りの石炭はツルハシと深さで増える() {
        // (E 100 + 100 × ツルハシ 2) × 1.1^2
        PlayerData data = player(2, 10.25);
        data.setToolCount(Tool.PICKAXE.id(), 2);
        DigEngine.Change c = DigEngine.manualMeter(data, new ServerData());
        assertEquals(11.25, c.newMeters(), 1e-9);
        assertClose(300 * 1.21, c.coal());
        assertClose(300 * 1.21, DigEngine.manualCoalPerMeter(data, 2));
    }

    @Test
    void 手掘りの最後の1mで次の階層へ進む() {
        PlayerData data = player(0, 63);
        DigEngine.Change c = DigEngine.manualMeter(data, new ServerData());
        assertTrue(c.completedOldLevel());
        assertEquals(1, data.mine().level());
        assertEquals(0, data.mine().progress());
        assertClose(100, c.coal());
    }

    @Test
    void 手掘りで長さを超えた分は捨てる() {
        PlayerData data = player(0, 63.4);
        DigEngine.Change c = DigEngine.manualMeter(data, new ServerData());
        assertEquals(1, c.newLevel());
        assertEquals(0, c.newMeters());
        assertEquals(0.6, c.meters(), 1e-9);
        assertClose(60, c.coal());
    }

    @Test
    void 手掘りと自動の掘進は同じ進み具合を使う() {
        PlayerData data = player(0, 0);
        data.setToolCount(Tool.BLASTING.id(), 100);
        ServerData server = new ServerData();
        DigEngine.autoTick(data, server, 1);
        DigEngine.manualMeter(data, server);
        DigEngine.autoTick(data, server, 1);
        assertEquals(3, DigEngine.meters(data.mine()), 1e-9);
        assertClose(300, data.coal());
    }
}
