package io.github.shitamori1272.coalclicker.level;

import io.github.shitamori1272.coalclicker.hud.HudText;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ケージへの誘導（企画書 5-5、#22）と到達ボーナス（#24）の判定と文字列。Bukkit に依存しない。
 */
public final class LevelGuide {

    /** 道しるべのパーティクルを、プレイヤーの何ブロック先から出すか。 */
    static final double GUIDE_START = 1.0;
    /** 道しるべのパーティクルの間隔（ブロック）。 */
    static final double GUIDE_STEP = 1.5;
    /** 道しるべのパーティクルの最大の数。 */
    static final int GUIDE_MAX_POINTS = 8;

    private LevelGuide() {
    }

    /** 数値上の階層が、置いてある坑道より進んでいる（ケージで降りられる）か。 */
    public static boolean awaitingCage(long numericLevel, long renderedLevel) {
        return numericLevel > renderedLevel;
    }

    /** 例: 階層 2 → {@code 第3坑道まで掘り進んでいます。ケージで降りよう}。 */
    public static String bossBarText(long numericLevel) {
        return HudText.tunnelName(numericLevel) + "まで掘り進んでいます。ケージで降りよう";
    }

    /** 降りたときのタイトル。例: {@code 第3坑道}。 */
    public static String descentTitle(long level) {
        return HudText.tunnelName(level);
    }

    /** 降りたときのサブタイトル。例: 階層 2 → {@code 深さ300m}。 */
    public static String descentSubtitle(long level) {
        return "深さ" + HudText.depth(level) + "m";
    }

    /** 掘り切った切羽の看板の 4 行。 */
    public static List<String> endSignLines(long numericLevel) {
        return List.of("掘り切りました", HudText.tunnelName(numericLevel) + "へは", "坑底のケージで", "降りよう");
    }

    /** 到達ボーナスのチャット。例: {@code 到達ボーナス +2,640個（1階層ぶん）}。 */
    public static String bonusMessage(ArrivalBonus.Result result) {
        return "到達ボーナス +" + JapaneseFormat.format(result.coal()) + "個（"
                + String.format(Locale.ROOT, "%,d", result.levels()) + "階層ぶん）";
    }

    /**
     * 道しるべのパーティクルを出す X 座標。プレイヤー（X = playerX）から坑底のケージ（X = targetX）の方へ、
     * 少し先から一定の間隔で並べる。ケージのそばにいるときは、ケージの手前に 1 つだけ出す。
     */
    public static List<Double> guidePointXs(double playerX, int targetX) {
        double stop = targetX + 0.5;
        List<Double> xs = new ArrayList<>();
        for (int i = 0; i < GUIDE_MAX_POINTS; i++) {
            double x = playerX - GUIDE_START - GUIDE_STEP * i;
            if (x < stop) {
                break;
            }
            xs.add(x);
        }
        if (xs.isEmpty()) {
            xs.add(stop + 1);
        }
        return xs;
    }
}
