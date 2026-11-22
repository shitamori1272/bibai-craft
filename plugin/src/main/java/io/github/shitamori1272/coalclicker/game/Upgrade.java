package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 1 回だけ買える改良（企画書 6-2。クッキークリッカーのアップグレード）。
 *
 * <ul>
 *   <li>道具の改良（道具ごとに 7 段階）：その道具を 1・5・25・50・100・150・200 個持つと出現。
 *       価格は道具の初期価格 × 10・50・500・5 万・500 万・5 億・500 億。その道具の効果 ×2。
 *       ID は {@code <道具ID>_<段階>}（例: {@code lamp_1}〜{@code lamp_7}）
 *   <li>ツルハシの改良（3 段階）：手掘り 100・1,000・1 万回で出現。価格 1,000・10 万・1,000 万。手掘り ×2。
 *       ID は {@code pickaxe_1}〜{@code pickaxe_3}
 *   <li>熟練の改良（5 段階）：累計採掘量 5 万・500 万・5 億・500 億・5 兆で出現。価格も同じ。
 *       手掘りに毎秒の石炭の 1% を上乗せ。ID は {@code skill_1}〜{@code skill_5}
 * </ul>
 *
 * <p>名前と解説は仮（#64 で史実に合わせて直す）。並び順は {@link #values()} の順（道具の改良 → ツルハシ → 熟練）。
 */
public final class Upgrade {

    /** 改良の種類。 */
    public enum Kind {
        /** 道具の改良。その道具の効果 ×2。 */
        TOOL,
        /** ツルハシの改良。手掘り ×2。 */
        PICKAXE,
        /** 熟練の改良。手掘りに毎秒の石炭の 1% を上乗せ。 */
        SKILL
    }

    /** 道具の改良が出現する所持数（段階 1〜7）。 */
    static final long[] TOOL_TIER_OWNED = {1, 5, 25, 50, 100, 150, 200};
    /** 道具の改良の価格 ＝ 道具の初期価格 × この値。 */
    static final double[] TOOL_TIER_PRICE_FACTOR = {10, 50, 500, 5e4, 5e6, 5e8, 5e10};
    /** ツルハシの改良が出現する手掘りの回数と価格。 */
    static final long[] PICKAXE_CLICKS = {100, 1_000, 10_000};
    static final double[] PICKAXE_PRICE = {1_000, 1e5, 1e7};
    /** 熟練の改良が出現する累計採掘量（価格も同じ）。 */
    static final double[] SKILL_TOTAL_MINED = {5e4, 5e6, 5e8, 5e10, 5e12};

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII"};

    private static final List<Upgrade> VALUES;
    private static final Map<String, Upgrade> BY_ID;

    static {
        List<Upgrade> list = new ArrayList<>();
        for (Tool tool : Tool.values()) {
            for (int i = 0; i < TOOL_TIER_OWNED.length; i++) {
                int tier = i + 1;
                list.add(new Upgrade(tool.id() + "_" + tier, Kind.TOOL, tool, tier,
                        tool.displayName() + "の改良 " + ROMAN[i],
                        tool.basePrice().multiply(TOOL_TIER_PRICE_FACTOR[i]),
                        BigNum.of(TOOL_TIER_OWNED[i]),
                        tool.displayName() + "の効果 ×2",
                        tool.displayName() + "をさらに使いこなす工夫。（仮の解説）"));
            }
        }
        for (int i = 0; i < PICKAXE_CLICKS.length; i++) {
            int tier = i + 1;
            list.add(new Upgrade("pickaxe_" + tier, Kind.PICKAXE, null, tier,
                    "ツルハシの改良 " + ROMAN[i],
                    BigNum.of(PICKAXE_PRICE[i]),
                    BigNum.of(PICKAXE_CLICKS[i]),
                    "手掘り ×2",
                    "よく手になじむツルハシで、1 回の手掘りで採れる石炭が増えます。（仮の解説）"));
        }
        for (int i = 0; i < SKILL_TOTAL_MINED.length; i++) {
            int tier = i + 1;
            list.add(new Upgrade("skill_" + tier, Kind.SKILL, null, tier,
                    "熟練の改良 " + ROMAN[i],
                    BigNum.of(SKILL_TOTAL_MINED[i]),
                    BigNum.of(SKILL_TOTAL_MINED[i]),
                    "手掘りに毎秒の石炭の 1% を上乗せ",
                    "経験を積んだ坑夫は、炭層の目を読んで効率よく掘ります。（仮の解説）"));
        }
        VALUES = Collections.unmodifiableList(list);
        Map<String, Upgrade> byId = new LinkedHashMap<>();
        for (Upgrade upgrade : list) {
            if (byId.put(upgrade.id, upgrade) != null) {
                throw new IllegalStateException("改良の ID が重複しています: " + upgrade.id);
            }
        }
        BY_ID = Collections.unmodifiableMap(byId);
    }

