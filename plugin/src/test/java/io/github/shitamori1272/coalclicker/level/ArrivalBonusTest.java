package io.github.shitamori1272.coalclicker.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ArrivalBonusTest {

    private static void assertClose(double expected, BigNum actual) {
        assertEquals(1.0, actual.divide(BigNum.of(expected)).toDouble(), 1e-9,
                () -> "期待 " + expected + " / 実際 " + actual);
    }

    /** 安全灯 10 個（深さ倍率の前で毎秒 10 個）で、階層 level のはじめにいるプレイヤー。 */
    private static PlayerData player(long level, long maxReached) {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setToolCount(Tool.LAMP.id(), 10);
        data.setTotalMined(Formulas.cumulativeToFinish(level - 1));
        data.mine().setMaxReachedLevel(maxReached);
        return data;
    }

    @Test
    void 第2坑道に着くとその階層の毎秒の60秒分() {
        PlayerData data = player(1, 0);
        BigNum before = data.totalMined();
        ServerData server = new ServerData();
        Optional<ArrivalBonus.Result> result = ArrivalBonus.claim(data, server);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().fromLevel());
        assertEquals(1, result.get().toLevel());
        assertEquals(1, result.get().levels());
        // 10 × 1.1 × 60 ＝ 660
        assertClose(660, result.get().coal());
        assertClose(660, data.coal());
        assertClose(before.toDouble() + 660, data.totalMined());
        assertClose(660, server.serverTotalMined());
        assertEquals(1, data.mine().maxReachedLevel());
    }

    @Test
    void 降りなかった階層の分もまとめてもらえる() {
        PlayerData data = player(4, 1);
        ArrivalBonus.Result result = ArrivalBonus.claim(data, new ServerData()).orElseThrow();
        assertEquals(3, result.levels());
        double expected = 10 * 60 * (Math.pow(1.1, 2) + Math.pow(1.1, 3) + Math.pow(1.1, 4));
        assertClose(expected, result.coal());
        assertEquals(4, data.mine().maxReachedLevel());
    }

    @Test
    void 同じ階層では二重にもらえない() {
        PlayerData data = player(3, 0);
        ServerData server = new ServerData();
        assertTrue(ArrivalBonus.claim(data, server).isPresent());
        BigNum after = data.coal();
        assertTrue(ArrivalBonus.claim(data, server).isEmpty());
        assertEquals(after, data.coal());
        assertEquals(3, data.mine().maxReachedLevel());
    }

    @Test
    void 第1坑道ではもらえない() {
        PlayerData data = player(0, 0);
        ServerData server = new ServerData();
        assertTrue(ArrivalBonus.claim(data, server).isEmpty());
        assertTrue(data.coal().isZero());
        assertTrue(server.serverTotalMined().isZero());
    }

    @Test
    void 最深より浅い階層では何もしない() {
        PlayerData data = player(2, 5);
        assertTrue(ArrivalBonus.claim(data, new ServerData()).isEmpty());
        assertEquals(5, data.mine().maxReachedLevel());
    }

    @Test
    void 道具がなくても手掘り1回分は保証される() {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setTotalMined(BigNum.of(3_000)); // 階層 2
        data.addUpgrade("pickaxe_1");
        ArrivalBonus.Result result = ArrivalBonus.claim(data, new ServerData()).orElseThrow();
        assertClose(2 * 20, result.coal());
    }
}
