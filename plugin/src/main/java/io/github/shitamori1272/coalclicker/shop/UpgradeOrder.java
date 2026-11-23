package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.game.GameState;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ショップ画面を開いている間の、改良の並び（どの改良がどのマスにあるか）。Bukkit に依存しない。
 *
 * <p>毎秒の描き直しで並べ直すと、買った直後に別の改良がそのマスへずれてきて、続けてクリックしたときに
 * 思っていないものを買ってしまう。そこで並びは開いている間は動かさない：
 * <ul>
 *   <li>買った改良はそのマスに「購入済み」として残す（クリックしても買えない）
 *   <li>新しく買えるようになった改良は最後の空きに足す
 *   <li>並べ直し（安い順・詰める）は {@link #reset} のとき（ページを変えたとき・開き直したとき）だけ
 * </ul>
 */
final class UpgradeOrder {

    /** マスの状態。 */
    enum Status {
        /** 買える。クリックで購入。 */
        AVAILABLE,
        /** この画面を開いてから買った。 */
        PURCHASED,
        /** 買えなくなった（管理者のリセットなど）。空きマスとして出す。 */
        GONE
    }

    /** 1 マスの中身。 */
    record Cell(Upgrade upgrade, Status status) {
    }

    private final List<Upgrade> order = new ArrayList<>();
    private final Set<Upgrade> placed = new HashSet<>();

    /** 並びを作り直す（安い順・詰める）。 */
    void reset(List<Upgrade> sortedAvailable) {
        order.clear();
        placed.clear();
        for (Upgrade upgrade : sortedAvailable) {
            if (placed.add(upgrade)) {
                order.add(upgrade);
            }
        }
    }

    /** 今の並びを保ったまま、新しく買えるようになった改良を最後に足す。 */
    void update(List<Upgrade> sortedAvailable) {
        for (Upgrade upgrade : sortedAvailable) {
            if (placed.add(upgrade)) {
                order.add(upgrade);
            }
        }
    }

    /** 並んでいるマスの数（購入済みのマスも含む）。ページ数はこれで決まる。 */
    int size() {
        return order.size();
    }

    /** {@code page} ページ目（0 始まり。範囲外なら収める）のマスの中身。 */
    List<Cell> page(int page, GameState state) {
        List<Cell> cells = new ArrayList<>();
        for (Upgrade upgrade : ShopLayout.pageOf(order, page)) {
            Status status;
            if (upgrade.isAvailable(state)) {
                status = Status.AVAILABLE;
            } else if (upgrade.isOwned(state)) {
                status = Status.PURCHASED;
            } else {
                status = Status.GONE;
            }
            cells.add(new Cell(upgrade, status));
        }
        return cells;
    }
}
