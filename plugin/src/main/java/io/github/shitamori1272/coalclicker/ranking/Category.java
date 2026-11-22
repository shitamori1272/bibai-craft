package io.github.shitamori1272.coalclicker.ranking;

import io.github.shitamori1272.coalclicker.hud.HudText;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/** ランキングの種類（{@code /coal top [mined|depth|rate]}）。 */
public enum Category {
    /** 累計採掘量。 */
    MINED("mined", "累計採掘量", Comparator.comparing(PlayerStats::totalMined)) {
        @Override
        public String format(PlayerStats stats) {
            return JapaneseFormat.format(stats.totalMined()) + "個";
        }
    },
    /** 到達した一番深い坑道。 */
    DEPTH("depth", "到達深さ", Comparator.comparingLong(PlayerStats::deepestLevel)) {
        @Override
        public String format(PlayerStats stats) {
            long level = stats.deepestLevel();
            return HudText.tunnelName(level) + "（深さ" + HudText.depth(level) + "m）";
        }
    },
    /** 今の坑道での毎秒採掘量。 */
    RATE("rate", "毎秒採掘量", Comparator.comparing(PlayerStats::coalPerSecond)) {
        @Override
        public String format(PlayerStats stats) {
            return "毎秒 " + JapaneseFormat.format(stats.coalPerSecond()) + "個";
        }
    };

    private final String id;
    private final String displayName;
    private final Comparator<PlayerStats> ascending;

    Category(String id, String displayName, Comparator<PlayerStats> ascending) {
        this.id = id;
        this.displayName = displayName;
        this.ascending = ascending;
    }

    /** コマンドで指定する名前（例: {@code mined}）。 */
    public String id() {
        return id;
    }

    /** 見出しに出す名前。 */
    public String displayName() {
        return displayName;
    }

    /** 値の小さい順の比較。順位は値の大きい順につける。 */
    Comparator<PlayerStats> ascending() {
        return ascending;
    }

    /** 表示用の値。 */
    public abstract String format(PlayerStats stats);

    /** 名前から探す（大文字小文字は区別しない）。 */
    public static Optional<Category> byId(String id) {
        String key = id.toLowerCase(Locale.ROOT);
        for (Category category : values()) {
            if (category.id.equals(key)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
