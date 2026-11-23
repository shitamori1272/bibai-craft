package io.github.shitamori1272.coalclicker.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
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
        Optional<ArrivalBonus.Result> result = ArrivalBonus.claim(data, 1);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().fromLevel());
        assertEquals(1, result.get().toLevel());
        assertEquals(1, result.get().levels());
        // 10 × 1.1 × 60 ＝ 660
        assertClose(660, result.get().coal());
        assertClose(660, data.coal());
        // 石炭がもらえるだけで、累計採掘量（坑道の進み）は増えない
        assertEquals(before, data.totalMined());
        assertEquals(1, data.mine().maxReachedLevel());
    }

    @Test
    void 降りなかった階層の分もまとめてもらえる() {
        PlayerData data = player(4, 1);
        ArrivalBonus.Result result = ArrivalBonus.claim(data, 4).orElseThrow();
        assertEquals(3, result.levels());
        double expected = 10 * 60 * (Math.pow(1.1, 2) + Math.pow(1.1, 3) + Math.pow(1.1, 4));
        assertClose(expected, result.coal());
        assertEquals(4, data.mine().maxReachedLevel());
    }

    @Test
    void 同じ階層では二重にもらえない() {
        PlayerData data = player(3, 0);
        assertTrue(ArrivalBonus.claim(data, 3).isPresent());
        BigNum after = data.coal();
        assertTrue(ArrivalBonus.claim(data, 3).isEmpty());
        assertEquals(after, data.coal());
        assertEquals(3, data.mine().maxReachedLevel());
    }

    @Test
    void 第1坑道ではもらえない() {
        PlayerData data = player(0, 0);
        assertTrue(ArrivalBonus.claim(data, 0).isEmpty());
        assertTrue(data.coal().isZero());
    }

    @Test
    void 最深より浅い階層では何もしない() {
        PlayerData data = player(2, 5);
        assertTrue(ArrivalBonus.claim(data, 2).isEmpty());
        assertEquals(5, data.mine().maxReachedLevel());
    }

    @Test
    void 道具がなくても手掘り1回分は保証される() {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setTotalMined(Formulas.cumulativeToFinish(1)); // 階層 2
        data.addUpgrade("pickaxe_1");
        ArrivalBonus.Result result = ArrivalBonus.claim(data, 2).orElseThrow();
        assertClose(2 * 20, result.coal());
    }

    @Test
    void 毎秒の石炭が小さい浅い階層は最低保証で深い階層は60秒分() {
        // 深さ倍率の前で毎秒 1 個・手掘りの基本 80。60 × 1.1^L は階層 1〜3 で 80 未満（最低保証）、4 から上回る
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setToolCount(Tool.LAMP.id(), 1);
        data.addUpgrade("pickaxe_1");
        data.addUpgrade("pickaxe_2");
        data.addUpgrade("pickaxe_3"); // 手掘りの基本 80
        data.setTotalMined(Formulas.cumulativeToFinish(29));
        ArrivalBonus.Result result = ArrivalBonus.claim(data, 30).orElseThrow();
        double expected = 0;
        for (int level = 1; level <= 30; level++) {
            expected += Math.max(60 * Math.pow(1.1, level), 80);
        }
        assertClose(expected, result.coal());
    }

    @Test
    void ボーナスでは坑道は進まない() {
        // 階層 1 の境目のすぐ手前で着いても、ボーナス 660 で階層 2 には入らない
        PlayerData data = player(1, 0);
        BigNum before = Formulas.cumulativeToFinish(1).subtract(BigNum.of(100));
        data.setTotalMined(before);
        ArrivalBonus.Result first = ArrivalBonus.claim(data, 1).orElseThrow();
        assertClose(660, first.coal());
        assertEquals(before, data.totalMined());
        assertEquals(1, Formulas.levelOf(data.totalMined()));
        assertEquals(1, data.mine().maxReachedLevel());
    }
}
