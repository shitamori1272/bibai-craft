package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.data.MineState;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Progress;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.mine.TunnelPlan;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Map;

/**
 * 自動の掘進（#15）と手掘り（#14）で、保存データの進み具合と石炭を進める計算。Bukkit に依存しない。
 *
 * <p>どちらも {@link Progress} の決まり（その階層の長さで止まり、超えた分は捨て、掘り切ったら数値上の階層を +1）に従い、
 * 採れた石炭を「今の石炭」「累計採掘量」「サーバー全体の累計採掘量」に足す。
 */
public final class DigEngine {

    /** 手掘りで切羽を 1 回崩したときに進む長さ（m）。 */
    public static final double MANUAL_METERS = 1;

    private DigEngine() {
    }

    /**
     * 進めた結果。
     *
     * @param oldLevel 進める前の階層
     * @param oldMeters 進める前の進み具合（m）
     * @param newLevel 進めたあとの階層
     * @param newMeters 進めたあとの進み具合（m。newLevel での値）
     * @param coal 採れた石炭
     * @param levelsCompleted 掘り切った階層の数
     * @param meters 掘った長さ（m。捨てた分は含まない）
     */
    public record Change(long oldLevel, double oldMeters, long newLevel, double newMeters, BigNum coal,
            long levelsCompleted, double meters) {

        /** 進み具合が変わったか。 */
        public boolean moved() {
            return levelsCompleted > 0 || newMeters != oldMeters;
        }

        /** 進める前の階層を掘り切ったか。 */
        public boolean completedOldLevel() {
            return levelsCompleted > 0;
        }

        /** 進める前の階層の長さ（掘り切ったときの進み具合）。 */
        public double oldLevelLength() {
            return Formulas.tunnelLength(oldLevel);
        }
    }

    /** 自動の掘進を {@code seconds} 秒ぶん進める（オンライン中は 1 秒ごとに呼ぶ）。 */
    public static Change autoTick(PlayerData data, ServerData server, double seconds) {
        Map<String, Long> tools = data.tools();
        MineState mine = data.mine();
        Progress.Result result = Progress.advance(mine.level(), mine.progress(),
                Formulas.digPower(tools), Formulas.coalPower(tools), seconds);
        return apply(data, server, result);
    }

    /** 手掘りで切羽を 1 回崩す。1m（階層の残りが 1m 未満ならそこまで）進め、手掘りの石炭をもらう。 */
    public static Change manualMeter(PlayerData data, ServerData server) {
        MineState mine = data.mine();
        Progress.Result result = Progress.advanceMeters(mine.level(), mine.progress(), MANUAL_METERS,
                manualCoalPerMeter(data, mine.level()));
        return apply(data, server, result);
    }

    /** 手掘りで 1m 崩したときの石炭（階層 level）。 */
    public static BigNum manualCoalPerMeter(PlayerData data, long level) {
        Map<String, Long> tools = data.tools();
        return Formulas.manualCoalPerMeter(Formulas.coalPower(tools), Formulas.count(tools, Tool.PICKAXE), level);
    }

    /** 保存データの今の進み具合（m）。 */
    public static double meters(MineState mine) {
        return TunnelPlan.meters(mine.progress(), Formulas.tunnelLength(mine.level()));
    }

    private static Change apply(PlayerData data, ServerData server, Progress.Result result) {
        MineState mine = data.mine();
        long oldLevel = mine.level();
        double oldMeters = meters(mine);
        // 変わらないときは書き込まない（毎秒全員を保存対象にしないため）
        if (result.level() != oldLevel) {
            mine.setLevel(result.level());
        }
        if (result.progress() != mine.progress()) {
            mine.setProgress(result.progress());
        }
        BigNum coal = result.coal();
        if (coal.signum() > 0) {
            data.setCoal(data.coal().add(coal));
            data.setTotalMined(data.totalMined().add(coal));
            server.addServerTotalMined(coal);
        }
        return new Change(oldLevel, oldMeters, mine.level(), meters(mine), coal, result.levelsCompleted(),
                result.meters());
    }
}
