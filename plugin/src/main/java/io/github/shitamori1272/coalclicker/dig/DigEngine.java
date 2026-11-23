package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.number.BigNum;

/**
 * 毎秒の石炭（企画書 4-1）と手掘り（4-2）で、石炭と累計採掘量を増やす計算。Bukkit に依存しない。
 *
 * <p>採れた石炭を「今の石炭」「累計採掘量」「サーバー全体の累計採掘量」に足す。坑道の階層と進み具合は
 * 累計採掘量から {@link Formulas#levelOf}・{@link Formulas#progressOf} で決まるので、ここでは書き込まない
 * （ワールド上の坑道は呼ぶ側が {@code MineService#syncProgress} で追いつかせる）。
 *
 * <p>毎秒の石炭は深さ倍率を含むので、累計採掘量が増えて階層が進むと途中から増える。{@link #autoCoal} は
 * 階層の境目ごとに区切って足すので、1 秒ごと（オンライン）でも 8 時間ぶん一度に（ログアウト中）でも同じ結果になる。
 */
public final class DigEngine {

    /**
     * {@link #autoCoal} で区切って計算する階層の数の上限。1 階層にかかる時間は階層ごとに約 1.8 倍になるので、
     * ふつうは数十回で済む。これを超えるほど毎秒の石炭が大きいときは、残りの時間をその階層の毎秒の石炭で足す
     * （少なめに見積もるだけで、多くは渡さない）。
     */
    static final int MAX_LEVEL_STEPS = 10_000;

    private DigEngine() {
    }

    /**
     * 増やした結果。
     *
     * @param oldLevel 増やす前の階層（数値上）
     * @param newLevel 増やしたあとの階層（数値上）
     * @param coal 採れた石炭
     */
    public record Change(long oldLevel, long newLevel, BigNum coal) {

        /** 進んだ階層の数。 */
        public long levelsAdvanced() {
            return newLevel - oldLevel;
        }
    }

    /** 毎秒の石炭を {@code seconds} 秒ぶん足す（倍率なし。ログアウト中の分はこちら）。 */
    public static Change autoTick(PlayerData data, ServerData server, double seconds) {
        return autoTick(data, server, seconds, 1);
    }

    /**
     * 毎秒の石炭 × {@code productionMultiplier}（良質な炭層の一時的な倍率。なければ 1）を {@code seconds} 秒ぶん足す。
     * オンライン中は 1 秒ごとに呼ぶ。
     */
    public static Change autoTick(PlayerData data, ServerData server, double seconds, double productionMultiplier) {
        BigNum base = Formulas.coalPerSecond(data.tools(), data.upgrades(), 0).multiply(productionMultiplier);
        return add(data, server, autoCoal(base, data.totalMined(), seconds));
    }

    /** 切羽を 1 回叩く（倍率なし）。 */
    public static Change click(PlayerData data, ServerData server) {
        return click(data, server, 1, 1);
    }

    /**
     * 切羽を 1 回叩く。手掘り 1 回分の石炭をもらい、手掘りの回数を 1 増やす。
     * 石炭 ＝ clickCoal(改良, 毎秒の石炭 × productionMultiplier) × clickMultiplier（倍率は良質な炭層の一時的なもの。なければ 1）。
     */
    public static Change click(PlayerData data, ServerData server, double productionMultiplier,
            double clickMultiplier) {
        BigNum perSecond = Formulas.coalPerSecond(data).multiply(productionMultiplier);
        BigNum coal = Formulas.clickCoal(data.upgrades(), perSecond).multiply(clickMultiplier);
        data.incrementClicks();
        return add(data, server, coal);
    }

    /** 置いてある坑道の切羽の状態（手掘りできるか）。 */
    public enum Face {
        /** 切羽があり、叩くと手掘りになる。 */
        OPEN,
        /** 置いてある坑道を掘り切って、ケージで降りるのを待っている。切羽はなく、叩いても何も出ない。 */
        AWAITING_CAGE
    }

    /**
     * 置いてある坑道（階層 renderedLevel・進み具合 renderedMeters）の切羽の状態。
     * 見た目の階層が数値上の階層と同じで、坑道を掘り切っていないときだけ手掘りできる（企画書 5-5）。
     */
    public static Face face(long numericLevel, long renderedLevel, double renderedMeters) {
        if (numericLevel == renderedLevel && renderedMeters < Formulas.tunnelLength(renderedLevel)) {
            return Face.OPEN;
        }
        return Face.AWAITING_CAGE;
    }

    /** 今の階層（累計採掘量から決まる、数値上の階層）。 */
    public static long level(PlayerData data) {
        return Formulas.levelOf(data.totalMined());
    }

    /**
     * 累計採掘量 {@code totalMined} から {@code seconds} 秒の間に毎秒の石炭で採れる量。
     *
     * <p>階層 L での毎秒の石炭は {@code base × 1.1^L}。今の階層を掘り切るのに要る量 ÷ 毎秒の石炭 が残りの時間より短ければ、
     * 境目までの量を足して次の階層へ進み、残りの時間で同じことを繰り返す（区切りは {@link #MAX_LEVEL_STEPS} 回まで）。
     *
     * @param baseCoalPerSecond 深さ倍率を掛ける前の毎秒の石炭（{@code coalPerSecond(tools, upgrades, 0)}）
     */
    public static BigNum autoCoal(BigNum baseCoalPerSecond, BigNum totalMined, double seconds) {
        if (!(seconds > 0) || baseCoalPerSecond.signum() <= 0) {
            return BigNum.ZERO;
        }
        BigNum total = totalMined;
        BigNum gained = BigNum.ZERO;
        double left = seconds;
        long level = Formulas.levelOf(total);
        for (int i = 0; i < MAX_LEVEL_STEPS && level < Long.MAX_VALUE; i++) {
            BigNum rate = baseCoalPerSecond.multiply(Formulas.depthMultiplier(level));
            BigNum toNext = Formulas.cumulativeToFinish(level).subtract(total).max(BigNum.ZERO);
            double need = toNext.divide(rate).toDouble();
            if (!(need < left)) {
                return gained.add(rate.multiply(left));
            }
            gained = gained.add(toNext);
            total = total.add(toNext);
            left -= need;
            level++;
        }
        return gained.add(baseCoalPerSecond.multiply(Formulas.depthMultiplier(level)).multiply(left));
    }

    private static Change add(PlayerData data, ServerData server, BigNum coal) {
        long oldLevel = level(data);
        // 増えないときは書き込まない（毎秒全員を保存対象にしないため）
        if (coal.signum() > 0) {
            data.setCoal(data.coal().add(coal));
            data.setTotalMined(data.totalMined().add(coal));
            server.addServerTotalMined(coal);
        }
        return new Change(oldLevel, level(data), coal);
    }
}
