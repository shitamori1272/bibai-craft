package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

/**
 * ゲームの計算式（企画書 4 章・6 章。クッキークリッカー型）。Bukkit に依存しない。
 *
 * <p>階層番号 {@code level} は 0 始まり（第 1 坑道 = 0）。大きくなりうる値は {@link BigNum} で扱う。
 * 道具の所持数は {@code Map<道具ID, 個数>}、買った改良は {@code Set<改良ID>} で受け取り、知らない ID は無視する。
 *
 * <p>累乗（1.15^n・1.1^n・2^n）は、double に収まる範囲では {@link Math#pow} で、それを超えると
 * 「指数 ＝ n × log10(底)」を高い精度（{@link BigDecimal}）で求めてから作る。掛け算を n 回しないので、
 * n がどれだけ大きくても速く、誤差も積み重ならない。
 */
public final class Formulas {

    /** 道具を 1 つ買うごとに価格に掛かる倍率。 */
    public static final double PRICE_GROWTH = 1.15;
    /** 深さ倍率の底。 */
    public static final double DEPTH_BASE = 1.1;
    /** 道具の改良 1 つで、その道具の効果に掛かる倍率。 */
    public static final double TOOL_UPGRADE_FACTOR = 2;
    /** 手掘り 1 回の基本の石炭。 */
    public static final double CLICK_BASE = 10;
    /** ツルハシの改良 1 つで、手掘りの基本に掛かる倍率。 */
    public static final double PICKAXE_FACTOR = 2;
    /** 熟練の改良 1 つで、手掘りに上乗せする毎秒の石炭の割合。 */
    public static final double SKILL_RATE = 0.01;
    /** 第 1 坑道（階層 0）を掘り切るのに必要な累計採掘量。階層ごとに 2 倍。 */
    public static final double BASE_LEVEL_REQUIREMENT = 1_000;
    /** 第 1 坑道の長さ（m）。 */
    public static final double BASE_TUNNEL_LENGTH = 64;
    /** 1 階層ごとに伸びる坑道の長さ（m）。 */
    public static final double TUNNEL_LENGTH_STEP = 16;
    /** 到達ボーナスで何秒分の石炭を渡すか。 */
    public static final double ARRIVAL_BONUS_SECONDS = 60;

    /** 1000 × (2^(n+1) − 1) を double で正確に計算できる最大の n（1000 = 2^3 × 125 で、125 は 7 ビット）。 */
    private static final int EXACT_CUMULATIVE_MAX_LEVEL = 45;
    /** これより大きい指数（10 の何乗か）は double に収まらないとみなして高い精度の対数で作る。 */
    private static final double DOUBLE_LOG10_LIMIT = 300;

    private static final MathContext MC = new MathContext(40, RoundingMode.HALF_EVEN);
    private static final BigDecimal LN10 = ln(BigDecimal.TEN);
    private static final BigDecimal LOG10_2 = log10(BigDecimal.valueOf(2));
    private static final BigDecimal LOG10_DEPTH = log10(BigDecimal.valueOf(DEPTH_BASE));
    private static final BigDecimal LOG10_PRICE = log10(BigDecimal.valueOf(PRICE_GROWTH));
    private static final double LOG10_DEPTH_DOUBLE = Math.log10(DEPTH_BASE);
    private static final BigNum BASE_REQ = BigNum.of(BASE_LEVEL_REQUIREMENT);

    private Formulas() {
    }

    // ---- 価格 ----

    /** 次の 1 つの価格 ＝ 初期価格 × 1.15^所持数。 */
    public static BigNum price(Tool tool, long owned) {
        requireNonNegative(owned, "所持数");
        return tool.basePrice().multiply(power(PRICE_GROWTH, LOG10_PRICE, owned));
    }

    /**
     * 所持数 {@code owned} から {@code amount} 個まとめて買う価格。
     * 等比数列の和 price(owned) × (1.15^amount − 1) ÷ 0.15 で一度に求める。
     */
    public static BigNum totalPrice(Tool tool, long owned, long amount) {
        requireNonNegative(amount, "個数");
        if (amount == 0) {
            return BigNum.ZERO;
        }
        BigNum series = power(PRICE_GROWTH, LOG10_PRICE, amount).subtract(BigNum.ONE).divide(PRICE_GROWTH - 1);
        return price(tool, owned).multiply(series);
    }

