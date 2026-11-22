package io.github.shitamori1272.coalclicker.level;

import io.github.shitamori1272.coalclicker.data.MineState;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Optional;

/**
 * 到達ボーナス（企画書 4-3、#24）。ケージか入坑で実際に着いたときに呼ぶ。Bukkit に依存しない。
 *
 * <p>{@code maxReachedLevel} は「すでにボーナスをもらった（または最初からいる）最深の階層」。
 * 初期値 0 は第 1 坑道に着いた扱いなので、第 1 坑道のボーナスはない。
 * 今の階層（累計採掘量から決まる数値上の階層）がそれより深ければ、間の階層ぶんを {@link Formulas#arrivalBonus} でまとめて渡す。
 * 1 階層あたり ＝ その階層での毎秒の石炭 × 60 秒分（最低保証はその階層での手掘り 1 回分）。
 *
 * <p>ボーナスは累計採掘量に含めるが、それで階層が進んでも、その階層のボーナスは次に着いたときに渡す。
 * TODO(#60): ケージで降りた階層（見た目の階層）と合わせるかを見直す。
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
     * 今の階層がはじめて着いた階層なら、ボーナスを石炭・累計採掘量・サーバー全体の累計採掘量に足し、
     * {@code maxReachedLevel} を今の階層にする。すでに着いたことのある階層なら何もしない。
     */
    public static Optional<Result> claim(PlayerData data, ServerData server) {
        MineState mine = data.mine();
        long from = mine.maxReachedLevel();
        long to = Formulas.levelOf(data.totalMined());
        if (to <= from) {
            return Optional.empty();
        }
        BigNum coal = Formulas.arrivalBonus(from, to, Formulas.coalPerSecond(data.tools(), data.upgrades(), 0),
                data.upgrades());
        if (coal.signum() > 0) {
            data.setCoal(data.coal().add(coal));
            data.setTotalMined(data.totalMined().add(coal));
            server.addServerTotalMined(coal);
        }
        mine.setMaxReachedLevel(to);
        return Optional.of(new Result(from, to, coal));
    }
}
