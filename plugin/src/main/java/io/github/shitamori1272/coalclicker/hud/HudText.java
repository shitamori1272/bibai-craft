package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Trophy;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        return "石炭 " + coal(data.coal())
                + " ｜ 毎秒 " + coal(Formulas.coalPerSecond(data.tools(), data.upgrades(), level))
                + " ｜ " + tunnelName(level) + " 深さ" + depth(level) + "m "
                + progressText(level, Formulas.progressOf(data.totalMined()))
                + (awaitingCage ? CAGE_HINT : "");
    }

    /** {@code /coal} の 1 行（見出しと値）。 */
    public record Entry(String label, String value) {
    }

    /**
     * {@code /coal} で表示する自分の記録（企画書 4 章・7 章）。
     *
     * <p>並びは、今の石炭・累計採掘量・毎秒の石炭・手掘り 1 回・手掘りの回数・今の坑道・最深の坑道、
     * 道具 8 種（所持数とその道具の毎秒の石炭）、改良（買った数 / 全部の数）。
     * 最深の坑道は「到達ボーナス済みの最深」と「今の階層」の深いほう（ランキングの到達深さと同じ）。
     */
    public static List<Entry> record(PlayerData data) {
        Map<String, Long> tools = data.tools();
        Set<String> upgrades = data.upgrades();
        BigNum totalMined = data.totalMined();
        long level = Formulas.levelOf(totalMined);
        BigNum perSecond = Formulas.coalPerSecond(tools, upgrades, level);
        List<Entry> lines = new ArrayList<>();
        lines.add(new Entry("現在の石炭", coal(data.coal())));
        lines.add(new Entry("累計採掘量", coal(totalMined)));
        lines.add(new Entry("毎秒の石炭", coal(perSecond)));
        lines.add(new Entry("手掘り 1 回", coal(Formulas.clickCoal(upgrades, perSecond))));
        lines.add(new Entry("手掘りの回数", String.format(Locale.ROOT, "%,d回", data.clicks())));
        lines.add(new Entry("今の坑道", tunnelName(level) + "・深さ" + depth(level) + "m・"
                + progressText(level, Formulas.progressOf(totalMined))
                + "・次の坑道まであと" + coal(untilNextLevel(totalMined, level))));
        long deepest = Math.max(data.mine().maxReachedLevel(), level);
        lines.add(new Entry("最深の坑道", tunnelName(deepest) + "（深さ" + depth(deepest) + "m）"));
        for (Tool tool : Tool.values()) {
            lines.add(new Entry(tool.displayName(),
                    String.format(Locale.ROOT, "%,d個", Formulas.count(tools, tool))
                            + "（毎秒 " + coal(Formulas.toolCoalPerSecond(tool, tools, upgrades, level)) + "）"));
        }
        lines.add(new Entry("改良", upgradeCount(upgrades) + "/" + Upgrade.values().size()));
        int trophies = Trophy.count(upgrades);
        lines.add(new Entry("トロフィー", trophies + "個（生産 +"
                + Math.round(Trophy.BONUS_PER_TROPHY * 100 * trophies) + "%）"));
        return lines;
    }

    /** 次の坑道に進むまでに掘る残りの石炭 ＝ 今の階層を掘り切るまでの累計 − 累計採掘量（0 未満にはしない）。 */
    static BigNum untilNextLevel(BigNum totalMined, long level) {
        BigNum rest = Formulas.cumulativeToFinish(level).subtract(totalMined);
        return rest.signum() > 0 ? rest : BigNum.ZERO;
    }

    /** 買った改良のうち、知っている ID の数。 */
    private static int upgradeCount(Set<String> upgrades) {
        int n = 0;
        for (Upgrade upgrade : Upgrade.values()) {
            if (upgrades.contains(upgrade.id())) {
                n++;
            }
        }
        return n;
    }

    private static String coal(BigNum value) {
        return JapaneseFormat.format(value) + "個";
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
