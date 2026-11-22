package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.data.MineState;
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

    /** 例: {@code 石炭 1,234個 ｜ 毎秒 12.3個 ｜ 第2坑道 深さ200m 37/80m} */
    public static String actionBar(PlayerData data) {
        MineState mine = data.mine();
        return "石炭 " + JapaneseFormat.format(data.coal()) + "個"
                + " ｜ 毎秒 " + JapaneseFormat.format(coalPerSecond(data.tools(), mine.level())) + "個"
                + " ｜ " + tunnelName(mine.level()) + " 深さ" + depth(mine.level()) + "m "
                + progressText(mine.level(), mine.progress());
    }

    /** {@code /coal} の 1 行（見出しと値）。 */
    public record Entry(String label, String value) {
    }

    /** {@code /coal} で表示する自分の記録。 */
    public static List<Entry> record(PlayerData data) {
        Map<String, Long> tools = data.tools();
        MineState mine = data.mine();
        BigNum digPower = Formulas.digPower(tools);
        BigNum coalPower = Formulas.coalPower(tools);
        List<Entry> lines = new ArrayList<>();
        lines.add(new Entry("現在の石炭", JapaneseFormat.format(data.coal()) + "個"));
        lines.add(new Entry("累計採掘量", JapaneseFormat.format(data.totalMined()) + "個"));
        lines.add(new Entry("掘進力 D", JapaneseFormat.format(digPower)));
        lines.add(new Entry("採炭力 E", JapaneseFormat.format(coalPower)));
        lines.add(new Entry("掘進速度", JapaneseFormat.format(BigNum.of(Formulas.digSpeed(digPower))) + "m/秒"));
        lines.add(new Entry("毎秒の石炭", JapaneseFormat.format(coalPerSecond(tools, mine.level())) + "個"));
        lines.add(new Entry("今の坑道", tunnelName(mine.level()) + "・深さ" + depth(mine.level()) + "m・"
                + progressText(mine.level(), mine.progress())));
        lines.add(new Entry("最深の坑道", tunnelName(mine.maxReachedLevel())));
        for (Tool tool : Tool.values()) {
            lines.add(new Entry(tool.displayName(), String.format(Locale.ROOT, "%,d個", Formulas.count(tools, tool))));
        }
        return lines;
    }

    /** 放置で採れる毎秒の石炭 ＝ 掘進速度 × 1m あたりの石炭。 */
    public static BigNum coalPerSecond(Map<String, Long> tools, long level) {
        double speed = Formulas.digSpeed(Formulas.digPower(tools));
        if (!(speed > 0)) {
            return BigNum.ZERO;
        }
        return Formulas.coalPerMeter(Formulas.coalPower(tools), level).multiply(speed);
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
