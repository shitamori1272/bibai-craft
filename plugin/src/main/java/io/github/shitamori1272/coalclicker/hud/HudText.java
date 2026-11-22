package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * アクションバーと {@code /coal} に出す文字列を作る（企画書 7 章）。Bukkit に依存しない。
 *
 * <p>どれだけ大きな値でもあふれないよう、石炭は {@link JapaneseFormat}、坑道の番号と深さは {@link BigInteger}、
 * 坑道の m 数は切り捨ててから {@link JapaneseFormat} で表す。
 */
public final class HudText {

    /** 1 階層あたりの深さ（m）。第 N 坑道の深さ ＝ N × 100m。 */
    public static final int DEPTH_PER_LEVEL = 100;

    private HudText() {
    }

    /** ケージで降りられるときにアクションバーの後ろに付ける一言（#22）。 */
    public static final String CAGE_HINT = " ｜ ケージで降りよう";

    /** 例: {@code 石炭 1,234個 ｜ 毎秒 12.3個 ｜ 第2坑道 深さ200m 37/80m} */
    public static String actionBar(PlayerData data) {
        return actionBar(data, false);
    }

    /**
     * アクションバーの文字列。坑道・深さはいつも数値上の階層で出す。
     * 置いてある坑道より数値上の階層が進んでいる（awaitingCage）ときは {@link #CAGE_HINT} を付ける。
     */
    public static String actionBar(PlayerData data, boolean awaitingCage) {
        long level = Formulas.levelOf(data.totalMined());
        return "石炭 " + JapaneseFormat.format(data.coal()) + "個"
                + " ｜ 毎秒 " + JapaneseFormat.format(Formulas.coalPerSecond(data)) + "個"
                + " ｜ " + tunnelName(level) + " 深さ" + depth(level) + "m "
                + progressText(level, Formulas.progressOf(data.totalMined()))
                + (awaitingCage ? CAGE_HINT : "");
    }

    /** {@code /coal} の 1 行（見出しと値）。 */
    public record Entry(String label, String value) {
    }

    /** {@code /coal} で表示する自分の記録。TODO(#62): 表示の作り直し。 */
    public static List<Entry> record(PlayerData data) {
        Map<String, Long> tools = data.tools();
        long level = Formulas.levelOf(data.totalMined());
        List<Entry> lines = new ArrayList<>();
        lines.add(new Entry("現在の石炭", JapaneseFormat.format(data.coal()) + "個"));
        lines.add(new Entry("累計採掘量", JapaneseFormat.format(data.totalMined()) + "個"));
        lines.add(new Entry("毎秒の石炭", JapaneseFormat.format(Formulas.coalPerSecond(data)) + "個"));
        lines.add(new Entry("手掘り 1 回", JapaneseFormat.format(Formulas.clickCoal(data)) + "個"));
        lines.add(new Entry("今の坑道", tunnelName(level) + "・深さ" + depth(level) + "m・"
                + progressText(level, Formulas.progressOf(data.totalMined()))));
        lines.add(new Entry("最深の坑道", tunnelName(data.mine().maxReachedLevel())));
        for (Tool tool : Tool.values()) {
            lines.add(new Entry(tool.displayName(), String.format(Locale.ROOT, "%,d個", Formulas.count(tools, tool))));
        }
        lines.add(new Entry("改良", String.format(Locale.ROOT, "%,d個", data.upgrades().size())));
        return lines;
    }

    /** 例: 階層 1 → {@code 第2坑道}。 */
    public static String tunnelName(long level) {
        return "第" + String.format(Locale.ROOT, "%,d", number(level)) + "坑道";
    }

    /** 例: 階層 1 → {@code 200}。 */
    public static String depth(long level) {
        return String.format(Locale.ROOT, "%,d", number(level).multiply(BigInteger.valueOf(DEPTH_PER_LEVEL)));
    }

    /** 例: {@code 37/80m}。進んだ m は切り捨てる。 */
    static String progressText(long level, double progress) {
        double length = Formulas.tunnelLength(level);
        return meters(progress * length) + "/" + meters(length) + "m";
    }

    private static String meters(double value) {
        return JapaneseFormat.format(BigNum.of(Math.floor(value)));
    }

    /** 0 始まりの階層を 1 始まりの番号にする（Long.MAX_VALUE でもあふれない）。 */
    private static BigInteger number(long level) {
        return BigInteger.valueOf(level).add(BigInteger.ONE);
    }
}
