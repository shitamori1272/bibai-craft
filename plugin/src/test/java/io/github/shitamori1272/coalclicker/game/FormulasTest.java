package io.github.shitamori1272.coalclicker.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FormulasTest {

    private static void assertClose(double expected, BigNum actual) {
        assertClose(BigNum.of(expected), actual, 1e-9);
    }

    /** 相対誤差 tolerance 以内で等しい。 */
    private static void assertClose(BigNum expected, BigNum actual, double tolerance) {
        if (expected.isZero()) {
            assertTrue(actual.isZero(), () -> "期待 0 / 実際 " + actual);
            return;
        }
        assertEquals(1.0, actual.divide(expected).toDouble(), tolerance,
                () -> "期待 " + expected + " / 実際 " + actual);
    }

    // ---- 価格 ----

    @Test
    void 価格は買うたびに1_15倍() {
        assertClose(150, Formulas.price(Tool.LAMP, 0));
        assertClose(150 * 1.15, Formulas.price(Tool.LAMP, 1));
        assertClose(150 * Math.pow(1.15, 10), Formulas.price(Tool.LAMP, 10));
        assertClose(3.3e9, Formulas.price(Tool.HEADFRAME, 0));
    }

    @Test
    void 価格は所持数が極端でもあふれない() {
        // 1.15^(10^12) ＝ 10^(10^12 × log10(1.15))
        BigNum price = Formulas.price(Tool.LAMP, 1_000_000_000_000L);
        double expectedLog = Math.log10(150) + 1e12 * Math.log10(1.15);
        assertEquals(expectedLog, price.log10(), 1e-3);
        assertTrue(Formulas.price(Tool.LAMP, Long.MAX_VALUE).signum() > 0);
    }

    @Test
    void まとめ買いの価格は1個ずつの合計() {
        BigNum sum = BigNum.ZERO;
        for (int i = 0; i < 10; i++) {
            sum = sum.add(Formulas.price(Tool.MINECART, 5 + i));
        }
        assertClose(sum, Formulas.totalPrice(Tool.MINECART, 5, 10), 1e-9);
        assertTrue(Formulas.totalPrice(Tool.MINECART, 5, 0).isZero());
        assertClose(1000, Formulas.totalPrice(Tool.MINECART, 0, 1));
    }

    @Test
    void 負の所持数は例外() {
        assertThrows(IllegalArgumentException.class, () -> Formulas.price(Tool.LAMP, -1));
        assertThrows(IllegalArgumentException.class, () -> Formulas.totalPrice(Tool.LAMP, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> Formulas.depthMultiplier(-1));
    }

    // ---- 毎秒の石炭 ----

    @Test
    void 毎秒の石炭は道具の基本値と所持数の積の和() {
        Map<String, Long> tools = Map.of("lamp", 10L, "minecart", 4L, "pickaxe", 99L);
        // 10 × 1 ＋ 4 × 10 ＝ 50（知らない ID は無視）
        assertClose(50, Formulas.coalPerSecond(tools, Set.of(), 0));
        assertTrue(Formulas.coalPerSecond(Map.of(), Set.of(), 0).isZero());
    }

    @Test
    void 道具の改良1つでその道具だけ2倍() {
        Map<String, Long> tools = Map.of("lamp", 10L, "minecart", 4L);
        assertClose(1, Formulas.toolMultiplier(Tool.LAMP, Set.of()));
        assertClose(2, Formulas.toolMultiplier(Tool.LAMP, Set.of("lamp_1")));
        assertClose(8, Formulas.toolMultiplier(Tool.LAMP, Set.of("lamp_1", "lamp_2", "lamp_7", "minecart_1")));
        // 10 × 1 × 4 ＋ 4 × 10 × 2 ＝ 120
        assertClose(120, Formulas.coalPerSecond(tools, Set.of("lamp_1", "lamp_2", "minecart_3"), 0));
        assertClose(40, Formulas.toolCoalPerSecond(Tool.LAMP, tools, Set.of("lamp_1", "lamp_2"), 0));
        assertEquals(2, Formulas.toolUpgradeCount(Tool.LAMP, Set.of("lamp_1", "lamp_2", "pickaxe_1")));
    }

    @Test
    void 全体の倍率は今は1() {
        assertClose(1, Formulas.globalMultiplier(Set.of("lamp_1", "skill_1")));
    }

    @Test
    void 深さ倍率は1_1の階層番号乗() {
        assertClose(1, Formulas.depthMultiplier(0));
        assertClose(1.1, Formulas.depthMultiplier(1));
        assertClose(Math.pow(1.1, 100), Formulas.depthMultiplier(100));
        // double を超える階層でも指数を直接求める
        BigNum deep = Formulas.depthMultiplier(1_000_000_000L);
        assertEquals(1e9 * Math.log10(1.1), deep.log10(), 1e-4);
        assertClose(BigNum.of(1.1), Formulas.depthMultiplier(7_301).divide(Formulas.depthMultiplier(7_300)), 1e-9);
        // double の範囲の境目をまたいでもつながる
        assertClose(BigNum.of(1.1), Formulas.depthMultiplier(7_247).divide(Formulas.depthMultiplier(7_246)), 1e-9);
        assertTrue(Formulas.depthMultiplier(Long.MAX_VALUE).signum() > 0);
    }

    @Test
    void 毎秒の石炭に深さ倍率が掛かる() {
        Map<String, Long> tools = Map.of("lamp", 10L);
        assertClose(10 * Math.pow(1.1, 5), Formulas.coalPerSecond(tools, Set.of(), 5));
        GameState state = GameState.of(tools, Set.of("lamp_1"), 0, BigNum.of(3_000)); // 階層 2
        assertClose(20 * 1.21, Formulas.coalPerSecond(state));
    }

    @Test
    void もう1つ買うと増える毎秒の石炭() {
        Map<String, Long> tools = Map.of("blasting", 3L);
        Set<String> upgrades = Set.of("blasting_1");
        BigNum before = Formulas.coalPerSecond(tools, upgrades, 4);
        BigNum after = Formulas.coalPerSecond(Map.of("blasting", 4L), upgrades, 4);
        assertClose(after.subtract(before), Formulas.marginalCoalPerSecond(Tool.BLASTING, upgrades, 4), 1e-9);
        assertClose(80 * 2 * Math.pow(1.1, 4), Formulas.marginalCoalPerSecond(Tool.BLASTING, upgrades, 4));
        // 持っていなくても 1 つ目の分が出る
        assertClose(1, Formulas.marginalCoalPerSecond(Tool.LAMP, Set.of(), 0));
    }

    // ---- 手掘り ----

    @Test
    void 手掘りの基本は10個でツルハシの改良1つごとに2倍() {
        assertClose(10, Formulas.clickCoal(Set.of(), BigNum.of(1e6)));
        assertClose(20, Formulas.clickCoal(Set.of("pickaxe_1"), BigNum.ZERO));
        assertClose(80, Formulas.clickCoal(Set.of("pickaxe_1", "pickaxe_2", "pickaxe_3"), BigNum.ZERO));
        assertClose(80, Formulas.clickBase(Set.of("pickaxe_1", "pickaxe_2", "pickaxe_3", "lamp_1")));
    }

    @Test
    void 熟練の改良1つごとに毎秒の石炭の1パーセントを上乗せ() {
        // 10 × 2 ＋ 1,000 × 0.01 × 2 ＝ 40
        assertClose(40, Formulas.clickCoal(Set.of("pickaxe_1", "skill_1", "skill_2"), BigNum.of(1_000)));
        assertEquals(2, Formulas.skillUpgradeCount(Set.of("skill_1", "skill_2", "pickaxe_1")));
        assertEquals(1, Formulas.pickaxeUpgradeCount(Set.of("skill_1", "skill_2", "pickaxe_1")));
        // GameState からは今の階層の毎秒の石炭を使う：100 × 1.1 × 0.01 ＋ 10 ＝ 11.1
        GameState state = GameState.of(Map.of("minecart", 10L), Set.of("skill_1"), 0, BigNum.of(1_000));
        assertClose(11.1, Formulas.clickCoal(state));
    }

    // ---- 累計採掘量による坑道 ----

    @Test
    void 必要量と累計() {
        assertClose(1_000, Formulas.levelRequirement(0));
        assertClose(2_000, Formulas.levelRequirement(1));
        assertClose(1_000 * Math.pow(2, 50), Formulas.levelRequirement(50));
        assertTrue(Formulas.cumulativeToFinish(-1).isZero());
        assertClose(1_000, Formulas.cumulativeToFinish(0));
        assertClose(3_000, Formulas.cumulativeToFinish(1));
        assertClose(1_000 * (Math.pow(2, 41) - 1), Formulas.cumulativeToFinish(40));
        assertClose(1_000 * (Math.pow(2, 101) - 1), Formulas.cumulativeToFinish(100));
        // 累計 ＝ 必要量の和
        BigNum sum = BigNum.ZERO;
        for (int n = 0; n < 60; n++) {
            sum = sum.add(Formulas.levelRequirement(n));
            assertClose(sum, Formulas.cumulativeToFinish(n), 1e-12);
        }
        assertThrows(IllegalArgumentException.class, () -> Formulas.cumulativeToFinish(-2));
    }

    @Test
    void 階層は累計の境目ちょうどで切り替わる() {
        assertEquals(0, Formulas.levelOf(BigNum.ZERO));
        assertEquals(0, Formulas.levelOf(BigNum.of(-5)));
        assertEquals(0, Formulas.levelOf(BigNum.of(999.999)));
        assertEquals(1, Formulas.levelOf(BigNum.of(1_000)));
        assertEquals(1, Formulas.levelOf(BigNum.of(2_999)));
        assertEquals(2, Formulas.levelOf(BigNum.of(3_000)));
        for (int n = 1; n <= 45; n++) {
            double boundary = 1_000 * (Math.pow(2, n) - 1);
            assertEquals(n, Formulas.levelOf(BigNum.of(boundary)), "境目 " + n);
            assertEquals(n - 1, Formulas.levelOf(BigNum.of(boundary).multiply(1 - 1e-12)), "境目の手前 " + n);
        }
        // それより深い階層でも cumulativeToFinish と食い違わない
        for (long n : new long[] {46, 47, 100, 1_000, 1_023, 1_024, 5_000, 1_000_000, 1L << 40, 1L << 60}) {
            BigNum boundary = Formulas.cumulativeToFinish(n - 1);
            assertEquals(n, Formulas.levelOf(boundary), "境目 " + n);
            assertEquals(n - 1, Formulas.levelOf(boundary.multiply(1 - 1e-12)), "境目の手前 " + n);
        }
    }

    @Test
    void 階層はとても大きな累計でも計算できる() {
        // 1e300 ＝ 1000 × 2^n → n ≈ log2(1e297) ≈ 986.6
        assertEquals((long) Math.floor(297 / Math.log10(2)), Formulas.levelOf(BigNum.of(1e300)));
        assertEquals((long) Math.floor(4997 / Math.log10(2)), Formulas.levelOf(BigNum.of(1, 5000)));
        long deep = Formulas.levelOf(BigNum.of(1, 1_000_000_000_000L));
        assertEquals(Math.floor((1e12 - 3) / Math.log10(2)), deep, 1e-3 * deep);
        // long の上限で頭打ち
        assertEquals(Long.MAX_VALUE, Formulas.levelOf(BigNum.of(9, Long.MAX_VALUE - 10)));
        assertTrue(Formulas.progressOf(BigNum.of(9, Long.MAX_VALUE - 10)) < 1);
    }

    @Test
    void 進み具合は今の階層の中の割合() {
        assertEquals(0, Formulas.progressOf(BigNum.ZERO));
        assertEquals(0.5, Formulas.progressOf(BigNum.of(500)), 1e-12);
        assertEquals(0, Formulas.progressOf(BigNum.of(1_000)), 1e-12);
        assertEquals(0.47, Formulas.progressOf(BigNum.of(1_940)), 1e-12);
        assertEquals(0.25, Formulas.progressOf(BigNum.of(4_000)), 1e-12);
        // 深い階層でも 0〜1 に収まる
        for (long n : new long[] {100, 10_000, 1L << 40}) {
            BigNum start = Formulas.cumulativeToFinish(n - 1);
            BigNum mid = start.add(Formulas.levelRequirement(n).multiply(0.3));
            assertEquals(n, Formulas.levelOf(mid));
            assertEquals(0.3, Formulas.progressOf(mid), 1e-6);
        }
        double p = Formulas.progressOf(BigNum.of(1, 5000));
        assertTrue(p >= 0 && p < 1, "進み具合 " + p);
    }

    @Test
    void 坑道の長さは64に階層ごと16を足す() {
        assertEquals(64, Formulas.tunnelLength(0));
        assertEquals(80, Formulas.tunnelLength(1));
        assertEquals(64 + 16 * 99, Formulas.tunnelLength(99));
    }

    // ---- 到達ボーナス ----

    /** Σ max(base × 1.1^L × 60, min) を 1 階層ずつ足す。 */
    private static BigNum bruteForceBonus(long from, long to, BigNum base, BigNum min) {
        BigNum sum = BigNum.ZERO;
        for (long level = from + 1; level <= to; level++) {
            sum = sum.add(base.multiply(Formulas.depthMultiplier(level)).multiply(60).max(min));
        }
        return sum;
    }

    @Test
    void 到達ボーナスは1階層ずつ足したものと同じ() {
        BigNum[] bases = {BigNum.ZERO, BigNum.of(0.01), BigNum.of(0.1), BigNum.of(1), BigNum.of(1e6)};
        BigNum[] mins = {BigNum.ZERO, BigNum.of(10), BigNum.of(80), BigNum.of(1e9)};
        long[][] ranges = {{-1, 0}, {0, 1}, {0, 5}, {3, 40}, {-1, 120}, {50, 51}};
        for (BigNum base : bases) {
            for (BigNum min : mins) {
                for (long[] r : ranges) {
                    BigNum expected = bruteForceBonus(r[0], r[1], base, min);
                    BigNum actual = Formulas.arrivalBonus(r[0], r[1], base, min);
                    assertClose(expected, actual, 1e-9);
                }
            }
        }
    }

    @Test
    void 到達ボーナスの最低保証() {
        // 毎秒 0 でも 1 階層あたり手掘り 1 回分
        assertClose(30, Formulas.arrivalBonus(0, 3, BigNum.ZERO, BigNum.of(10)));
        // 毎秒 1 なら 60 × 1.1 ＝ 66 個
        assertClose(66, Formulas.arrivalBonus(0, 1, BigNum.ONE, BigNum.of(10)));
        assertTrue(Formulas.arrivalBonus(5, 5, BigNum.ONE, BigNum.of(10)).isZero());
        assertTrue(Formulas.arrivalBonus(5, 3, BigNum.ONE, BigNum.of(10)).isZero());
        assertThrows(IllegalArgumentException.class, () -> Formulas.arrivalBonus(-2, 3, BigNum.ONE, BigNum.ONE));
    }

    @Test
    void 到達ボーナスは何百万階層でも一度に計算できる() {
        BigNum bonus = Formulas.arrivalBonus(0, 10_000_000, BigNum.of(5), BigNum.of(10));
        // ほぼ最後の項 × 1.1 ÷ 0.1 ＝ 5 × 60 × 1.1^(10^7) × 11
        double expectedLog = Math.log10(5 * 60 * 11) + 1e7 * Math.log10(1.1);
        assertEquals(expectedLog, bonus.log10(), 1e-6);
        assertTrue(Formulas.arrivalBonus(-1, Long.MAX_VALUE, BigNum.ONE, BigNum.ONE).signum() > 0);
        assertTrue(Formulas.arrivalBonus(-1, Long.MAX_VALUE, BigNum.ZERO, BigNum.ONE).signum() > 0);
    }
}
