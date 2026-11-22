package io.github.shitamori1272.coalclicker.boost;

import java.util.random.RandomGenerator;

/**
 * 良質な炭層（#63）を叩いたときに起きる効果。Bukkit に依存しない。
 *
 * <p>どれが起きるかは重み（{@link #weight()}）で抽選する。重みの合計に対する割合が確率になる
 * （今は 豊炭 45 : 大当たり 45 : 手掘り祭り 10）。
 */
public enum BoostEffect {

    /** 豊炭：毎秒の石炭（自動）× 7 を 77 秒。 */
    RICH_OUTPUT("豊炭", 45, 77_000L, 7, 1),
    /** 大当たり：その場で石炭をもらう（{@link RichSeamRules#jackpotCoal}）。時間のある効果ではない。 */
    JACKPOT("大当たり", 45, 0L, 1, 1),
    /** 手掘り祭り：手掘り 1 回の石炭 × 777 を 13 秒。めずらしい。 */
    HAND_FESTIVAL("手掘り祭り", 10, 13_000L, 1, 777);

    private final String displayName;
    private final int weight;
    private final long durationMillis;
    private final double productionMultiplier;
    private final double clickMultiplier;

    BoostEffect(String displayName, int weight, long durationMillis, double productionMultiplier,
            double clickMultiplier) {
        this.displayName = displayName;
        this.weight = weight;
        this.durationMillis = durationMillis;
        this.productionMultiplier = productionMultiplier;
        this.clickMultiplier = clickMultiplier;
    }

    /** 画面に出す名前。 */
    public String displayName() {
        return displayName;
    }

    /** 抽選の重み。 */
    public int weight() {
        return weight;
    }

    /** 続く時間（ミリ秒）。0 ならその場で終わる効果。 */
    public long durationMillis() {
        return durationMillis;
    }

    /** 時間の続く効果か。 */
    public boolean timed() {
        return durationMillis > 0;
    }

    /** 続いている間、毎秒の石炭に掛かる倍率。 */
    public double productionMultiplier() {
        return productionMultiplier;
    }

    /** 続いている間、手掘り 1 回の石炭に掛かる倍率。 */
    public double clickMultiplier() {
        return clickMultiplier;
    }

    /** 重みの合計。 */
    public static int totalWeight() {
        int sum = 0;
        for (BoostEffect effect : values()) {
            sum += effect.weight;
        }
        return sum;
    }

    /** random で 1 つ抽選する。 */
    public static BoostEffect pick(RandomGenerator random) {
        return pick(random.nextDouble());
    }

    /**
     * roll（0 以上 1 未満）に当たる効果。宣言順に重みの幅を並べ、roll × 重みの合計 が入る幅の効果を返す。
     */
    public static BoostEffect pick(double roll) {
        if (!(roll >= 0 && roll < 1)) {
            throw new IllegalArgumentException("roll は 0 以上 1 未満: " + roll);
        }
        double point = roll * totalWeight();
        double upper = 0;
        BoostEffect[] all = values();
        for (BoostEffect effect : all) {
            upper += effect.weight;
            if (point < upper) {
                return effect;
            }
        }
        return all[all.length - 1];
    }
}
