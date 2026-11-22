package io.github.shitamori1272.coalclicker.boost;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.random.RandomGenerator;

/**
 * 良質な炭層（#63）の出方と大当たりの計算。Bukkit に依存しない。
 *
 * <ul>
 *   <li>自分の坑道にいる間、前の炭層が消えて（叩いて・時間切れで）から 2〜5 分（一様乱数）たつと、次の炭層が光る
 *   <li>光っている炭層は 13 秒で消える
 *   <li>大当たり ＝ min(今の石炭 × 15%, 毎秒の石炭 × 900) ＋ 13
 * </ul>
 */
public final class RichSeamRules {

    /** 次の炭層までの最短（ミリ秒）。 */
    public static final long MIN_INTERVAL_MILLIS = 120_000L;
    /** 次の炭層までの最長（ミリ秒）。 */
    public static final long MAX_INTERVAL_MILLIS = 300_000L;
    /** 炭層が光っている時間（ミリ秒）。 */
    public static final long LIFETIME_MILLIS = 13_000L;
    /** 大当たりで今の石炭から取る割合。 */
    public static final double JACKPOT_COAL_RATE = 0.15;
    /** 大当たりの上限（毎秒の石炭の何秒分か）。 */
    public static final double JACKPOT_MAX_SECONDS = 900;
    /** 大当たりにいつも足す分。 */
    public static final double JACKPOT_FLAT = 13;

    private RichSeamRules() {
    }

    /** 次の炭層までの待ち時間（ミリ秒。{@link #MIN_INTERVAL_MILLIS} 以上 {@link #MAX_INTERVAL_MILLIS} 以下）。 */
    public static long nextIntervalMillis(RandomGenerator random) {
        return random.nextLong(MIN_INTERVAL_MILLIS, MAX_INTERVAL_MILLIS + 1);
    }

    /**
     * 大当たりでもらう石炭 ＝ min(coal × 15%, coalPerSecond × 900) ＋ 13。
     *
     * @param coal 今の石炭
     * @param coalPerSecond 今の毎秒の石炭（豊炭の倍率は掛けない）
     */
    public static BigNum jackpotCoal(BigNum coal, BigNum coalPerSecond) {
        BigNum byCoal = coal.multiply(JACKPOT_COAL_RATE);
        BigNum cap = coalPerSecond.multiply(JACKPOT_MAX_SECONDS);
        return byCoal.min(cap).max(BigNum.ZERO).add(BigNum.of(JACKPOT_FLAT));
    }
}
