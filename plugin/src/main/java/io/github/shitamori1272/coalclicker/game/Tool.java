package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Optional;

/**
 * ショップで買える道具（企画書 6 章。MVP は 6 種類）。
 *
 * <p>アイコンは Bukkit に依存しないよう、Material の名前を文字列で持つ。
 */
public enum Tool {
    PICKAXE("pickaxe", "ツルハシ", ToolKind.MANUAL, 500, 2.0, 100, "WOODEN_PICKAXE",
            "1m 崩したときの石炭 +100 個",
            "手掘りの基本の道具",
            "炭鉱の始まりは手掘りでした。炭壁の下に切り込みを入れ、楔で石炭を割り落とします。"),
    LAMP("lamp", "安全灯", ToolKind.COAL, 150, 1.15, 10, "LANTERN",
            "採炭力 +10",
            "坑内を照らす、火が外に出ない灯り",
            "暗い坑内で欠かせない明かり。炭鉱には燃えやすいガスがたまることがあり、火が外に出ない仕組みの灯りが使われました。"),
    MINECART("minecart", "炭車", ToolKind.COAL, 1_000, 1.15, 100, "MINECART",
            "採炭力 +100",
            "掘った石炭をレールで運ぶ車",
            "掘った石炭を積んでレールの上を運ぶ車。坑道には何両もの炭車がつながって走りました。"),
    COAL_PICK("coal_pick", "コールピック", ToolKind.COAL, 12_000, 1.15, 1_000, "IRON_PICKAXE",
            "採炭力 +1,000",
            "圧縮空気で打ち付ける採炭機",
            "圧縮空気の力で先端を何度も打ち付ける採炭機。坑夫 1 人あたりの採炭量を大きく増やしました。"),
    BLASTING("blasting", "発破", ToolKind.DIG, 200, 1.15, 1, "TNT",
            "掘進力 +1",
            "火薬で岩盤をまとめて砕く",
            "岩盤に穴をあけて火薬を詰め、一度にまとめて砕く方法。硬い岩の中に坑道を掘り進めるのに欠かせませんでした。"),
    DRILL("drill", "削岩機", ToolKind.DIG, 5_000, 1.15, 10, "PISTON",
            "掘進力 +10",
            "圧縮空気で発破用の穴をあける機械",
            "圧縮空気の力で岩に発破用の穴をあける機械。手作業より何倍も速く穴をあけられ、坑道を掘る速さが大きく上がりました。");

    private final String id;
    private final String displayName;
    private final ToolKind kind;
    private final BigNum basePrice;
    private final double priceGrowth;
    private final double effect;
    private final String iconMaterial;
    private final String effectDescription;
    private final String shortDescription;
    private final String explanation;

    Tool(String id, String displayName, ToolKind kind, double basePrice, double priceGrowth, double effect,
            String iconMaterial, String effectDescription, String shortDescription, String explanation) {
        this.id = id;
        this.displayName = displayName;
        this.kind = kind;
        this.basePrice = BigNum.of(basePrice);
        this.priceGrowth = priceGrowth;
        this.effect = effect;
        this.iconMaterial = iconMaterial;
        this.effectDescription = effectDescription;
        this.shortDescription = shortDescription;
        this.explanation = explanation;
    }

    /** 保存やコマンドで使う ID（例: {@code pickaxe}）。 */
    public String id() {
        return id;
    }

    /** 日本語の名前（例: ツルハシ）。 */
    public String displayName() {
        return displayName;
    }

    public ToolKind kind() {
        return kind;
    }

    /** 1 つ目の価格。 */
    public BigNum basePrice() {
        return basePrice;
    }

    /** 1 つ買うごとに価格に掛かる倍率（ツルハシ ×2.0、ほかは ×1.15）。 */
    public double priceGrowth() {
        return priceGrowth;
    }

    /**
     * 1 つあたりの効果量。種類によって意味が違う。
     *
     * <ul>
     *   <li>{@link ToolKind#MANUAL}：1m 崩したときの石炭の増分（深さ倍率を掛ける前）
     *   <li>{@link ToolKind#COAL}：採炭力 E の増分
     *   <li>{@link ToolKind#DIG}：掘進力 D の増分
     * </ul>
     */
    public double effect() {
        return effect;
    }

    /** アイコンにする Material の名前（例: {@code LANTERN}）。 */
    public String iconMaterial() {
        return iconMaterial;
    }

    /** 効果の説明（例: 採炭力 +10）。ショップの説明文用。 */
    public String effectDescription() {
        return effectDescription;
    }

    /** 1 行の短い解説。ショップの説明文用。 */
    public String shortDescription() {
        return shortDescription;
    }

    /** はじめて買ったときにチャットに出す詳しい解説（企画書 6 章「解説文（仮）」）。 */
    public String explanation() {
        return explanation;
    }

    /** ID から道具を探す。 */
    public static Optional<Tool> byId(String id) {
        for (Tool tool : values()) {
            if (tool.id.equals(id)) {
                return Optional.of(tool);
            }
        }
        return Optional.empty();
    }
}
