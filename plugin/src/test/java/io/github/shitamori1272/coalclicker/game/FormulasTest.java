package io.github.shitamori1272.coalclicker.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FormulasTest {

    /** 相対誤差で比べる。 */
    static void assertClose(BigNum expected, BigNum actual) {
        if (expected.isZero()) {
            assertTrue(actual.isZero(), () -> "0 のはず: " + actual);
            return;
        }
        double ratio = actual.divide(expected).toDouble();
        assertEquals(1.0, ratio, 1e-9, () -> "期待 " + expected + " / 実際 " + actual);
    }

    static void assertClose(double expected, BigNum actual) {
        assertClose(BigNum.of(expected), actual);
    }

    // ---- 道具 ----

    @Test
    void 道具は6種類でIDから引ける() {
        assertEquals(6, Tool.values().length);
        assertEquals(Tool.LAMP, Tool.byId("lamp").orElseThrow());
        assertEquals(Tool.COAL_PICK, Tool.byId("coal_pick").orElseThrow());
        assertTrue(Tool.byId("unknown").isEmpty());
        for (Tool tool : Tool.values()) {
            assertEquals(tool, Tool.byId(tool.id()).orElseThrow());
            assertTrue(!tool.explanation().isBlank() && !tool.shortDescription().isBlank());
        }
        assertEquals(ToolKind.MANUAL, Tool.PICKAXE.kind());
        assertEquals(ToolKind.DIG, Tool.DRILL.kind());
        assertEquals("TNT", Tool.BLASTING.iconMaterial());
    }

    // ---- 価格 ----

    @Test
    void 価格は買うたびに倍率が掛かる() {
        assertClose(150, Formulas.price(Tool.LAMP, 0));
        assertClose(172.5, Formulas.price(Tool.LAMP, 1));
        assertClose(198.375, Formulas.price(Tool.LAMP, 2));
        assertClose(500, Formulas.price(Tool.PICKAXE, 0));
        assertClose(1000, Formulas.price(Tool.PICKAXE, 1));
        assertClose(4000, Formulas.price(Tool.PICKAXE, 3));
        assertClose(200, Formulas.price(Tool.BLASTING, 0));
        assertClose(12_000, Formulas.price(Tool.COAL_PICK, 0));
    }

    @Test
    void 大量に持っていても価格があふれない() {
        BigNum p = Formulas.price(Tool.PICKAXE, 10_000);
        assertEquals(Math.log10(500) + 10_000 * Math.log10(2), p.log10(), 1e-6);
    }

    @Test
    void まとめ買いの価格は1つずつ買った合計と同じ() {
        assertClose(150 + 172.5 + 198.375, Formulas.totalPrice(Tool.LAMP, 0, 3));
        assertClose(1000 + 2000 + 4000, Formulas.totalPrice(Tool.PICKAXE, 1, 3));
        assertTrue(Formulas.totalPrice(Tool.DRILL, 5, 0).isZero());
        for (Tool tool : Tool.values()) {
            BigNum sum = BigNum.ZERO;
            for (long i = 7; i < 7 + 25; i++) {
                sum = sum.add(Formulas.price(tool, i));
            }
            assertClose(sum, Formulas.totalPrice(tool, 7, 25));
        }
        assertClose(Formulas.price(Tool.MINECART, 4), Formulas.totalPrice(Tool.MINECART, 4, 1));
    }

    @Test
    void 負の所持数は受け付けない() {
        assertThrows(IllegalArgumentException.class, () -> Formulas.price(Tool.LAMP, -1));
        assertThrows(IllegalArgumentException.class, () -> Formulas.totalPrice(Tool.LAMP, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> Formulas.digPower(Map.of("drill", -1L)));
    }

    // ---- ステータス ----

    @Test
    void 初期のステータス() {
        assertTrue(Formulas.digPower(Map.of()).isZero());
        assertClose(100, Formulas.coalPower(Map.of()));
    }

    @Test
    void 採炭力と掘進力は道具ごとに集計する() {
        Map<String, Long> tools = new HashMap<>();
        tools.put("lamp", 3L);
        tools.put("minecart", 2L);
        tools.put("coal_pick", 1L);
        tools.put("blasting", 5L);
        tools.put("drill", 2L);
        tools.put("pickaxe", 7L); // 手掘りはどちらにも入らない
        tools.put("unknown", 9L); // 知らない ID は無視
        assertClose(100 + 30 + 200 + 1000, Formulas.coalPower(tools));
        assertClose(5 + 20, Formulas.digPower(tools));
        assertEquals(7, Formulas.count(tools, Tool.PICKAXE));
        assertEquals(0, Formulas.count(Map.of(), Tool.PICKAXE));
    }

    @Test
    void 所持数がlongの上限近くでもあふれない() {
        BigNum d = Formulas.digPower(Map.of("drill", Long.MAX_VALUE, "blasting", Long.MAX_VALUE));
        assertEquals(Math.log10(11.0 * Long.MAX_VALUE), d.log10(), 1e-9);
    }

    // ---- 掘進 ----

    @Test
    void 掘進速度() {
        assertEquals(0, Formulas.digSpeed(BigNum.ZERO));
        assertEquals(0.1, Formulas.digSpeed(BigNum.ONE), 1e-12);
        assertEquals(1.0, Formulas.digSpeed(BigNum.of(100)), 1e-12);
        assertEquals(0.5, Formulas.digSpeed(BigNum.of(25)), 1e-12);
        assertEquals(1e99, Formulas.digSpeed(BigNum.of(1, 200)), 1e87);
        assertEquals(Double.MAX_VALUE, Formulas.digSpeed(BigNum.of(1, 10_000)));
    }

    @Test
    void 坑道の長さ() {
        assertEquals(64, Formulas.tunnelLength(0));
        assertEquals(80, Formulas.tunnelLength(1));
        assertEquals(1_648, Formulas.tunnelLength(99)); // 第 100 坑道
        // 企画書 5-5 の「第 4898 坑道 78,432m」は 64 + 16 × 4898。第 1 坑道 = 階層 0 とすると 1 つずれている
        assertEquals(78_432, Formulas.tunnelLength(4_898));
        assertEquals(64 + 16.0 * Long.MAX_VALUE, Formulas.tunnelLength(Long.MAX_VALUE));
    }

    @Test
    void 階層にかかる時間() {
        // 発破 1 つ：0.1m/秒で 64m → 640 秒（約 11 分）
        assertEquals(640, Formulas.levelSeconds(0, BigNum.ONE), 1e-9);
        assertEquals(800, Formulas.levelSeconds(1, BigNum.ONE), 1e-9);
        // 速すぎても最短 10 秒
        assertEquals(10, Formulas.levelSeconds(0, BigNum.of(1_000_000)));
        assertEquals(10, Formulas.levelSeconds(0, BigNum.of(1, 10_000)));
        assertEquals(Double.POSITIVE_INFINITY, Formulas.levelSeconds(0, BigNum.ZERO));
        assertEquals(Double.POSITIVE_INFINITY, Formulas.levelSecondsAtSpeed(0, 0));
    }

    @Test
    void 深さ倍率() {
        assertClose(1, Formulas.depthMultiplier(0));
        assertClose(1.1, Formulas.depthMultiplier(1));
        assertClose(1.21, Formulas.depthMultiplier(2));
        assertEquals(4898 * Math.log10(1.1), Formulas.depthMultiplier(4898).log10(), 1e-6);
        assertEquals(202.7, Formulas.depthMultiplier(4898).log10(), 0.1);
        assertEquals(Long.MAX_VALUE * Math.log10(1.1),
                Formulas.depthMultiplier(Long.MAX_VALUE).log10(), Long.MAX_VALUE * 1e-9);
    }

    // ---- 石炭 ----

    @Test
    void 一メートルあたりの石炭() {
        assertClose(100, Formulas.coalPerMeter(BigNum.of(100), 0));
        assertClose(220, Formulas.coalPerMeter(BigNum.of(200), 1));
        // 手掘り：(E + 100 × ツルハシ) × 深さ倍率
        assertClose(100, Formulas.manualCoalPerMeter(BigNum.of(100), 0, 0));
        assertClose(200, Formulas.manualCoalPerMeter(BigNum.of(100), 1, 0));
        assertClose((150 + 300) * 1.21, Formulas.manualCoalPerMeter(BigNum.of(150), 3, 2));
    }

    // ---- 到達ボーナス ----

    /** 1 階層ずつ足す素朴な計算。 */
    private static BigNum bruteForceBonus(long fromExclusive, long toInclusive, double speed, BigNum e) {
        BigNum sum = BigNum.ZERO;
        for (long level = fromExclusive + 1; level <= toInclusive; level++) {
            BigNum perMeter = e.multiply(Formulas.depthMultiplier(level));
            BigNum perMinute = perMeter.multiply(speed * 60);
            sum = sum.add(perMinute.max(perMeter));
        }
        return sum;
    }

    @Test
    void 到達ボーナスの例() {
        // 第 2 坑道（深さ倍率 1.1）に 0.2m/秒・E = 200 で着くと約 2,640 個
        assertClose(2_640, Formulas.arrivalBonus(0, 1, 0.2, BigNum.of(200)));
    }

    @Test
    void 到達ボーナスは最低でも1m分() {
        // 速度 0 なら E × 深さ倍率
        assertClose(100 * 1.1, Formulas.arrivalBonus(0, 1, 0, BigNum.of(100)));
        // 0.01m/秒 × 60 = 0.6m 分 < 1m 分
        assertClose(100 * 1.21, Formulas.arrivalBonus(1, 2, 0.01, BigNum.of(100)));
    }

    @Test
    void 到達ボーナスの合計は一つずつ足した値と同じ() {
        double[] speeds = {0, 0.01, 1.0 / 60, 0.1, 2.2, 1234.5};
        long[][] ranges = {{-1, 0}, {0, 1}, {1, 4}, {-1, 30}, {100, 137}, {4_000, 4_898}};
        for (double speed : speeds) {
            for (long[] r : ranges) {
                BigNum e = BigNum.of(1330);
                assertClose(bruteForceBonus(r[0], r[1], speed, e), Formulas.arrivalBonus(r[0], r[1], speed, e));
            }
        }
    }

    @Test
    void 到達ボーナスは何百万階層でも一瞬で計算できる() {
        BigNum bonus = Formulas.arrivalBonus(-1, 10_000_000, 5.0, BigNum.of(100));
        // Σ_{L=0}^{n} 1.1^L ≈ 1.1^(n+1) ÷ 0.1
        double expected = Math.log10(100 * 5.0 * 60) + 10_000_001 * Math.log10(1.1) + 1;
        assertEquals(expected, bonus.log10(), 1e-3);
        assertTrue(Formulas.arrivalBonus(0, Long.MAX_VALUE / 2, Double.MAX_VALUE, BigNum.of(1, 500)).signum() > 0);
    }

    @Test
    void 到達ボーナスは範囲が空なら0() {
        assertTrue(Formulas.arrivalBonus(5, 5, 1, BigNum.of(100)).isZero());
        assertTrue(Formulas.arrivalBonus(5, 3, 1, BigNum.of(100)).isZero());
        assertThrows(IllegalArgumentException.class, () -> Formulas.arrivalBonus(-2, 3, 1, BigNum.of(100)));
    }
}
