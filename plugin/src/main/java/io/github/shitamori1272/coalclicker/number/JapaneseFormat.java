package io.github.shitamori1272.coalclicker.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * {@link BigNum} を日本の単位（万・億・兆…無量大数）で表示する。
 *
 * <ul>
 *   <li>1 万未満：{@code 1,234}・{@code 12.3}（100 未満は小数第 1 位まで）
 *   <li>1 万以上：有効数字 4 桁で単位を付ける（{@code 1.234万}・{@code 123.4億}・{@code 9999無量大数}）
 *   <li>無量大数の上限（10^72）以上：{@code 1.23×10^1234}
 * </ul>
 *
 * <p>表示はすべて切り捨てる。四捨五入すると、まだ足りない額でも価格に届いているように見えてしまうため。
 */
public final class JapaneseFormat {

    /** 4 桁ごとの単位。添字 i の単位は 10^(4i)。 */
    private static final String[] UNITS = {
        "", "万", "億", "兆", "京", "垓", "秭", "穣", "溝", "澗", "正", "載", "極",
        "恒河沙", "阿僧祇", "那由他", "不可思議", "無量大数",
    };

    private static final int SIGNIFICANT_DIGITS = 4;

    private JapaneseFormat() {
    }

    public static String format(BigNum value) {
        if (value.signum() < 0) {
            return "-" + format(value.negate());
        }
        if (value.isZero()) {
            return "0";
        }
        if (value.exponent() < 4) {
            return formatSmall(value.toDouble());
        }
        BigNum rounded = floorToSignificant(value, SIGNIFICANT_DIGITS);
        long exponent = rounded.exponent();
        if (exponent < 4L * UNITS.length) {
            int unit = (int) (exponent / 4);
            int integerDigits = (int) (exponent % 4) + 1;
            BigDecimal shown = BigDecimal.valueOf(rounded.mantissa())
                    .movePointRight(integerDigits - 1)
                    .setScale(SIGNIFICANT_DIGITS - integerDigits, RoundingMode.DOWN)
                    .stripTrailingZeros();
            return shown.toPlainString() + UNITS[unit];
        }
        BigDecimal mantissa = BigDecimal.valueOf(rounded.mantissa())
                .setScale(2, RoundingMode.DOWN)
                .stripTrailingZeros();
        return mantissa.toPlainString() + "×10^" + exponent;
    }

    private static String formatSmall(double value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.JAPAN);
        String pattern = value < 100 ? "#,##0.#" : "#,##0";
        DecimalFormat format = new DecimalFormat(pattern, symbols);
        format.setRoundingMode(RoundingMode.FLOOR);
        return format.format(value);
    }

    private static BigNum floorToSignificant(BigNum value, int digits) {
        double scale = Math.pow(10, digits - 1);
        // 1.2 が 1.19999… と表されていても 1.199 にならないよう、わずかに余裕を持たせる
        double mantissa = Math.floor(value.mantissa() * scale + 1e-9) / scale;
        return BigNum.of(mantissa, value.exponent());
    }
}
