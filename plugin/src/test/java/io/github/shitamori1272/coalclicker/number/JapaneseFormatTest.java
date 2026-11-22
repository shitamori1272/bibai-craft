package io.github.shitamori1272.coalclicker.number;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JapaneseFormatTest {

    private static String f(double value) {
        return JapaneseFormat.format(BigNum.of(value));
    }

    private static String f(double mantissa, long exponent) {
        return JapaneseFormat.format(BigNum.of(mantissa, exponent));
    }

    @Test
    void 一万未満() {
        assertEquals("0", f(0));
        assertEquals("12.3", f(12.34));
        assertEquals("12", f(12));
        assertEquals("1,234", f(1234.9));
        assertEquals("9,999", f(9999));
    }

    @Test
    void 単位を付ける() {
        assertEquals("1万", f(10000));
        assertEquals("1.234万", f(12345));
        assertEquals("12.34万", f(123456));
        assertEquals("1234万", f(12_345_678));
        assertEquals("1億", f(1e8));
        assertEquals("1.5兆", f(1.5e12));
        assertEquals("3京", f(3, 16));
        assertEquals("1無量大数", f(1, 68));
        assertEquals("9999無量大数", f(9.999, 71));
    }

    @Test
    void 切り捨てで表示する() {
        // まだ 1 億に届いていない数を「1億」と表示しない
        assertEquals("9999万", f(99_996_000));
        assertEquals("9.999万", f(99_999));
        assertEquals("9,999", f(9999.6));
        assertEquals("1.2万", f(12_000));
    }

    @Test
    void 無量大数を超えたら指数表記() {
        assertEquals("1×10^72", f(1, 72));
        assertEquals("1.23×10^1234", f(1.234, 1234));
    }

    @Test
    void 負の数() {
        assertEquals("-1.5万", f(-15000));
    }
}
