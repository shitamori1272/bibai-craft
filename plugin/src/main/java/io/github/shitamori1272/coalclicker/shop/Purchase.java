package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Objects;

/** 道具と改良の購入処理。Bukkit に依存しないので単体テストできる。 */
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

    /**
     * 改良の購入の結果。
     *
     * @param success 買えたか
     * @param cost 価格
     * @param shortage 足りなかった石炭（買えたとき・買えない状態だったときは 0）
     * @param firstPurchase はじめて買った（解説をまだ出していなかった）か
     */
    public record UpgradeResult(boolean success, Upgrade upgrade, BigNum cost, BigNum shortage,
            boolean firstPurchase) {
    }

    /** 改良を買う。出現していない・買ってある・石炭が足りないときは何も変えない。 */
    public static UpgradeResult tryBuy(PlayerData data, Upgrade upgrade) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(upgrade);
        BigNum cost = upgrade.price();
        if (!upgrade.isAvailable(data)) {
            return new UpgradeResult(false, upgrade, cost, BigNum.ZERO, false);
        }
        BigNum coal = data.coal();
        if (!coal.isGreaterThanOrEqual(cost)) {
            return new UpgradeResult(false, upgrade, cost, cost.subtract(coal), false);
        }
        data.setCoal(coal.subtract(cost).max(BigNum.ZERO));
        data.addUpgrade(upgrade.id());
        boolean first = data.markExplained(upgrade.id());
        return new UpgradeResult(true, upgrade, cost, BigNum.ZERO, first);
    }

    /** 今の石炭で買える個数（最大 {@code max} 個）。 */
    public static long affordableCount(PlayerData data, Tool tool, long max) {
        return affordableCount(data.coal(), tool, data.getToolCount(tool.id()), max);
    }

    /** 石炭 {@code coal} で、所持数 {@code owned} から買える個数（最大 {@code max} 個）。 */
    public static long affordableCount(BigNum coal, Tool tool, long owned, long max) {
        // 合計価格（等比数列の和）は個数に対して単調に増えるので二分探索で求める
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
