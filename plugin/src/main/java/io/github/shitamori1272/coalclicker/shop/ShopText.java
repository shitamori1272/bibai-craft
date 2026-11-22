package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.GameState;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * ショップの説明文（lore）の組み立て。Bukkit・Adventure に依存しないので単体テストできる。
 * 色は {@link Tone} で持ち、画面側（{@link ShopMenu}）で実際の色に置き換える。
 */
public final class ShopText {

    /** 改良の解説を折り返す文字数。 */
    static final int WRAP_WIDTH = 20;

    private ShopText() {
    }

    /** 文字の役割。画面側で色に置き換える。 */
    public enum Tone {
        /** 「価格：」などの見出し（灰色）。 */
        LABEL,
        /** ふつうの値（白）。 */
        VALUE,
        /** 買える価格（黄）。 */
        AFFORDABLE,
        /** 足りない価格（赤）。 */
        UNAFFORDABLE,
        /** 増える量・効果（緑）。 */
        GAIN,
        /** 解説（灰色）。 */
        DESCRIPTION,
        /** 操作の案内（黄）。 */
        HINT,
        /** 目立たない補足（濃い灰色）。 */
        MUTED
    }

    /** 色付きの文字のかたまり。 */
    public record Segment(String text, Tone tone) {
    }

    /** 説明文の 1 行。空なら空行。 */
    public record Line(List<Segment> segments) {

        public Line {
            segments = List.copyOf(segments);
        }

        static Line empty() {
            return new Line(List.of());
        }

        static Line of(String text, Tone tone) {
            return new Line(List.of(new Segment(text, tone)));
        }

        static Line of(String label, String value, Tone tone) {
            return new Line(List.of(new Segment(label, Tone.LABEL), new Segment(value, tone)));
        }

        /** 色を除いた文字だけ（テスト用）。 */
        public String plain() {
            StringBuilder sb = new StringBuilder();
            for (Segment segment : segments) {
                sb.append(segment.text());
            }
            return sb.toString();
        }
    }

    // ---- 道具 ----

    /**
     * 道具の説明文：価格（足りなければ赤）・所持数・1 つ買うと毎秒 +◯個・この道具の合計（全体の ◯%）・短い解説・操作。
     *
     * @param coal 今の石炭の残高
     */
    public static List<Line> toolLore(Tool tool, GameState state, BigNum coal) {
        long owned = state.toolCount(tool);
        long level = Formulas.levelOf(state.totalMined());
        BigNum price = Formulas.price(tool, owned);
        boolean affordable = coal.isGreaterThanOrEqual(price);
        BigNum toolRate = Formulas.toolCoalPerSecond(tool, state.tools(), state.upgrades(), level);
        BigNum totalRate = Formulas.coalPerSecond(state.tools(), state.upgrades(), level);

        List<Line> lore = new ArrayList<>();
        lore.add(Line.of("価格：", JapaneseFormat.format(price) + " 個",
                affordable ? Tone.AFFORDABLE : Tone.UNAFFORDABLE));
        lore.add(Line.of("所持数：", JapaneseFormat.format(BigNum.of(owned)) + " 個", Tone.VALUE));
        lore.add(Line.of("1 つ買うと毎秒 ",
                "+" + JapaneseFormat.format(Formulas.marginalCoalPerSecond(tool, state.upgrades(), level)) + " 個",
                Tone.GAIN));
        lore.add(Line.of("この道具の合計：", "毎秒 " + JapaneseFormat.format(toolRate) + " 個・全体の "
                + percent(toolRate, totalRate), Tone.VALUE));
        lore.add(Line.empty());
        for (String row : wrap(tool.shortDescription(), WRAP_WIDTH)) {
            lore.add(Line.of(row, Tone.DESCRIPTION));
        }
        lore.add(Line.empty());
        lore.add(Line.of("クリックで 1 つ購入", Tone.HINT));
        long bulk = Purchase.affordableCount(coal, tool, owned, ShopMenu.BULK_AMOUNT);
        String bulkNote = bulk >= 1
                ? "（今なら " + bulk + " 個・" + JapaneseFormat.format(Formulas.totalPrice(tool, owned, bulk)) + " 個）"
                : "";
        lore.add(Line.of("Shift+クリックで買えるだけ（最大 " + ShopMenu.BULK_AMOUNT + " 個）" + bulkNote, Tone.MUTED));
        return lore;
    }

    /**
     * 全体に占める割合の表示（例: {@code 12.3%}）。小数第 1 位で切り捨てる。
     * 全体が 0 なら {@code 0%}、0 より大きいが 0.1% に届かなければ {@code 0.1% 未満}。
     */
    public static String percent(BigNum part, BigNum total) {
        if (part.signum() <= 0 || total.signum() <= 0) {
            return "0%";
        }
        if (part.compareTo(total) >= 0) {
            return "100%";
        }
        double ratio = part.divide(total).toDouble() * 100;
        // 切り捨て（浮動小数の誤差で 12.3 が 12.299… にならないよう少し足す）
        double shown = Math.floor(ratio * 10 + 1e-9) / 10;
        if (shown <= 0) {
            return "0.1% 未満";
        }
        if (shown == Math.rint(shown)) {
            return String.format(Locale.ROOT, "%.0f%%", shown);
        }
        return String.format(Locale.ROOT, "%.1f%%", shown);
    }

