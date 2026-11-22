package io.github.shitamori1272.coalclicker.number;

import java.util.Locale;

/**
 * double の上限（約 10^308）を超える数を扱うための不変な数値型。
 *
 * <p>値は {@code mantissa × 10^exponent} で表す。0 以外では仮数の絶対値が 1 以上 10 未満になるよう正規化する。
 * 指数は long なので、事実上どんなに大きな数でも扱える。精度は double と同じ（有効数字およそ 15〜16 桁）。
 */
public final class BigNum implements Comparable<BigNum> {

    public static final BigNum ZERO = new BigNum(0, 0);
    public static final BigNum ONE = new BigNum(1, 0);

    /** 指数の差がこれ以上あると、小さい方は加減算の結果に影響しない。 */
    private static final long PRECISION_DIGITS = 17;

    private final double mantissa;
    private final long exponent;

    private BigNum(double mantissa, long exponent) {
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    /** 仮数と指数から正規化した値を作る。 */
    public static BigNum of(double mantissa, long exponent) {
        if (Double.isNaN(mantissa) || Double.isInfinite(mantissa)) {
            throw new ArithmeticException("仮数が有限の数ではありません: " + mantissa);
        }
        if (mantissa == 0) {
            return ZERO;
        }
        long shift = (long) Math.floor(Math.log10(Math.abs(mantissa)));
        double m = mantissa / Math.pow(10, shift);
        long e = Math.addExact(exponent, shift);
        // 丸め誤差で 10 ちょうどや 1 未満になった場合を補正する
        if (Math.abs(m) >= 10) {
            m /= 10;
            e = Math.addExact(e, 1);
        } else if (Math.abs(m) < 1) {
            m *= 10;
            e = Math.subtractExact(e, 1);
        }
        return new BigNum(m, e);
    }

    public static BigNum of(double value) {
        return of(value, 0);
    }

    /** {@code base^power} を求める。掛け算を power 回繰り返さず、対数から直接計算する。 */
    public static BigNum pow(double base, long power) {
        if (base <= 0) {
            throw new IllegalArgumentException("底は正の数にしてください: " + base);
        }
        return fromLog10(power * Math.log10(base));
    }

    /** 常用対数 {@code log10(x)} から x を作る。 */
    public static BigNum fromLog10(double log10) {
        if (Double.isNaN(log10) || Double.isInfinite(log10)) {
            throw new ArithmeticException("対数が有限の数ではありません: " + log10);
        }
        double floor = Math.floor(log10);
        return of(Math.pow(10, log10 - floor), (long) floor);
    }

    /**
     * 保存用の文字列（{@link #toString()} の出力）や、{@code 1234}・{@code 1.5e300}・{@code 2e1000} のような表記を読み取る。
     */
    public static BigNum parse(String text) {
        String s = text.trim().toLowerCase(Locale.ROOT);
        try {
            int e = s.indexOf('e');
            if (e < 0) {
                return of(Double.parseDouble(s));
            }
            return of(Double.parseDouble(s.substring(0, e)), Long.parseLong(s.substring(e + 1)));
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new NumberFormatException("数値として読み取れません: " + text);
        }
    }

    public double mantissa() {
        return mantissa;
    }

    public long exponent() {
        return exponent;
    }

    public boolean isZero() {
        return mantissa == 0;
    }

    public int signum() {
        return (int) Math.signum(mantissa);
    }

    public BigNum negate() {
        return isZero() ? this : new BigNum(-mantissa, exponent);
    }

    public BigNum add(BigNum other) {
        if (isZero()) {
            return other;
        }
        if (other.isZero()) {
            return this;
        }
        BigNum big = exponent >= other.exponent ? this : other;
        BigNum small = big == this ? other : this;
        long diff = big.exponent - small.exponent;
        if (diff >= PRECISION_DIGITS) {
            return big;
        }
        return of(big.mantissa + small.mantissa / Math.pow(10, diff), big.exponent);
    }

    public BigNum subtract(BigNum other) {
        return add(other.negate());
    }

    public BigNum multiply(BigNum other) {
        if (isZero() || other.isZero()) {
            return ZERO;
        }
        return of(mantissa * other.mantissa, Math.addExact(exponent, other.exponent));
    }

    public BigNum multiply(double factor) {
        return multiply(of(factor));
    }

    public BigNum divide(BigNum other) {
        if (other.isZero()) {
            throw new ArithmeticException("0 で割ることはできません");
        }
        if (isZero()) {
            return ZERO;
        }
        return of(mantissa / other.mantissa, Math.subtractExact(exponent, other.exponent));
    }

    public BigNum divide(double divisor) {
        return divide(of(divisor));
    }

    /** 常用対数。正の数だけを受け付ける。 */
    public double log10() {
        if (signum() <= 0) {
            throw new ArithmeticException("正の数ではありません: " + this);
        }
        return exponent + Math.log10(mantissa);
    }

    /** double に変換する。範囲を超える場合は無限大または 0 になる。 */
    public double toDouble() {
        if (exponent > 308) {
            return mantissa > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        if (exponent < -324) {
            return 0;
        }
        return mantissa * Math.pow(10, exponent);
    }

    public BigNum max(BigNum other) {
        return compareTo(other) >= 0 ? this : other;
    }

    public BigNum min(BigNum other) {
        return compareTo(other) <= 0 ? this : other;
    }

    public boolean isGreaterThanOrEqual(BigNum other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(BigNum other) {
        int sign = signum();
        int otherSign = other.signum();
        if (sign != otherSign) {
            return Integer.compare(sign, otherSign);
        }
        if (sign == 0) {
            return 0;
        }
        // 同じ符号なら、指数が大きいほど絶対値が大きい
        int byMagnitude = exponent != other.exponent
                ? Long.compare(exponent, other.exponent)
                : Double.compare(Math.abs(mantissa), Math.abs(other.mantissa));
        return sign > 0 ? byMagnitude : -byMagnitude;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BigNum other && compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(mantissa) * 31 + Long.hashCode(exponent);
    }

    /** 保存用の表記（例: {@code 1.234e5678}）。{@link #parse(String)} で元に戻せる。 */
    @Override
    public String toString() {
        return isZero() ? "0" : mantissa + "e" + exponent;
    }
}