    private final String id;
    private final Kind kind;
    private final Tool tool;
    private final int tier;
    private final String displayName;
    private final BigNum price;
    private final BigNum threshold;
    private final String effectDescription;
    private final String explanation;

    private Upgrade(String id, Kind kind, Tool tool, int tier, String displayName, BigNum price, BigNum threshold,
            String effectDescription, String explanation) {
        this.id = id;
        this.kind = kind;
        this.tool = tool;
        this.tier = tier;
        this.displayName = displayName;
        this.price = price;
        this.threshold = threshold;
        this.effectDescription = effectDescription;
        this.explanation = explanation;
    }

    /** すべての改良（表示順）。 */
    public static List<Upgrade> values() {
        return VALUES;
    }

    /** ID から改良を探す。 */
    public static Optional<Upgrade> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    /** 道具の改良（段階は 1〜7）。 */
    public static Upgrade toolTier(Tool tool, int tier) {
        return byId(Objects.requireNonNull(tool).id() + "_" + tier)
                .orElseThrow(() -> new IllegalArgumentException("段階は 1〜7 にしてください: " + tier));
    }

    /** 出現条件を満たしていて、まだ買っていない改良（表示順）。 */
    public static List<Upgrade> available(GameState state) {
        List<Upgrade> list = new ArrayList<>();
        for (Upgrade upgrade : VALUES) {
            if (upgrade.isAvailable(state)) {
                list.add(upgrade);
            }
        }
        return list;
    }

    /** 保存に使う ID（例: {@code lamp_1}・{@code pickaxe_2}・{@code skill_3}）。 */
    public String id() {
        return id;
    }

    public Kind kind() {
        return kind;
    }

    /** 道具の改良なら、その道具。 */
    public Optional<Tool> tool() {
        return Optional.ofNullable(tool);
    }

    /** 段階（1 始まり）。 */
    public int tier() {
        return tier;
    }

    /** 日本語の名前（仮。例: 安全灯の改良 I）。 */
    public String displayName() {
        return displayName;
    }

    /** 価格。 */
    public BigNum price() {
        return price;
    }

    /**
     * 出現条件のしきい値。種類によって意味が違う（道具の所持数・手掘りの回数・累計採掘量）。
     */
    public BigNum threshold() {
        return threshold;
    }

    /** 効果の説明（例: 安全灯の効果 ×2）。 */
    public String effectDescription() {
        return effectDescription;
    }

    /** はじめて買ったときにチャットに出す解説（仮）。 */
    public String explanation() {
        return explanation;
    }

    /** 出現条件を満たしているか（買ったかどうかは見ない）。 */
    public boolean isUnlocked(GameState state) {
        return switch (kind) {
            case TOOL -> BigNum.of(state.toolCount(tool)).isGreaterThanOrEqual(threshold);
            case PICKAXE -> BigNum.of(state.clicks()).isGreaterThanOrEqual(threshold);
            case SKILL -> state.totalMined().isGreaterThanOrEqual(threshold);
        };
    }

    /** 買ってあるか。 */
    public boolean isOwned(GameState state) {
        return state.upgrades().contains(id);
    }

    /** 買える状態か ＝ 出現していて、まだ買っていない。 */
    public boolean isAvailable(GameState state) {
        return isUnlocked(state) && !isOwned(state);
    }

    @Override
    public String toString() {
        return id;
    }
}
