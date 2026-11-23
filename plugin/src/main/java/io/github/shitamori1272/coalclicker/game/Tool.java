package io.github.shitamori1272.coalclicker.game;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Optional;

/**
 * ショップで買える道具（企画書 6-1。クッキークリッカーの建物）。何個でも買え、1 つごとに毎秒の石炭が増える。
 *
 * <p>アイコンは Bukkit に依存しないよう、Material の名前を文字列で持つ。並び順はショップの表示順（安い順）。
 */
public enum Tool {
    LAMP("lamp", "安全灯", 150, 1, "LANTERN",
            "坑内を照らす、火が外に出ない灯り",
            "暗い坑内で欠かせない明かり。炭鉱には燃えやすいガスがたまることがあり、火が外に出ない仕組みの灯りが使われました。"),
    MINECART("minecart", "炭車", 1_000, 10, "MINECART",
            "掘った石炭をレールで運ぶ車",
            "掘った石炭を積んでレールの上を運ぶ車。坑道には何両もの炭車がつながって走りました。"),
    BLASTING("blasting", "発破", 11_000, 80, "TNT",
            "火薬で岩盤をまとめて砕く",
            "岩盤に穴をあけて火薬を詰め、一度にまとめて砕く方法。硬い岩の中に坑道を掘り進めるのに欠かせませんでした。"),
    COAL_PICK("coal_pick", "コールピック", 120_000, 470, "IRON_PICKAXE",
            "圧縮空気で打ち付ける採炭機",
            "圧縮空気の力で先端を何度も打ち付ける採炭機。坑夫 1 人あたりの採炭量を大きく増やしました。"),
    CONVEYOR("conveyor", "ベルトコンベア", 1_300_000, 2_600, "POWERED_RAIL",
            "切羽から休みなく石炭を運び出す",
            "掘った石炭を切羽から休みなく運び出す設備。運び出しの待ち時間が減り、掘る時間が増えました。"),
    DRUM_CUTTER("drum_cutter", "ドラムカッター", 14_000_000, 14_000, "GRINDSTONE",
            "回転するドラムで炭壁を削る採炭機",
            "長い切羽に沿って走りながら、回転するドラムで炭壁を削り、そのままコンベアに積み込む採炭機。"),
    SHIELD("shield", "自走枠", 200_000_000, 78_000, "IRON_BARS",
            "天井を支えながら自分で進む鋼鉄の枠",
            "油圧で天井を支えながら、自分で前へ進む鋼鉄の枠。長い切羽でも安全に掘れるようになりました。"),
    HEADFRAME("headframe", "立坑櫓", 3_300_000_000.0, 440_000, "LIGHTNING_ROD",
            "ケージで人や石炭を上げ下げする櫓",
            "地下深くまで続く立坑の上に建つ櫓。巻揚機の滑車でケージを上げ下げし、人や石炭を運びました。"
                    + "美唄には今も赤い立坑櫓が残っています。");

    private final String id;
    private final String displayName;
    private final BigNum basePrice;
    private final BigNum baseCoalPerSecond;
    private final String iconMaterial;
    private final String shortDescription;
    private final String explanation;

    Tool(String id, String displayName, double basePrice, double baseCoalPerSecond, String iconMaterial,
            String shortDescription, String explanation) {
        this.id = id;
        this.displayName = displayName;
        this.basePrice = BigNum.of(basePrice);
        this.baseCoalPerSecond = BigNum.of(baseCoalPerSecond);
        this.iconMaterial = iconMaterial;
        this.shortDescription = shortDescription;
        this.explanation = explanation;
    }

    /** 保存やコマンドで使う ID（例: {@code lamp}）。 */
    public String id() {
        return id;
    }

    /** 日本語の名前（例: 安全灯）。 */
    public String displayName() {
        return displayName;
    }

    /** 1 つ目の価格。買うたびに ×{@link Formulas#PRICE_GROWTH}。 */
    public BigNum basePrice() {
        return basePrice;
    }

    /** 1 つあたりの毎秒の石炭（改良・全体・深さの倍率を掛ける前）。 */
    public BigNum baseCoalPerSecond() {
        return baseCoalPerSecond;
    }

    /** アイコンにする Material の名前（例: {@code LANTERN}）。 */
    public String iconMaterial() {
        return iconMaterial;
    }

    /** 1 行の短い解説。ショップの説明文用。 */
    public String shortDescription() {
        return shortDescription;
    }

    /** はじめて買ったときにチャットに出す詳しい解説（企画書 6-4）。 */
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