    // ---- 倍率 ----

    /** 道具の所持数。持っていなければ 0。 */
    public static long count(Map<String, Long> tools, Tool tool) {
        Long n = tools.get(tool.id());
        return n == null ? 0 : n;
    }

    /** その道具の改良を買った数（0〜7）。 */
    public static int toolUpgradeCount(Tool tool, Set<String> upgrades) {
        int n = 0;
        for (Upgrade upgrade : Upgrade.values()) {
            if (upgrade.kind() == Upgrade.Kind.TOOL && upgrade.tool().orElseThrow() == tool
                    && upgrades.contains(upgrade.id())) {
                n++;
            }
        }
        return n;
    }

    /** 道具の改良倍率 ＝ 2^(その道具の改良を買った数)。 */
    public static BigNum toolMultiplier(Tool tool, Set<String> upgrades) {
        return BigNum.of(Math.pow(TOOL_UPGRADE_FACTOR, toolUpgradeCount(tool, upgrades)));
    }

    /** 全体の倍率 ＝ 全体の改良の積。今は全体の改良がないので 1（あとで足すための入り口）。 */
    public static BigNum globalMultiplier(Set<String> upgrades) {
        return BigNum.ONE;
    }

    /** 深さ倍率 ＝ 1.1^階層番号。 */
    public static BigNum depthMultiplier(long level) {
        requireNonNegative(level, "階層番号");
        return power(DEPTH_BASE, LOG10_DEPTH, level);
    }

    // ---- 毎秒の石炭 ----

    /**
     * 毎秒の石炭 ＝ Σ（道具の基本値 × 所持数 × その道具の改良倍率）× 全体の倍率 × 深さ倍率。
     */
    public static BigNum coalPerSecond(Map<String, Long> tools, Set<String> upgrades, long level) {
        BigNum sum = BigNum.ZERO;
        for (Tool tool : Tool.values()) {
            sum = sum.add(rawToolCoalPerSecond(tool, tools, upgrades));
        }
        return sum.multiply(globalMultiplier(upgrades)).multiply(depthMultiplier(level));
    }

    /** {@link #coalPerSecond(Map, Set, long)} を、累計採掘量から決まる今の階層で求める。 */
    public static BigNum coalPerSecond(GameState state) {
        return coalPerSecond(state.tools(), state.upgrades(), levelOf(state.totalMined()));
    }

    /** その道具 1 種類が出している毎秒の石炭（所持数ぶん。全体・深さの倍率込み）。 */
    public static BigNum toolCoalPerSecond(Tool tool, Map<String, Long> tools, Set<String> upgrades, long level) {
        return rawToolCoalPerSecond(tool, tools, upgrades)
                .multiply(globalMultiplier(upgrades))
                .multiply(depthMultiplier(level));
    }

    /** その道具をもう 1 つ買うと増える毎秒の石炭（所持数によらない）。 */
    public static BigNum marginalCoalPerSecond(Tool tool, Set<String> upgrades, long level) {
        return tool.baseCoalPerSecond()
                .multiply(toolMultiplier(tool, upgrades))
                .multiply(globalMultiplier(upgrades))
                .multiply(depthMultiplier(level));
    }

    private static BigNum rawToolCoalPerSecond(Tool tool, Map<String, Long> tools, Set<String> upgrades) {
        long n = count(tools, tool);
        requireNonNegative(n, tool.id() + " の所持数");
        if (n == 0) {
            return BigNum.ZERO;
        }
        return tool.baseCoalPerSecond().multiply(BigNum.of(n)).multiply(toolMultiplier(tool, upgrades));
    }

    // ---- 手掘り ----

    /** 買ったツルハシの改良の数。 */
    public static int pickaxeUpgradeCount(Set<String> upgrades) {
        return countKind(upgrades, Upgrade.Kind.PICKAXE);
    }

    /** 買った熟練の改良の数。 */
    public static int skillUpgradeCount(Set<String> upgrades) {
        return countKind(upgrades, Upgrade.Kind.SKILL);
    }

