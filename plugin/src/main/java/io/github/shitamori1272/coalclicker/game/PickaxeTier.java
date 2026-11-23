package io.github.shitamori1272.coalclicker.game;

import java.util.Set;

/**
 * 入坑したときに渡すツルハシの見た目（企画書 7 章）。買ったツルハシの改良の数で素材が上がる。
 *
 * <table>
 *   <tr><th>改良の数</th><th>素材</th></tr>
 *   <tr><td>0</td><td>木</td></tr>
 *   <tr><td>1</td><td>石</td></tr>
 *   <tr><td>2</td><td>鉄</td></tr>
 *   <tr><td>3</td><td>ダイヤモンド</td></tr>
 * </table>
 *
 * <p>Bukkit に頼らない計算だけを置く（素材は {@code Material} の名前で返す）。
 */
public final class PickaxeTier {

    /** 段階ごとの素材（{@code Material} の名前）。 */
    static final String[] MATERIALS = {"WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "DIAMOND_PICKAXE"};
    /** 段階ごとの名前。 */
    static final String[] NAMES = {"木のツルハシ", "石のツルハシ", "鉄のツルハシ", "ダイヤのツルハシ"};

    /** いちばん上の段階。 */
    public static final int MAX = MATERIALS.length - 1;

    private PickaxeTier() {
    }

    /** 買った改良から、渡すツルハシの段階（0〜{@link #MAX}）を求める。 */
    public static int of(Set<String> upgrades) {
        return Math.min(Formulas.pickaxeUpgradeCount(upgrades), MAX);
    }

    /** 段階の素材（{@code Material} の名前）。範囲の外は端に寄せる。 */
    public static String material(int tier) {
        return MATERIALS[clamp(tier)];
    }

    /** 段階の名前（例: 鉄のツルハシ）。範囲の外は端に寄せる。 */
    public static String displayName(int tier) {
        return NAMES[clamp(tier)];
    }

    private static int clamp(int tier) {
        return Math.clamp(tier, 0, MAX);
    }
}
