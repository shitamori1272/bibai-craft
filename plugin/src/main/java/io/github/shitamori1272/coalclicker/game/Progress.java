package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;

/**
 * 自動の掘進を時間ぶん進める計算（企画書 4-1・5-5）。オンライン中の 1 秒ごとの処理と、オフライン中の
 * まとめての処理の両方で使う。
 *
 * <ul>
 *   <li>進み具合は割合（0〜1）で持ち、その階層の長さで止まる
 *   <li>長さに達したら階層を +1 し、次の階層は 0m から掘り始める
 *   <li>1 階層にかかる時間は最短 10 秒
 *   <li>採れる石炭 ＝ 掘った m × その階層の 1m あたりの石炭
 * </ul>
 *
 * <p><b>超えた分を捨てる</b>：処理は {@link #TICK_SECONDS} 秒ごとの区切り（tick）で進む前提とし、
 * 階層を掘り切った tick の残り時間は捨てて、次の階層は次の tick の頭から掘り始める。
 * こうしておくと、1 秒ずつ N 回進めても、N 秒を一度に進めても同じ結果になる。
 *
 * <p>1 階層は最短 10 秒なので、ループは「経過秒数 ÷ 10 ＋ 2」回以内で終わる（8 時間で 2,882 回以内）。
 * 呼び出し側はオフライン時間に上限を設けること。
 *
 * <p>極端に深い階層（坑道が長く、1 回の増分が割合の精度より小さい）では、止まらないよう進み具合を
 * double の最小単位ずつ進める。このとき進み具合は飛び飛びになり、正確な値より少し速く進む。
 * 階層番号は {@link Long#MAX_VALUE} で頭打ちになり、その階層は掘り切れない（進み具合は 1 未満で止まる）。
 */
public final class Progress {

    /** 掘進の処理の区切り（秒）。オンライン中のタスクの周期に合わせる。 */
    public static final double TICK_SECONDS = 1;

    /** tick の境目ちょうどで掘り切ったときに、丸め誤差で次の tick まで捨てないための余裕。 */
    private static final double TICK_EPSILON = 1e-9;

    /** 1 未満で最大の進み具合。最後の階層（{@link Long#MAX_VALUE}）はここで止まる。 */
    private static final double LAST_PROGRESS = Math.nextDown(1.0);

    private Progress() {
    }

    /**
     * 進めた結果。
     *
     * @param level 進めたあとの階層番号
     * @param progress 進めたあとの進み具合（0 以上 1 未満の割合）
     * @param coal この間に採れた石炭
     * @param levelsCompleted この間に掘り切った階層の数
     * @param meters この間に掘った長さ（m。捨てた分は含まない）
     */
    public record Result(long level, double progress, BigNum coal, long levelsCompleted, double meters) {
    }

    /** {@link #TICK_SECONDS} 秒ごとの区切りで {@code seconds} 秒ぶん進める。 */
    public static Result advance(long level, double progress, BigNum digPower, BigNum coalPower, double seconds) {
        return advance(level, progress, digPower, coalPower, seconds, TICK_SECONDS);
    }

    /**
     * {@code tickSeconds} 秒ごとの区切りで {@code seconds} 秒ぶん進める。
     *
     * @param level 今の階層番号（0 始まり）
     * @param progress 今の進み具合（0〜1 の割合。範囲外は丸める）
     * @param digPower 掘進力 D。0 なら進まない
     * @param coalPower 採炭力 E
     * @param seconds 経過時間（秒）
     * @param tickSeconds 区切りの長さ（秒）。0 以下なら残り時間を捨てずに次の階層へ持ち越す
     */
    public static Result advance(long level, double progress, BigNum digPower, BigNum coalPower,
            double seconds, double tickSeconds) {
        if (level < 0) {
            throw new IllegalArgumentException("階層番号は 0 以上にしてください: " + level);
        }
        if (Double.isNaN(progress) || Double.isNaN(seconds) || Double.isNaN(tickSeconds)) {
            throw new IllegalArgumentException("NaN は受け付けません");
        }
        if (Double.isInfinite(seconds)) {
            throw new IllegalArgumentException("経過時間は有限にしてください");
        }
        double p = Math.clamp(progress, 0.0, 1.0);
        long lvl = level;
        long completed = 0;

        // 保存データなどで掘り切ったまま残っていた場合は、時間を使わずに次の階層へ進める
        if (p >= 1) {
            if (lvl == Long.MAX_VALUE) {
                p = LAST_PROGRESS;
            } else {
                lvl++;
                completed++;
                p = 0;
            }
        }

        double speed = Formulas.digSpeed(digPower);
        if (!(seconds > 0) || !(speed > 0)) {
            return new Result(lvl, p, BigNum.ZERO, completed, 0);
        }

        double elapsed = 0;
        double meters = 0;
        BigNum coal = BigNum.ZERO;

        while (elapsed < seconds) {
            double length = Formulas.tunnelLength(lvl);
            double levelSeconds = Formulas.levelSecondsAtSpeed(lvl, speed);
            BigNum perMeter = Formulas.coalPerMeter(coalPower, lvl);
            double remaining = seconds - elapsed;
            double timeToFinish = (1 - p) * levelSeconds;

            if (remaining < timeToFinish) {
                // この階層の途中で時間切れ
                double dp = remaining / levelSeconds;
                double dug = dp * length;
                double next = p + dp;
                if (next <= p) {
                    // とても深い階層では増分が割合の精度より小さい。止まらないよう最小単位だけ進める
                    next = Math.nextUp(p);
                }
                if (next < 1) {
                    meters += dug;
                    coal = coal.add(perMeter.multiply(dug));
                    p = next;
                    break;
                }
                // 丸めで 1 に届いたら、掘り切ったものとして扱う（下へ続く）
            }

            // 掘り切った。残りの長さぶんだけ石炭をもらい、超えた分は捨てる
            double dug = (1 - p) * length;
            meters += dug;
            coal = coal.add(perMeter.multiply(dug));
            if (lvl == Long.MAX_VALUE) {
                // 最後の階層は掘り切れない（番号があふれないよう、ここで止める）
                p = LAST_PROGRESS;
                break;
            }
            elapsed += Math.min(timeToFinish, remaining);
            if (tickSeconds > 0) {
                elapsed = Math.ceil(elapsed / tickSeconds - TICK_EPSILON) * tickSeconds;
            }
            lvl++;
            completed++;
            p = 0;
        }
        return new Result(lvl, p, coal, completed, meters);
    }
}
