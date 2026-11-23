package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Map;
import java.util.Set;

/**
 * 計算式が読むプレイヤーの値（読み取り専用の見方）。{@code data.PlayerData} が実装する。
 * テストでは {@link #of} で手軽に作れる。
 */
public interface GameState {

    /** 道具の所持数（道具 ID → 個数）。知らない ID は無視される。 */
    Map<String, Long> tools();

    /** 買った改良の ID。 */
    Set<String> upgrades();

    /** 手掘りの回数。 */
    long clicks();

    /** 累計採掘量。 */
    BigNum totalMined();

    /** 道具の所持数。持っていなければ 0。 */
    default long toolCount(Tool tool) {
        return Formulas.count(tools(), tool);
    }

    /** 改良を買ってあるか。 */
    default boolean hasUpgrade(Upgrade upgrade) {
        return upgrades().contains(upgrade.id());
    }

    /** 値を固定した GameState を作る（テストや計算の試し用）。 */
    static GameState of(Map<String, Long> tools, Set<String> upgrades, long clicks, BigNum totalMined) {
        record Snapshot(Map<String, Long> tools, Set<String> upgrades, long clicks, BigNum totalMined)
                implements GameState {
        }
        return new Snapshot(Map.copyOf(tools), Set.copyOf(upgrades), clicks, totalMined);
    }
}
