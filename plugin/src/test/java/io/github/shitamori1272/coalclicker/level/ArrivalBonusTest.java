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

    /** 掘進速度 0.2m/秒（発破 4 個）・E = 200（安全灯 10 個）のプレイヤー。 */
    private static PlayerData player(long level, long maxReached) {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setToolCount(Tool.BLASTING.id(), 4);
        data.setToolCount(Tool.LAMP.id(), 10);
        data.mine().setLevel(level);
        data.mine().setMaxReachedLevel(maxReached);
        return data;
    }

    @Test
    void 企画書の例_第2坑道に着くと約2640個() {
        PlayerData data = player(1, 0);
        ServerData server = new ServerData();
        Optional<ArrivalBonus.Result> result = ArrivalBonus.claim(data, server);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().fromLevel());
        assertEquals(1, result.get().toLevel());
        assertEquals(1, result.get().levels());
        assertClose(2_640, result.get().coal());
        assertClose(2_640, data.coal());
        assertClose(2_640, data.totalMined());
        assertClose(2_640, server.serverTotalMined());
        assertEquals(1, data.mine().maxReachedLevel());
    }

    @Test
    void 降りなかった階層の分もまとめてもらえる() {
        // 第 2 坑道から一気に第 5 坑道へ：第 3・4・5 坑道のボーナス
        PlayerData data = player(4, 1);
        data.setCoal(BigNum.of(1_000));
        ServerData server = new ServerData();
        ArrivalBonus.Result result = ArrivalBonus.claim(data, server).orElseThrow();
        assertEquals(3, result.levels());
        double perLevel = 0.2 * 200 * 60;
        double expected = perLevel * (Math.pow(1.1, 2) + Math.pow(1.1, 3) + Math.pow(1.1, 4));
        assertClose(expected, result.coal());
        assertClose(1_000 + expected, data.coal());
        assertClose(expected, data.totalMined());
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
        assertEquals(0, data.mine().maxReachedLevel());
    }

    @Test
    void 最深より浅い階層では何もしない() {
        PlayerData data = player(2, 5);
        assertTrue(ArrivalBonus.claim(data, new ServerData()).isEmpty());
        assertEquals(5, data.mine().maxReachedLevel());
    }

    @Test
    void 掘進力0でも1m分は保証される() {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.mine().setLevel(1);
        ArrivalBonus.Result result = ArrivalBonus.claim(data, new ServerData()).orElseThrow();
        assertClose(100 * 1.1, result.coal());
    }

    @Test
    void 何百万階層ぶんでも一瞬で計算する() {
        PlayerData data = player(10_000_000, 0);
        ArrivalBonus.Result result = ArrivalBonus.claim(data, new ServerData()).orElseThrow();
        assertEquals(10_000_000, result.levels());
        BigNum expected = Formulas.arrivalBonus(0, 10_000_000, 0.2, BigNum.of(200));
        assertEquals(expected.log10(), result.coal().log10(), 1e-6);
        assertEquals(10_000_000, data.mine().maxReachedLevel());
    }
}
