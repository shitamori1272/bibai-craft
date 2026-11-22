package io.github.shitamori1272.coalclicker.shop;

import java.util.List;

/**
 * ショップ画面（6 段・54 マス）のマスの割り当てと、改良のページ分け。Bukkit に依存しない。
 *
 * <pre>
 *  0  1  2 [石炭][毎秒][手掘り] 6  7  8     1 段目：上段の表示（中央）
 * [道][道][道][道] 13 [道][道][道][道]      2 段目：道具 8 種類
 * 18 19 20 21 [改良の見出し] 23 24 25 26   3 段目：区切り
 * [改][改][改][改][改][改][改][改][改]      4〜5 段目：買える改良（1 ページ 18 個）
 * [改][改][改][改][改][改][改][改][改]
 * [前] 46 47 48 [次の改良] 50 51 52 [次]   6 段目：ページ送り・次の改良のヒント
 * </pre>
 */
public final class ShopLayout {

    public static final int ROWS = 6;
    public static final int SIZE = ROWS * 9;

    public static final int COAL_SLOT = 3;
    public static final int RATE_SLOT = 4;
    public static final int CLICK_SLOT = 5;

    /** 道具を置く場所。{@code Tool.values()} の順。 */
    static final int[] TOOL_SLOTS = {9, 10, 11, 12, 14, 15, 16, 17};

    public static final int UPGRADE_HEADER_SLOT = 22;
    /** 改良を置く場所の先頭と数（4〜5 段目）。 */
    public static final int FIRST_UPGRADE_SLOT = 27;
    public static final int UPGRADES_PER_PAGE = 18;

    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int LOCKED_HINT_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 53;

    private ShopLayout() {
    }

    /** 道具の番号（{@code Tool.values()} の添字）のマス。 */
    public static int toolSlot(int index) {
        return TOOL_SLOTS[index];
    }

    /** 置ける道具の数。 */
    public static int toolSlotCount() {
        return TOOL_SLOTS.length;
    }

    /** そのマスにある道具の番号。道具のマスでなければ −1。 */
    public static int toolIndexAt(int slot) {
        for (int i = 0; i < TOOL_SLOTS.length; i++) {
            if (TOOL_SLOTS[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    /** ページの中で {@code index} 番目（0 始まり）の改良のマス。 */
    public static int upgradeSlot(int index) {
        if (index < 0 || index >= UPGRADES_PER_PAGE) {
            throw new IndexOutOfBoundsException("改良のマスは 0〜" + (UPGRADES_PER_PAGE - 1) + ": " + index);
        }
        return FIRST_UPGRADE_SLOT + index;
    }

    /** そのマスが、ページの中で何番目の改良のマスか。改良のマスでなければ −1。 */
    public static int upgradeIndexAt(int slot) {
        int index = slot - FIRST_UPGRADE_SLOT;
        return index >= 0 && index < UPGRADES_PER_PAGE ? index : -1;
    }

    /** 改良が {@code count} 個のときのページ数。0 個でも 1 ページ（空のページ）とする。 */
    public static int pageCount(int count) {
        if (count <= 0) {
            return 1;
        }
        return (count + UPGRADES_PER_PAGE - 1) / UPGRADES_PER_PAGE;
    }

    /** ページ番号（0 始まり）を 0〜最後のページに収める。買って改良が減り、ページが消えたとき用。 */
    public static int clampPage(int page, int count) {
        return Math.clamp(page, 0, pageCount(count) - 1);
    }

    /** {@code page} ページ目（0 始まり。範囲外なら収める）に並べる分。 */
    public static <T> List<T> pageOf(List<T> items, int page) {
        int p = clampPage(page, items.size());
        int from = p * UPGRADES_PER_PAGE;
        int to = Math.min(items.size(), from + UPGRADES_PER_PAGE);
        return List.copyOf(items.subList(from, to));
    }

    /** 前のページがあるか。 */
    public static boolean hasPreviousPage(int page, int count) {
        return clampPage(page, count) > 0;
    }

    /** 次のページがあるか。 */
    public static boolean hasNextPage(int page, int count) {
        return clampPage(page, count) < pageCount(count) - 1;
    }
}
