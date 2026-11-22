package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Map;

/**
 * ゲームの計算式（企画書 4 章・5-5・6 章）。
 *
 * <p>階層番号 {@code level} は 0 始まり（第 1 坑道 = 0）。大きくなりうる値は {@link BigNum} で扱う。
 * 道具の所持数は {@code Map<道具ID, 個数>} で受け取り、知らない ID は無視する。
 */
public final class Formulas {

    /** 採炭力 E の初期値。 */
    public static final double INITIAL_COAL_POWER = 100;
    /** 掘進速度の係数（m/秒）。速度 = この値 × √D。 */
    public static final double DIG_SPEED_FACTOR = 0.1;
    /** 深さ倍率の底。 */
    public static final double DEPTH_BASE = 1.1;
    /** 第 1 坑道の長さ（m）。 */
    public static final double BASE_TUNNEL_LENGTH = 64;
    /** 1 階層ごとに伸びる坑道の長さ（m）。 */
    public static final double TUNNEL_LENGTH_STEP = 16;
    /** 1 階層にかかる最短の時間（秒）。安全装置。 */
    public static final double MIN_LEVEL_SECONDS = 10;
    /** 到達ボーナスで何秒分の石炭を渡すか。 */
    public static final double ARRIVAL_BONUS_SECONDS = 60;

    private static final double LOG10_DIG_SPEED_FACTOR = Math.log10(DIG_SPEED_FACTOR);

    private Formulas() {
    }

    // ---- 価格 ----

    /** 次の 1 つの価格 ＝ 初期価格 × 倍率^所持数。 */
    public static BigNum price(Tool tool, long ownedCount) {
        requireNonNegative(ownedCount, "所持数");
        return tool.basePrice().multiply(BigNum.pow(tool.priceGrowth(), ownedCount));
    }

    /**
     * 所持数 {@code owned} から {@code amount} 個まとめて買う価格。
     * 等比数列の和 price(owned) × (g^amount − 1) ÷ (g − 1) で一度に求める。
     */
    public static BigNum totalPrice(Tool tool, long owned, long amount) {
        requireNonNegative(amount, "個数");
        if (amount == 0) {
            return BigNum.ZERO;
        }
        double g = tool.priceGrowth();
        if (g == 1) {
            return price(tool, owned).multiply(BigNum.of(amount));
        }
        BigNum series = BigNum.pow(g, amount).subtract(BigNum.ONE).divide(g - 1);
        return price(tool, owned).multiply(series);
    }

    // ---- ステータス ----

    /** 掘進力 D ＝ 掘進の道具の効果 × 所持数 の合計（初期値 0）。 */
    public static BigNum digPower(Map<String, Long> tools) {
        return sumEffects(tools, ToolKind.DIG);
    }

    /** 採炭力 E ＝ 100 ＋ 採炭の道具の効果 × 所持数 の合計。 */
    public static BigNum coalPower(Map<String, Long> tools) {
        return BigNum.of(INITIAL_COAL_POWER).add(sumEffects(tools, ToolKind.COAL));
    }

    /** 道具の所持数。持っていなければ 0。 */
    public static long count(Map<String, Long> tools, Tool tool) {
        Long n = tools.get(tool.id());
        return n == null ? 0 : n;
    }

    private static BigNum sumEffects(Map<String, Long> tools, ToolKind kind) {
        BigNum sum = BigNum.ZERO;
        for (Tool tool : Tool.values()) {
            if (tool.kind() != kind) {
                continue;
            }
            long n = count(tools, tool);
            requireNonNegative(n, tool.id() + " の所持数");
            if (n > 0) {
                sum = sum.add(BigNum.of(tool.effect()).multiply(BigNum.of(n)));
            }
        }
        return sum;
    }

    // ---- 掘進 ----

    /**
     * 掘進速度（m/秒）＝ 0.1 × √D。D が 0 以下なら 0。
     * √ は対数で求めるので D がどれだけ大きくてもあふれず、double の上限で頭打ちにする。
     */
    public static double digSpeed(BigNum digPower) {
        if (digPower.signum() <= 0) {
            return 0;
        }
        double log = digPower.log10() / 2 + LOG10_DIG_SPEED_FACTOR;
        if (log > 308) {
            return Double.MAX_VALUE;
        }
        return Math.pow(10, log);
    }

