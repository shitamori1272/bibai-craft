package io.github.shitamori1272.coalclicker.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.game.Tool;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ShopLayoutTest {

    @Test
    void 道具8種類がすべて並び_マスが重ならない() {
        assertEquals(Tool.values().length, ShopLayout.toolSlotCount());
        Set<Integer> used = new HashSet<>(List.of(ShopLayout.COAL_SLOT, ShopLayout.RATE_SLOT, ShopLayout.CLICK_SLOT,
                ShopLayout.UPGRADE_HEADER_SLOT, ShopLayout.PREVIOUS_PAGE_SLOT, ShopLayout.LOCKED_HINT_SLOT,
                ShopLayout.NEXT_PAGE_SLOT));
        assertEquals(7, used.size());
        for (int i = 0; i < ShopLayout.toolSlotCount(); i++) {
            assertTrue(used.add(ShopLayout.toolSlot(i)));
            assertEquals(i, ShopLayout.toolIndexAt(ShopLayout.toolSlot(i)));
        }
        for (int i = 0; i < ShopLayout.UPGRADES_PER_PAGE; i++) {
            assertTrue(used.add(ShopLayout.upgradeSlot(i)));
            assertEquals(i, ShopLayout.upgradeIndexAt(ShopLayout.upgradeSlot(i)));
        }
        for (int slot : used) {
            assertTrue(slot >= 0 && slot < ShopLayout.SIZE);
        }
    }

    @Test
    void 上段は1段目の中央() {
        assertEquals(List.of(3, 4, 5), List.of(ShopLayout.COAL_SLOT, ShopLayout.RATE_SLOT, ShopLayout.CLICK_SLOT));
    }

    @Test
    void ほかのマスは道具でも改良でもない() {
        assertEquals(-1, ShopLayout.toolIndexAt(ShopLayout.COAL_SLOT));
        assertEquals(-1, ShopLayout.upgradeIndexAt(ShopLayout.PREVIOUS_PAGE_SLOT));
        assertEquals(-1, ShopLayout.upgradeIndexAt(ShopLayout.UPGRADE_HEADER_SLOT));
        assertEquals(-1, ShopLayout.toolIndexAt(13));
    }

    @Test
    void ページ数() {
        assertEquals(1, ShopLayout.pageCount(0));
        assertEquals(1, ShopLayout.pageCount(1));
        assertEquals(1, ShopLayout.pageCount(18));
        assertEquals(2, ShopLayout.pageCount(19));
        assertEquals(3, ShopLayout.pageCount(37));
    }

    @Test
    void ページの切り出しと範囲外の補正() {
        List<Integer> items = IntStream.range(0, 20).boxed().toList();
        assertEquals(IntStream.range(0, 18).boxed().toList(), ShopLayout.pageOf(items, 0));
        assertEquals(List.of(18, 19), ShopLayout.pageOf(items, 1));
        // 買って減ったなどでページがなくなったら最後のページ
        assertEquals(List.of(18, 19), ShopLayout.pageOf(items, 5));
        assertEquals(1, ShopLayout.clampPage(5, 20));
        assertEquals(0, ShopLayout.clampPage(-1, 20));
        assertEquals(List.of(), ShopLayout.pageOf(List.of(), 3));
    }

    @Test
    void 前後のページの有無() {
        assertFalse(ShopLayout.hasPreviousPage(0, 40));
        assertTrue(ShopLayout.hasNextPage(0, 40));
        assertTrue(ShopLayout.hasPreviousPage(1, 40));
        assertTrue(ShopLayout.hasNextPage(1, 40));
        assertFalse(ShopLayout.hasNextPage(2, 40));
        assertFalse(ShopLayout.hasNextPage(0, 18));
        assertFalse(ShopLayout.hasPreviousPage(0, 0));
    }
}
