package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.mine.TunnelPlan;
import io.github.shitamori1272.coalclicker.number.BigNum;

/**
 * 毎秒の石炭（自動）と手掘りで、石炭と累計採掘量を増やす計算。Bukkit に依存しない。
 *
 * <p>採れた石炭を「今の石炭」「累計採掘量」「サーバー全体の累計採掘量」に足す。坑道の階層と進み具合は
 * 累計採掘量から {@link Formulas#levelOf}・{@link Formulas#progressOf} で決まるので、ここでは書き込まない。
 *
 * <p>TODO(#59): 新しいルールに合わせた最小限の書き直し。手掘り・自動の処理と表示は #59 で作り直す。
 */
public final class DigEngine {

    private DigEngine() {
    }

    /**
     * 増やした結果。
     *
     * @param oldLevel 増やす前の階層
     * @param oldMeters 増やす前の進み具合（m）
     * @param newLevel 増やしたあとの階層
     * @param newMeters 増やしたあとの進み具合（m。newLevel での値）
     * @param coal 採れた石炭
     */
    public record Change(long oldLevel, double oldMeters, long newLevel, double newMeters, BigNum coal) {

        /** 進み具合が変わったか。 */
        public boolean moved() {
            return newLevel != oldLevel || newMeters != oldMeters;
        }

        /** 進めた間に掘り切った階層の数。 */
        public long levelsCompleted() {
            return newLevel - oldLevel;
        }

        /** 進める前の階層を掘り切ったか。 */
        public boolean completedOldLevel() {
            return newLevel > oldLevel;
        }

        /** 進める前の階層の長さ（掘り切ったときの進み具合）。 */
        public double oldLevelLength() {
            return Formulas.tunnelLength(oldLevel);
        }
    }

    /** 毎秒の石炭を {@code seconds} 秒ぶん足す（オンライン中は 1 秒ごとに呼ぶ）。 */
    public static Change autoTick(PlayerData data, ServerData server, double seconds) {
        BigNum coal = seconds > 0 ? Formulas.coalPerSecond(data).multiply(seconds) : BigNum.ZERO;
        return add(data, server, coal);
    }

    /** 切羽を 1 回叩く。手掘り 1 回分の石炭をもらい、手掘りの回数を 1 増やす。 */
    public static Change click(PlayerData data, ServerData server) {
        BigNum coal = Formulas.clickCoal(data);
        data.incrementClicks();
        return add(data, server, coal);
    }

    /** 今の階層（累計採掘量から決まる、数値上の階層）。 */
    public static long level(PlayerData data) {
        return Formulas.levelOf(data.totalMined());
    }

    /** 今の進み具合（m）。 */
    public static double meters(PlayerData data) {
        BigNum total = data.totalMined();
        return TunnelPlan.meters(Formulas.progressOf(total), Formulas.tunnelLength(Formulas.levelOf(total)));
    }

    private static Change add(PlayerData data, ServerData server, BigNum coal) {
        long oldLevel = level(data);
        double oldMeters = meters(data);
        // 増えないときは書き込まない（毎秒全員を保存対象にしないため）
        if (coal.signum() > 0) {
            data.setCoal(data.coal().add(coal));
            data.setTotalMined(data.totalMined().add(coal));
            server.addServerTotalMined(coal);
        }
        return new Change(oldLevel, oldMeters, level(data), meters(data), coal);
    }
}