    /** 手掘り 1 回の石炭のうち、毎秒の石炭によらない部分 ＝ 10 × 2^(ツルハシの改良の数)。 */
    public static BigNum clickBase(Set<String> upgrades) {
        return BigNum.of(CLICK_BASE * Math.pow(PICKAXE_FACTOR, pickaxeUpgradeCount(upgrades)));
    }

    /**
     * 手掘り 1 回の石炭 ＝ 10 × 2^(ツルハシの改良の数) ＋ 毎秒の石炭 × 0.01 × (熟練の改良の数)。
     *
     * @param coalPerSecond 今の毎秒の石炭（深さ倍率込み）
     */
    public static BigNum clickCoal(Set<String> upgrades, BigNum coalPerSecond) {
        int skills = skillUpgradeCount(upgrades);
        BigNum base = clickBase(upgrades);
        if (skills == 0) {
            return base;
        }
        return base.add(coalPerSecond.multiply(SKILL_RATE * skills));
    }

    /** {@link #clickCoal(Set, BigNum)} を今の状態で求める。 */
    public static BigNum clickCoal(GameState state) {
        return clickCoal(state.upgrades(), coalPerSecond(state));
    }

    private static int countKind(Set<String> upgrades, Upgrade.Kind kind) {
        int n = 0;
        for (Upgrade upgrade : Upgrade.values()) {
            if (upgrade.kind() == kind && upgrades.contains(upgrade.id())) {
                n++;
            }
        }
        return n;
    }

    // ---- 累計採掘量による坑道 ----

    /** 階層 n（0 始まり）を掘り切るのに必要な量 ＝ 1,000 × 2^n。 */
    public static BigNum levelRequirement(long level) {
        requireNonNegative(level, "階層番号");
        if (level <= EXACT_CUMULATIVE_MAX_LEVEL) {
            return BigNum.of(BASE_LEVEL_REQUIREMENT * Math.scalb(1.0, (int) level));
        }
        return BASE_REQ.multiply(pow2(level));
    }

    /**
     * 階層 n（0 始まり）を掘り切るまでの累計 ＝ 1,000 × (2^(n+1) − 1)。n = −1 なら 0。
     * n ≤ 45 では double で正確に計算するので、{@code BigNum.of(1000 × (2^(n+1) − 1))} とちょうど等しい。
     */
    public static BigNum cumulativeToFinish(long level) {
        if (level < -1) {
            throw new IllegalArgumentException("階層番号は −1 以上にしてください: " + level);
        }
        if (level == -1) {
            return BigNum.ZERO;
        }
        if (level <= EXACT_CUMULATIVE_MAX_LEVEL) {
            return BigNum.of(BASE_LEVEL_REQUIREMENT * (Math.scalb(1.0, (int) level + 1) - 1));
        }
        // 2^(n+1) = 2^n × 2。n = Long.MAX_VALUE でもあふれない
        return BASE_REQ.multiply(pow2(level).multiply(2).subtract(BigNum.ONE));
    }

