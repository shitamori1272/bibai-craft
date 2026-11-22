package io.github.shitamori1272.coalclicker.admin;

import io.github.shitamori1272.coalclicker.data.MineState;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.List;
import java.util.Optional;

/** 管理コマンドの中身。Bukkit に依存しない。 */
public final class AdminActions {

    private AdminActions() {
    }

    /** 付与量を読む。{@code 1e100} のような表記も受け付ける。0 以下や読めない値なら空。 */
    public static Optional<BigNum> parseAmount(String text) {
        try {
            BigNum amount = BigNum.parse(text);
            return amount.signum() > 0 ? Optional.of(amount) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * 石炭を付与する（デモ用）。今の石炭だけを増やし、累計採掘量やサーバー全体の出炭量には数えない。
     *
     * @return 付与後の石炭。大きすぎて表せないときは空（データは変えない）
     */
    public static Optional<BigNum> give(PlayerData data, BigNum amount) {
        BigNum after;
        try {
            after = data.coal().add(amount);
        } catch (ArithmeticException overflow) {
            return Optional.empty();
        }
        data.setCoal(after);
        return Optional.of(after);
    }

    /** データを初期値に戻す。区画番号は再割り当てしないよう残す。 */
    public static void reset(PlayerData data) {
        data.setCoal(BigNum.ZERO);
        data.setTotalMined(BigNum.ZERO);
        for (String id : List.copyOf(data.tools().keySet())) {
            data.setToolCount(id, 0);
        }
        data.clearExplained();
        MineState mine = data.mine();
        mine.setLevel(0);
        mine.setProgress(0);
        mine.setMaxReachedLevel(0);
    }
}
