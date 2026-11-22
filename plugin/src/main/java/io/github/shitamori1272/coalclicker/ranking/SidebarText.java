package io.github.shitamori1272.coalclicker.ranking;

import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;

/** サイドバーに出す文字列。Bukkit に依存しない。 */
public final class SidebarText {

    /** サイドバーの見出し。 */
    public static final String TITLE = "石炭クリッカー";

    private SidebarText() {
    }

    /** 例: {@code 全体の出炭量 1.234万個}。JapaneseFormat は有効数字 4 桁なので、どんな値でも短く収まる。 */
    public static String serverTotal(BigNum serverTotal) {
        return "全体の出炭量 " + JapaneseFormat.format(serverTotal) + "個";
    }
}
