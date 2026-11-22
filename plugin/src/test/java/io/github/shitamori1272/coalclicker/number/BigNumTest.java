package io.github.shitamori1272.coalclicker.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BigNumTest {

    private static void assertNum(double mantissa, long exponent, BigNum actual) {
        assertEquals(exponent, actual.exponent(), () -> "指数: " + actual);
        assertEquals(mantissa, actual.mantissa(), 1e-9, () -> "仮数: " + actual);
    }

    @Test
    void 正規化される() {
        assertNum(1.234, 3, BigNum.of(1234));
        assertNum(5, -1, BigNum.of(0.5));
        assertNum(1, 1, BigNum.of(10));
        assertNum(1.5, 101, BigNum.of(15, 100));
        assertTrue(BigNum.of(0).isZero());
    }

    @Test
    void 四則演算() {
        assertEquals(BigNum.of(300), BigNum.of(100).add(BigNum.of(200)));
        assertEquals(BigNum.of(50), BigNum.of(150).subtract(BigNum.of(100)));
        assertTrue(BigNum.of(100).subtract(BigNum.of(100)).isZero());
        assertEquals(BigNum.of(-50), BigNum.of(100).subtract(BigNum.of(150)));
        assertEquals(BigNum.of(600), BigNum.of(20).multiply(BigNum.of(30)));
        assertEquals(BigNum.of(2.5), BigNum.of(10).divide(4));
        assertThrows(ArithmeticException.class, () -> BigNum.ONE.divide(BigNum.ZERO));
    }

    @Test
    void 桁が大きく違う足し算は大きい方が残る() {
        BigNum big = BigNum.of(1, 100);
        assertEquals(big, big.add(BigNum.of(1)));
    }

    @Test
    void doubleの上限を超えて計算できる() {
        BigNum huge = BigNum.of(1, 300).multiply(BigNum.of(1, 300));
        assertNum(1, 600, huge);
        assertNum(2, 600, huge.add(huge));
        assertEquals(Double.POSITIVE_INFINITY, huge.toDouble());
    }

    @Test
    void 累乗を対数から求める() {
        assertNum(1.21, 0, BigNum.pow(1.1, 2));
        // 1.1^4898 ≒ 10^202.74
        BigNum deep = BigNum.pow(1.1, 4898);
        assertEquals(202, deep.exponent());
        assertEquals(4898 * Math.log10(1.1), deep.log10(), 1e-9);
        // double では無限大になる階層でも求められる
        assertEquals(100_000_000L * Math.log10(1.1), BigNum.pow(1.1, 100_000_000L).log10(), 1e-3);
        assertTrue(BigNum.pow(1.1, 0).equals(BigNum.ONE));
    }

    @Test
    void 比較() {
        assertTrue(BigNum.of(1, 1000).compareTo(BigNum.of(9, 999)) > 0);
        assertTrue(BigNum.of(2).compareTo(BigNum.of(3)) < 0);
        assertTrue(BigNum.of(-1).compareTo(BigNum.ZERO) < 0);
        assertTrue(BigNum.of(-2).compareTo(BigNum.of(-1)) < 0);
        assertTrue(BigNum.of(-1, 100).compareTo(BigNum.of(-1, 50)) < 0);
        assertTrue(BigNum.of(500).isGreaterThanOrEqual(BigNum.of(500)));
        assertEquals(BigNum.of(5), BigNum.of(5).max(BigNum.of(3)));
    }

    @Test
    void 保存用の文字列と相互変換できる() {
        for (BigNum n : new BigNum[] {BigNum.ZERO, BigNum.of(1234.5), BigNum.of(1.234, 5678), BigNum.of(-7, 12)}) {
            assertEquals(n, BigNum.parse(n.toString()));
        }
        assertNum(2, 1000, BigNum.parse("2e1000"));
        assertNum(1.5, 300, BigNum.parse("1.5E300"));
        assertNum(1.2, 3, BigNum.parse(" 1200 "));
        assertThrows(NumberFormatException.class, () -> BigNum.parse("abc"));
    }
}
