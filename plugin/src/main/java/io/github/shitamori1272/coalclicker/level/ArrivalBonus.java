package io.github.shitamori1272.coalclicker.level;

import io.github.shitamori1272.coalclicker.data.MineState;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Optional;

/**
 * 到達ボーナス（企画書 4-4）。ケージか入坑で実際に着いたときに、着いた階層（作り直した見た目の階層）を渡して呼ぶ。
 * Bukkit に依存しない。
 *
 * <p>{@code maxReachedLevel} は「すでにボーナスをもらった（または最初からいる）最深の階層」。
 * 初期値 0 は第 1 坑道に着いた扱いなので、第 1 坑道のボーナスはない。
 * 着いた階層がそれより深ければ、間の階層ぶんを {@link Formulas#arrivalBonus} の和の公式でまとめて渡す。
 * 1 階層あたり ＝ その階層での毎秒の石炭（深さ倍率を掛ける前の値 × 1.1^階層）× 60 秒分。
 * 最低保証は手掘り 1 回分のうち毎秒の石炭によらない部分 {@link Formulas#clickBase}。
 *
 * <p>ボーナスは累計採掘量に含めるので、それで数値上の階層が進むことがある。その階層は「着いた」ことにならず、
 * ボーナスは次にケージで着いたときに渡す（着いた階層は呼ぶ側が決めるので、ここで累計採掘量から求め直さない）。
 */
public final class ArrivalBonus {

    private ArrivalBonus() {
    }

    /**
     * もらったボーナス。
     *
     * @param fromLevel それまでの最深の階層（この階層のボーナスは含まない）
     * @param toLevel 今回着いた階層
     * @param coal もらった石炭
     */
    public record Result(long fromLevel, long toLevel, BigNum coal) {

        /** まとめてもらった階層の数。 */
        public long levels() {
            return toLevel - fromLevel;
        }
    }

    /**
     * 着いた階層 {@code arrivedLevel} がはじめての階層なら、ボーナスを石炭・累計採掘量・サーバー全体の累計採掘量に足し、
     * {@code maxReachedLevel} をその階層にする。すでに着いたことのある階層なら何もしない。
     */
    public static Optional<Result> claim(PlayerData data, ServerData server, long arrivedLevel) {
        MineState mine = data.mine();
        long from = mine.maxReachedLevel();
        long to = arrivedLevel;
        if (to <= from) {
            return Optional.empty();
        }
        BigNum coal = Formulas.arrivalBonus(from, to, Formulas.coalPerSecond(data.tools(), data.upgrades(), 0),
                Formulas.clickBase(data.upgrades()));
        if (coal.signum() > 0) {
            data.setCoal(data.coal().add(coal));
            data.setTotalMined(data.totalMined().add(coal));
            server.addServerTotalMined(coal);
        }
        mine.setMaxReachedLevel(to);
        return Optional.of(new Result(from, to, coal));
    }
}