    // ---- 改良 ----

    /** 買える改良（出現していて、まだ買っていない）を価格の安い順に。同じ価格なら定義の順。 */
    public static List<Upgrade> availableByPrice(GameState state) {
        List<Upgrade> list = new ArrayList<>(Upgrade.available(state));
        list.sort(Comparator.comparing(Upgrade::price));
        return list;
    }

    /** 改良の説明文：効果・価格（足りなければ赤）・短い解説・操作。 */
    public static List<Line> upgradeLore(Upgrade upgrade, BigNum coal) {
        boolean affordable = coal.isGreaterThanOrEqual(upgrade.price());
        List<Line> lore = new ArrayList<>();
        lore.add(Line.of("効果：", upgrade.effectDescription(), Tone.GAIN));
        lore.add(Line.of("価格：", JapaneseFormat.format(upgrade.price()) + " 個",
                affordable ? Tone.AFFORDABLE : Tone.UNAFFORDABLE));
        lore.add(Line.empty());
        for (String row : wrap(upgrade.explanation(), WRAP_WIDTH)) {
            lore.add(Line.of(row, Tone.DESCRIPTION));
        }
        lore.add(Line.empty());
        lore.add(Line.of("クリックで購入", Tone.HINT));
        return lore;
    }

    /** 改良の見出しの説明文（買える数とページ）。 */
    public static List<Line> upgradeHeaderLore(int count, int page) {
        return upgradeHeaderLore(count, page, count);
    }

    /**
     * 改良の見出しの説明文。
     *
     * @param count 買える改良の数
     * @param page ページ（0 始まり）
     * @param slotsUsed 並んでいるマスの数（購入済みのマスも含む。ページ数はこれで決まる）
     */
    public static List<Line> upgradeHeaderLore(int count, int page, int slotsUsed) {
        List<Line> lore = new ArrayList<>();
        if (count == 0) {
            lore.add(Line.of("まだ買える改良はありません", Tone.DESCRIPTION));
            lore.add(Line.of("道具を買ったり手掘りしたりすると現れます", Tone.MUTED));
            return lore;
        }
        lore.add(Line.of("買える改良：", count + " 個", Tone.VALUE));
        int pages = ShopLayout.pageCount(slotsUsed);
        if (pages > 1) {
            lore.add(Line.of("ページ：", (ShopLayout.clampPage(page, slotsUsed) + 1) + " / " + pages, Tone.VALUE));
        }
        lore.add(Line.of("下に安い順に並んでいます（開き直すと並べ直します）", Tone.MUTED));
        return lore;
    }

    /**
     * まだ出現していない改良のうち、いちばん安いものについてのヒント（例: 「安全灯をあと 4 個で次の改良」）。
     * 出現していない改良がもうなければ空。
     */
    public static Optional<String> nextLockedHint(GameState state) {
        return nextLocked(state).map(upgrade -> lockedHint(upgrade, state));
    }

    /** まだ出現していない（買ってもいない）改良のうち、いちばん安いもの。 */
    static Optional<Upgrade> nextLocked(GameState state) {
        return Upgrade.values().stream()
                .filter(upgrade -> !upgrade.isUnlocked(state) && !upgrade.isOwned(state))
                .min(Comparator.comparing(Upgrade::price));
    }

    static String lockedHint(Upgrade upgrade, GameState state) {
        return switch (upgrade.kind()) {
            case TOOL -> {
                Tool tool = upgrade.tool().orElseThrow();
                BigNum rest = upgrade.threshold().subtract(BigNum.of(state.toolCount(tool)));
                yield tool.displayName() + "をあと " + JapaneseFormat.format(rest) + " 個で次の改良";
            }
            case PICKAXE -> "手掘りをあと " + JapaneseFormat.format(upgrade.threshold().subtract(BigNum.of(state.clicks())))
                    + " 回で次の改良";
            case SKILL -> "累計採掘量があと " + JapaneseFormat.format(upgrade.threshold().subtract(state.totalMined()))
                    + " 個で次の改良";
        };
    }

    // ---- 共通 ----

    /** 長い文を {@code width} 文字ごとに折り返す。句読点で区切れるところがあれば、そこで折る。 */
    static List<String> wrap(String text, int width) {
        List<String> rows = new ArrayList<>();
        String rest = text.strip();
        while (rest.length() > width) {
            int cut = -1;
            // 幅の半分より後ろにある最後の句読点の直後で折る
            for (int i = width - 1; i >= width / 2; i--) {
                char c = rest.charAt(i);
                if (c == '。' || c == '、' || c == ' ') {
                    cut = i + 1;
                    break;
                }
            }
            if (cut < 0) {
                cut = width;
            }
            rows.add(rest.substring(0, cut).strip());
            rest = rest.substring(cut).strip();
        }
        if (!rest.isEmpty()) {
            rows.add(rest);
        }
        return rows;
    }
}