    /** 深さ倍率 ＝ 1.1^階層番号。 */
    public static BigNum depthMultiplier(long level) {
        requireNonNegative(level, "階層番号");
        return BigNum.pow(DEPTH_BASE, level);
    }

    /**
     * 坑道の長さ L（m）＝ 64 ＋ 16 × 階層番号。long であふれないよう double で返す。
     * 2^53 m（約 9×10^15 m）を超えると double の精度で丸まり、階層ごとの +16m は正確でなくなる。
     */
    public static double tunnelLength(long level) {
        requireNonNegative(level, "階層番号");
        return BASE_TUNNEL_LENGTH + TUNNEL_LENGTH_STEP * (double) level;
    }

    /** 1 階層にかかる時間（秒）＝ max(L ÷ 掘進速度, 10)。D = 0 なら無限大。 */
    public static double levelSeconds(long level, BigNum digPower) {
        return levelSecondsAtSpeed(level, digSpeed(digPower));
    }

    /** {@link #levelSeconds(long, BigNum)} の、掘進速度を直接渡す版。速度が 0 以下なら無限大。 */
    public static double levelSecondsAtSpeed(long level, double speed) {
        if (!(speed > 0)) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.max(tunnelLength(level) / speed, MIN_LEVEL_SECONDS);
    }

    // ---- 石炭 ----

    /** 自動の掘進で 1m あたりに採れる石炭 ＝ E × 深さ倍率。 */
    public static BigNum coalPerMeter(BigNum coalPower, long level) {
        return coalPower.multiply(depthMultiplier(level));
    }

    /** 手掘りで 1m 崩したときの石炭 ＝ (E ＋ 100 × ツルハシの所持数) × 深さ倍率。 */
    public static BigNum manualCoalPerMeter(BigNum coalPower, long pickaxeCount, long level) {
        requireNonNegative(pickaxeCount, "ツルハシの所持数");
        BigNum pickaxeBonus = BigNum.of(Tool.PICKAXE.effect()).multiply(BigNum.of(pickaxeCount));
        return coalPower.add(pickaxeBonus).multiply(depthMultiplier(level));
    }

    /**
     * 到達ボーナスの合計。階層 {@code fromExclusive} の次から {@code toInclusive} までの各階層 L について
     * max(掘進速度 × E × 1.1^L × 60, E × 1.1^L) を足す。
     *
     * <p>掘進速度は範囲内で共通なので、合計 ＝ E × max(速度 × 60, 1) × Σ 1.1^L。
     * Σ は等比数列の和 1.1^(from+1) × (1.1^n − 1) ÷ 0.1 で求めるので、何百万階層ぶんでも一度で済む。
     *
     * @param fromExclusive すでにボーナスをもらった最深の階層（まだ一度ももらっていないなら −1）
     * @param toInclusive 今回着いた階層
     * @param speed 掘進速度（m/秒）
     * @param coalPower 採炭力 E
     */
    public static BigNum arrivalBonus(long fromExclusive, long toInclusive, double speed, BigNum coalPower) {
        if (fromExclusive < -1) {
            throw new IllegalArgumentException("fromExclusive は −1 以上にしてください: " + fromExclusive);
        }
        if (toInclusive <= fromExclusive) {
            return BigNum.ZERO;
        }
        long first = fromExclusive + 1;
        // 項の数 n = to − first + 1 は long に収まらないことがある（−1〜Long.MAX_VALUE）。
        // 1.1^n = 1.1^(to − first) × 1.1 として、n そのものは求めない
        BigNum growth = BigNum.pow(DEPTH_BASE, toInclusive - first).multiply(DEPTH_BASE);
        BigNum series = depthMultiplier(first)
                .multiply(growth.subtract(BigNum.ONE))
                .divide(DEPTH_BASE - 1);
        // 速度が double の上限近くでも ×60 であふれないよう BigNum で掛ける
        BigNum perLevel = speed > 0
                ? BigNum.of(speed).multiply(ARRIVAL_BONUS_SECONDS).max(BigNum.ONE)
                : BigNum.ONE;
        return coalPower.multiply(perLevel).multiply(series);
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + "は 0 以上にしてください: " + value);
        }
    }
}
