package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Objects;

/** 道具の購入処理。Bukkit に依存しないので単体テストできる。 */
public final class Purchase {

    private Purchase() {
    }

    /**
     * 購入の結果。
     *
     * @param success 買えたか
     * @param tool 道具
     * @param amount 買おうとした個数
     * @param cost 合計価格（買えなかったときも、必要だった額）
     * @param shortage 足りなかった石炭（買えたときは 0）
     * @param firstPurchase はじめて買った（解説をまだ出していなかった）か。買えなかったときは false
     */
    public record Result(boolean success, Tool tool, long amount, BigNum cost, BigNum shortage,
            boolean firstPurchase) {
    }

    /**
     * 道具を {@code amount} 個まとめて買う。石炭が足りなければ何も変えない。
     * 買えたら石炭を減らして所持数を増やし、はじめての購入なら解説済みにする。
     */
    public static Result tryBuy(PlayerData data, Tool tool, long amount) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(tool);
        if (amount <= 0) {
            throw new IllegalArgumentException("個数は 1 以上にしてください: " + amount);
        }
        BigNum cost = Formulas.totalPrice(tool, data.getToolCount(tool.id()), amount);
        BigNum coal = data.coal();
        if (!coal.isGreaterThanOrEqual(cost)) {
            return new Result(false, tool, amount, cost, cost.subtract(coal), false);
        }
        // 浮動小数の誤差で負にならないよう 0 で止める
        data.setCoal(coal.subtract(cost).max(BigNum.ZERO));
        data.addTool(tool.id(), amount);
        boolean first = data.markExplained(tool.id());
        return new Result(true, tool, amount, cost, BigNum.ZERO, first);
    }

    /** 今の石炭で買える個数（最大 {@code max} 個）。 */
    public static long affordableCount(PlayerData data, Tool tool, long max) {
        long owned = data.getToolCount(tool.id());
        BigNum coal = data.coal();
        // 価格は個数に対して単調に増えるので二分探索で求める
        long low = 0;
        long high = Math.max(0, max);
        while (low < high) {
            long mid = low + (high - low + 1) / 2;
            if (coal.isGreaterThanOrEqual(Formulas.totalPrice(tool, owned, mid))) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }
}
