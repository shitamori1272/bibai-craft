package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 累計採掘量の節目でもらえるトロフィー（企画書 4 章）。1 万・10 万・100 万…と、累計採掘量の桁が 1 つ増えるたびに 1 つもらえ、
 * 1 つにつき毎秒の石炭と手掘りが {@link #BONUS_PER_TROPHY}（+5%）増える（足し算。3 つなら ×1.15）。
 *
 * <p>もらったトロフィーは改良と同じく {@code upgrades} に ID {@code trophy_<桁>}（例: 1 万 ＝ {@code trophy_4}）で入れる。
 * 買った改良と一緒に保存・リセットされ、倍率は {@link Formulas#globalMultiplier} から全部の計算にかかる。
 * 改良の一覧（{@link Upgrade#values()}）には含まれない。
 *
 * <p>節目は 10^{@value #MIN_DIGITS}〜10^{@value #MAX_DIGITS}（1 万〜1,000 無量大数）。Bukkit に依存しない。
 */
public final class Trophy {

    /** 最初のトロフィーの桁（10^4 ＝ 1 万）。 */
    public static final int MIN_DIGITS = 4;
    /** 最後のトロフィーの桁（10^71 ＝ 1,000 無量大数。日本の単位で表せる最大）。 */
    public static final int MAX_DIGITS = 71;
    /** トロフィー 1 つで増える生産の割合。 */
    public static final double BONUS_PER_TROPHY = 0.05;

    private static final String PREFIX = "trophy_";

    private Trophy() {
    }

    /** 桁 digits の節目のトロフィーの ID（例: 4 → {@code trophy_4}）。 */
    public static String id(int digits) {
        if (digits < MIN_DIGITS || digits > MAX_DIGITS) {
            throw new IllegalArgumentException("桁は " + MIN_DIGITS + "〜" + MAX_DIGITS + " にしてください: " + digits);
        }
        return PREFIX + digits;
    }

    /** トロフィーの ID か（範囲の外の桁は違うとみなす）。 */
    public static boolean isId(String id) {
        if (id == null || !id.startsWith(PREFIX)) {
            return false;
        }
        String digits = id.substring(PREFIX.length());
        if (digits.isEmpty() || digits.length() > 2 || !digits.chars().allMatch(Character::isDigit)
                || (digits.length() > 1 && digits.charAt(0) == '0')) {
            return false;
        }
        int n = Integer.parseInt(digits);
        return n >= MIN_DIGITS && n <= MAX_DIGITS;
    }

    /** もらったトロフィーの数。 */
    public static int count(Set<String> upgrades) {
        int n = 0;
        for (String id : upgrades) {
            if (isId(id)) {
                n++;
            }
        }
        return n;
    }

    /** トロフィーによる生産の倍率 ＝ 1 ＋ 0.05 × トロフィーの数。 */
    public static double multiplier(Set<String> upgrades) {
        return 1 + BONUS_PER_TROPHY * count(upgrades);
    }

    /**
     * 累計採掘量 totalMined で届いている節目のうち、まだもらっていないものの桁（小さい順）。
     */
    public static List<Integer> newlyReached(BigNum totalMined, Set<String> upgrades) {
        List<Integer> list = new ArrayList<>();
        if (totalMined.signum() <= 0) {
            return list;
        }
        // 仮数は 1 以上 10 未満に正規化されているので、指数がそのまま桁（10 の何乗以上か）になる
        long reached = Math.min(totalMined.exponent(), MAX_DIGITS);
        for (int digits = MIN_DIGITS; digits <= reached; digits++) {
            if (!upgrades.contains(id(digits))) {
                list.add(digits);
            }
        }
        return list;
    }

    /** 節目の名前（例: 4 → {@code 1万個}、9 → {@code 10億個}）。 */
    public static String milestoneName(int digits) {
        return JapaneseFormat.format(BigNum.of(1, digits)) + "個";
    }
}