    /**
     * 累計採掘量から決まる、数値上の階層（0 始まり）。
     * cumulativeToFinish(n − 1) ≤ 累計 &lt; cumulativeToFinish(n) となる n ＝ floor(log2(累計 ÷ 1,000 ＋ 1))。
     *
     * <p>log2 を高い精度で見積もってから {@link #cumulativeToFinish} と比べて補正するので、境目ちょうどでも
     * {@link #cumulativeToFinish} と食い違わない。どんなに大きな累計でも計算でき、{@link Long#MAX_VALUE} で頭打ちにする。
     */
    public static long levelOf(BigNum totalMined) {
        if (totalMined.compareTo(BASE_REQ) < 0) {
            return 0;
        }
        BigNum x = totalMined.divide(BASE_LEVEL_REQUIREMENT).add(BigNum.ONE);
        BigDecimal log10 = BigDecimal.valueOf(x.exponent()).add(BigDecimal.valueOf(Math.log10(x.mantissa())));
        BigDecimal log2 = log10.divide(LOG10_2, MC).setScale(0, RoundingMode.FLOOR);
        if (log2.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) >= 0) {
            return Long.MAX_VALUE;
        }
        long n = Math.max(0, log2.longValueExact());
        // 見積もりの誤差（ふつうは 0、境目で ±1）を正確な比較で直す
        while (n < Long.MAX_VALUE && cumulativeToFinish(n).compareTo(totalMined) <= 0) {
            n++;
        }
        while (n > 0 && cumulativeToFinish(n - 1).compareTo(totalMined) > 0) {
            n--;
        }
        return n;
    }

    /** 今の階層の進み具合（0 以上 1 未満の割合）＝（累計 − 前の階層までの累計）÷ この階層に必要な量。 */
    public static double progressOf(BigNum totalMined) {
        if (totalMined.signum() <= 0) {
            return 0;
        }
        long level = levelOf(totalMined);
        BigNum done = totalMined.subtract(cumulativeToFinish(level - 1));
        double p = done.divide(levelRequirement(level)).toDouble();
        if (!(p > 0)) {
            return 0;
        }
        return Math.min(p, Math.nextDown(1.0));
    }

    /**
     * 坑道の長さ L（m）＝ 64 ＋ 16 × 階層番号。long であふれないよう double で返す。
     * 2^53 m（約 9×10^15 m）を超えると double の精度で丸まり、階層ごとの +16m は正確でなくなる。
     */
    public static double tunnelLength(long level) {
        requireNonNegative(level, "階層番号");
        return BASE_TUNNEL_LENGTH + TUNNEL_LENGTH_STEP * (double) level;
    }

    // ---- 到達ボーナス ----

    /**
     * 到達ボーナスの合計 ＝ 階層 {@code fromExclusive} の次から {@code toInclusive} までの各階層 L について
     * max(base × 1.1^L × 60, minimum) を足したもの。
     *
     * <p>base × 1.1^L は L について増えていくので、minimum を下回るのは前の方の階層だけ。そこで
     * 「base × 1.1^L × 60 ≥ minimum になる最初の階層 t」を対数で求め（比較で補正）、
     * 合計 ＝ minimum ×（t より前の階層の数）＋ base × 60 × Σ_{L=t..to} 1.1^L とする。
     * Σ は等比数列の和の公式で求めるので、何百万階層ぶんでも一度で済む。
     *
     * <p>最低保証について：企画書では「手掘り 1 回分」。手掘り 1 回は 10 × 2^p ＋ 毎秒 × 0.01 × s で、
     * 後ろの項は階層ごとに変わる。呼び出し側には階層によらない部分 {@link #clickBase(Set)} を渡してもらう想定。
     * 毎秒に比例する部分（最大で毎秒の 5%）は 60 秒分（毎秒の 60 倍）より十分小さいので、無視しても
     * 1 階層あたりのずれは 1/1,200 以下になる。
     *
     * @param fromExclusive すでにボーナスをもらった最深の階層（まだ一度ももらっていないなら −1）
     * @param toInclusive 今回着いた階層
     * @param baseCoalPerSecond 深さ倍率を掛ける前の毎秒の石炭（{@code coalPerSecond(tools, upgrades, 0)}）
     * @param minimum 1 階層あたりの最低保証（ふつうは {@link #clickBase(Set)}）
     */
    public static BigNum arrivalBonus(long fromExclusive, long toInclusive, BigNum baseCoalPerSecond, BigNum minimum) {
        if (fromExclusive < -1) {
            throw new IllegalArgumentException("fromExclusive は −1 以上にしてください: " + fromExclusive);
        }
        if (toInclusive <= fromExclusive) {
            return BigNum.ZERO;
        }
        long first = fromExclusive + 1;
        BigNum perLevel = baseCoalPerSecond.multiply(ARRIVAL_BONUS_SECONDS);
        BigNum min = minimum.max(BigNum.ZERO);
        if (perLevel.signum() <= 0) {
            return min.multiply(levelCount(first, toInclusive));
        }
        long t = firstLevelAtLeast(perLevel, min, first, toInclusive);
        BigNum sum = BigNum.ZERO;
        if (t > first) {
            sum = sum.add(min.multiply(levelCount(first, t - 1)));
        }
        if (t <= toInclusive) {
            sum = sum.add(perLevel.multiply(depthSeries(t, toInclusive)));
        }
        return sum;
    }

    /** first〜to の中で perLevel × 1.1^L ≥ min になる最初の L。どこも届かなければ to + 1。 */
    private static long firstLevelAtLeast(BigNum perLevel, BigNum min, long first, long to) {
        if (min.signum() <= 0 || perLevel.multiply(depthMultiplier(first)).isGreaterThanOrEqual(min)) {
            return first;
        }
        if (!perLevel.multiply(depthMultiplier(to)).isGreaterThanOrEqual(min)) {
            return to + 1;
        }
        // 1.1^L ≥ min ÷ perLevel ⇔ L ≥ log10(min ÷ perLevel) ÷ log10(1.1)
        double estimate = Math.ceil(min.divide(perLevel).log10() / LOG10_DEPTH_DOUBLE);
        long t = (long) Math.clamp(estimate, first, to);
        while (t > first && perLevel.multiply(depthMultiplier(t - 1)).isGreaterThanOrEqual(min)) {
            t--;
        }
        while (t < to && !perLevel.multiply(depthMultiplier(t)).isGreaterThanOrEqual(min)) {
            t++;
        }
        return t;
    }

    /** Σ_{L=from..to} 1.1^L ＝ 1.1^from × (1.1^(to − from + 1) − 1) ÷ 0.1。 */
    private static BigNum depthSeries(long from, long to) {
        // 項の数 to − from + 1 は long に収まらないことがあるので、1.1^(to − from) × 1.1 とする
        BigNum growth = depthMultiplier(to - from).multiply(DEPTH_BASE);
        return depthMultiplier(from).multiply(growth.subtract(BigNum.ONE)).divide(DEPTH_BASE - 1);
    }

    /** from〜to の階層の数（long に収まらなくてもよい）。 */
    private static BigNum levelCount(long from, long to) {
        return BigNum.of((double) to - (double) from + 1);
    }

    // ---- 累乗 ----

    /** 2^n（n ≥ 0）。 */
    static BigNum pow2(long n) {
        requireNonNegative(n, "指数");
        if (n <= 1000) {
            return BigNum.of(Math.scalb(1.0, (int) n));
        }
        return fromPreciseLog10(LOG10_2.multiply(BigDecimal.valueOf(n)));
    }

    /** base^n（n ≥ 0）。double に収まる範囲は Math.pow、それを超えると高い精度の対数から作る。 */
    private static BigNum power(double base, BigDecimal log10Base, long n) {
        requireNonNegative(n, "指数");
        if ((double) n * Math.log10(base) < DOUBLE_LOG10_LIMIT) {
            return BigNum.of(Math.pow(base, n));
        }
        return fromPreciseLog10(log10Base.multiply(BigDecimal.valueOf(n)));
    }

    private static BigNum fromPreciseLog10(BigDecimal log10) {
        BigDecimal floor = log10.setScale(0, RoundingMode.FLOOR);
        double fraction = log10.subtract(floor).doubleValue();
        return BigNum.of(Math.pow(10, fraction), floor.longValueExact());
    }

    /** 高い精度の常用対数（クラスの初期化で定数を作るのに使う）。 */
    private static BigDecimal log10(BigDecimal x) {
        return ln(x).divide(LN10, MC);
    }

    /** 高い精度の自然対数。ln(x) ＝ 2 × Σ z^(2k+1) ÷ (2k+1)、z ＝ (x − 1) ÷ (x + 1)。x &gt; 0。 */
    private static BigDecimal ln(BigDecimal x) {
        BigDecimal z = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), MC);
        BigDecimal z2 = z.multiply(z, MC);
        BigDecimal term = z;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal eps = BigDecimal.ONE.movePointLeft(45);
        for (int k = 0; term.abs().compareTo(eps) > 0 && k < 10_000; k++) {
            sum = sum.add(term.divide(BigDecimal.valueOf(2L * k + 1), MC), MC);
            term = term.multiply(z2, MC);
        }
        return sum.multiply(BigDecimal.valueOf(2), MC);
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + "は 0 以上にしてください: " + value);
        }
    }
}
