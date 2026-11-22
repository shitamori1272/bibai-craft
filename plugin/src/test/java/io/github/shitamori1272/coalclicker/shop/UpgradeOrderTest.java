package io.github.shitamori1272.coalclicker.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.game.GameState;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UpgradeOrderTest {

    private static final Upgrade PICKAXE_1 = Upgrade.byId("pickaxe_1").orElseThrow();
    private static final Upgrade LAMP_1 = Upgrade.byId("lamp_1").orElseThrow();
    private static final Upgrade LAMP_2 = Upgrade.byId("lamp_2").orElseThrow();

    private static GameState state(long lamps, Set<String> upgrades, long clicks) {
        Map<String, Long> tools = new HashMap<>();
        tools.put("lamp", lamps);
        return GameState.of(tools, upgrades, clicks, BigNum.ZERO);
    }

    private static List<String> describe(List<UpgradeOrder.Cell> cells) {
        return cells.stream().map(c -> c.upgrade().id() + ":" + c.status()).toList();
    }

    @Test
    void 買った改良はそのマスに購入済みとして残り_ほかはずれない() {
        UpgradeOrder order = new UpgradeOrder();
        GameState before = state(1, Set.of(), 100);
        order.reset(ShopText.availableByPrice(before));
        assertEquals(List.of("pickaxe_1:AVAILABLE", "lamp_1:AVAILABLE"), describe(order.page(0, before)));

        // pickaxe_1 を買っても lamp_1 は 2 マス目のまま
        GameState after = state(1, Set.of("pickaxe_1"), 100);
        order.update(ShopText.availableByPrice(after));
        assertEquals(List.of("pickaxe_1:PURCHASED", "lamp_1:AVAILABLE"), describe(order.page(0, after)));
    }

    @Test
    void 新しく出た改良は安くても最後に足す() {
        UpgradeOrder order = new UpgradeOrder();
        GameState before = state(5, Set.of(), 0);
        order.reset(ShopText.availableByPrice(before));
        assertEquals(List.of("lamp_1:AVAILABLE", "lamp_2:AVAILABLE"), describe(order.page(0, before)));

        // 手掘り 100 回で pickaxe_1（1,000。いちばん安い）が出ても先頭には割り込まない
        GameState after = state(5, Set.of(), 100);
        order.update(ShopText.availableByPrice(after));
        assertEquals(List.of("lamp_1:AVAILABLE", "lamp_2:AVAILABLE", "pickaxe_1:AVAILABLE"),
                describe(order.page(0, after)));
    }

    @Test
    void 作り直すと安い順に詰める() {
        UpgradeOrder order = new UpgradeOrder();
        GameState first = state(5, Set.of(), 0);
        order.reset(ShopText.availableByPrice(first));
        GameState later = state(5, Set.of("lamp_1"), 100);
        order.update(ShopText.availableByPrice(later));
        assertEquals(3, order.size());

        order.reset(ShopText.availableByPrice(later));
        assertEquals(List.of("pickaxe_1:AVAILABLE", "lamp_2:AVAILABLE"), describe(order.page(0, later)));
        assertEquals(2, order.size());
    }

    @Test
    void 買えなくなった改良は空きになる() {
        UpgradeOrder order = new UpgradeOrder();
        GameState before = state(1, Set.of(), 0);
        order.reset(ShopText.availableByPrice(before));
        // 管理者のリセットなどで安全灯がなくなった
        GameState after = state(0, Set.of(), 0);
        order.update(ShopText.availableByPrice(after));
        assertEquals(List.of("lamp_1:GONE"), describe(order.page(0, after)));
    }

    @Test
    void 同じ改良を二重に並べない() {
        UpgradeOrder order = new UpgradeOrder();
        order.reset(List.of(LAMP_1, LAMP_1, LAMP_2));
        order.update(List.of(LAMP_2, LAMP_1, PICKAXE_1));
        assertEquals(3, order.size());
    }

    @Test
    void 中身が変わった直後のマスはしばらくクリックを受け付けない() {
        SlotGuard guard = new SlotGuard(54);
        assertFalse(guard.isGuarded(30, 0));
        guard.record(30, "upgrade:lamp_1", 1_000);
        assertTrue(guard.isGuarded(30, 1_000));
        assertTrue(guard.isGuarded(30, 1_299));
        assertFalse(guard.isGuarded(30, 1_300));
        // 同じものなら（説明文の数字が変わっただけ）時刻は変わらない
        guard.record(30, "upgrade:lamp_1", 2_000);
        assertFalse(guard.isGuarded(30, 2_000));
        // 別のものに変わったら、また止める
        guard.record(30, "upgrade:lamp_2", 3_000);
        assertTrue(guard.isGuarded(30, 3_100));
        assertFalse(guard.isGuarded(31, 3_100));
        assertFalse(guard.isGuarded(-999, 3_100));
    }
}
