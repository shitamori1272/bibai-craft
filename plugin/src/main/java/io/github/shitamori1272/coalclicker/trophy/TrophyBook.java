package io.github.shitamori1272.coalclicker.trophy;

import io.github.shitamori1272.coalclicker.game.Trophy;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * トロフィーの一覧画面（6 段・54 マス）のマスの割り当てと文言（{@code /coal trophy}）。Bukkit に依存しない。
 *
 * <pre>
 * [ト][ト][ト][ト][ト][ト][ト][ト][ト]   1〜5 段目：トロフィー（1 ページ 45 個。節目の小さい順）
 *  …
 * [前] 46 47 48 [まとめ] 50 51 52 [次]   6 段目：ページ送り・獲得数と生産の倍率
 * </pre>
 */
public final class TrophyBook {

    public static final int ROWS = 6;
    public static final int SIZE = ROWS * 9;
    public static final int PER_PAGE = 45;
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int SUMMARY_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 53;

    /** トロフィーの総数。 */
    public static final int TOTAL = Trophy.MAX_DIGITS - Trophy.MIN_DIGITS + 1;

    /** トロフィーのマスの状態。 */
    public enum State {
        /** もらった。 */
        EARNED,
        /** まだで、次にもらえる（残りの量を出す）。 */
        NEXT,
        /** まだ。 */
        LOCKED
    }

    /** 1 マス分の中身。 */
    public record Entry(int digits, State state, String name, List<String> lore) {
    }

    private TrophyBook() {
    }

    /** ページの数。 */
    public static int pageCount() {
        return (TOTAL + PER_PAGE - 1) / PER_PAGE;
    }

    /** ページを 0〜最後のページに収める。 */
    public static int clampPage(int page) {
        return Math.clamp(page, 0, pageCount() - 1);
    }

    /** そのマスのトロフィーの節目の桁。トロフィーのマスでない・その先にトロフィーがなければ空。 */
    public static OptionalInt digitsAt(int page, int slot) {
        if (slot < 0 || slot >= PER_PAGE) {
            return OptionalInt.empty();
        }
        int digits = Trophy.MIN_DIGITS + clampPage(page) * PER_PAGE + slot;
        return digits <= Trophy.MAX_DIGITS ? OptionalInt.of(digits) : OptionalInt.empty();
    }

    /** まだもらっていないうち、いちばん小さい節目の桁。全部もらっていれば空。 */
    public static OptionalInt nextDigits(Set<String> upgrades) {
        for (int digits = Trophy.MIN_DIGITS; digits <= Trophy.MAX_DIGITS; digits++) {
            if (!upgrades.contains(Trophy.id(digits))) {
                return OptionalInt.of(digits);
            }
        }
        return OptionalInt.empty();
    }

    /** ページ page のトロフィーのマス（マスの番号の順。先頭はマス 0）。 */
    public static List<Entry> entries(int page, Set<String> upgrades, BigNum totalMined) {
        OptionalInt next = nextDigits(upgrades);
        List<Entry> list = new ArrayList<>();
        for (int slot = 0; slot < PER_PAGE; slot++) {
            OptionalInt digits = digitsAt(page, slot);
            if (digits.isEmpty()) {
                break;
            }
            list.add(entry(digits.getAsInt(), upgrades, totalMined, next));
        }
        return list;
    }

    static Entry entry(int digits, Set<String> upgrades, BigNum totalMined, OptionalInt next) {
        String milestone = Trophy.milestoneName(digits);
        String bonus = "生産 +" + percent(1) + "%";
        if (upgrades.contains(Trophy.id(digits))) {
            return new Entry(digits, State.EARNED, "🏆 " + milestone + " 突破",
                    List.of("累計採掘量 " + milestone + " で獲得", bonus));
        }
        String name = "？ " + milestone + " 突破";
        if (next.isPresent() && next.getAsInt() == digits) {
            BigNum rest = BigNum.of(1, digits).subtract(totalMined);
            // 引き算の丸めで 70万 − ε が「69.99万」と切り捨て表示されないよう、ごくわずかに切り上げる
            String restText = JapaneseFormat.format(rest.signum() > 0 ? rest.multiply(1 + 1e-9) : BigNum.ZERO) + "個";
            return new Entry(digits, State.NEXT, name,
                    List.of("未獲得", "あと " + restText, "獲得すると " + bonus));
        }
        return new Entry(digits, State.LOCKED, name, List.of("未獲得"));
    }

    /** まとめの見出し（例: {@code トロフィー 3/68 ・ 生産 +15%}）。 */
    public static String summary(Set<String> upgrades) {
        int count = Trophy.count(upgrades);
        return "トロフィー " + count + "/" + TOTAL + " ・ 生産 +" + percent(count) + "%";
    }

    private static long percent(int count) {
        return Math.round(Trophy.BONUS_PER_TROPHY * 100 * count);
    }
}
