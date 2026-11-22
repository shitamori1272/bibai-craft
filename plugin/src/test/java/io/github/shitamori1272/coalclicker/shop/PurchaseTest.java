package io.github.shitamori1272.coalclicker.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PurchaseTest {

    private static PlayerData withCoal(double coal) {
        PlayerData data = new PlayerData(UUID.randomUUID());
        data.setCoal(BigNum.of(coal));
        return data;
    }

    private static void assertClose(double expected, BigNum actual) {
        assertEquals(expected, actual.toDouble(), Math.abs(expected) * 1e-9 + 1e-9);
    }

    @Test
    void 足りれば買えて石炭が減る() {
        PlayerData data = withCoal(250);
        Purchase.Result result = Purchase.tryBuy(data, Tool.BLASTING, 1);
        assertTrue(result.success());
        assertClose(200, result.cost());
        assertClose(50, data.coal());
        assertEquals(1, data.getToolCount("blasting"));
        assertClose(1, Formulas.digPower(data.tools()));
    }

    @Test
    void ちょうどの額でも買える() {
        PlayerData data = withCoal(150);
        assertTrue(Purchase.tryBuy(data, Tool.LAMP, 1).success());
        assertTrue(data.coal().isZero() || data.coal().toDouble() < 1e-9);
    }

    @Test
    void 足りなければ何も変わらない() {
        PlayerData data = withCoal(199);
        Purchase.Result result = Purchase.tryBuy(data, Tool.BLASTING, 1);
        assertFalse(result.success());
        assertFalse(result.firstPurchase());
        assertClose(1, result.shortage());
        assertClose(199, data.coal());
        assertEquals(0, data.getToolCount("blasting"));
        assertFalse(data.isExplained("blasting"));
    }

    @Test
    void 買うたびに価格が上がる() {
        PlayerData data = withCoal(10_000);
        assertClose(500, Purchase.tryBuy(data, Tool.PICKAXE, 1).cost());
        assertClose(1_000, Purchase.tryBuy(data, Tool.PICKAXE, 1).cost());
        assertClose(150, Purchase.tryBuy(data, Tool.LAMP, 1).cost());
        assertClose(150 * 1.15, Purchase.tryBuy(data, Tool.LAMP, 1).cost());
        assertClose(10_000 - 500 - 1_000 - 150 - 150 * 1.15, data.coal());
    }

    @Test
    void 解説ははじめて買ったときだけ() {
        PlayerData data = withCoal(10_000);
        assertTrue(Purchase.tryBuy(data, Tool.LAMP, 1).firstPurchase());
        assertTrue(data.isExplained("lamp"));
        assertFalse(Purchase.tryBuy(data, Tool.LAMP, 1).firstPurchase());
        assertTrue(Purchase.tryBuy(data, Tool.BLASTING, 1).firstPurchase());
    }

    @Test
    void まとめて買える() {
        PlayerData data = withCoal(1_000_000);
        Purchase.Result result = Purchase.tryBuy(data, Tool.MINECART, 5);
        assertTrue(result.success());
        assertTrue(result.firstPurchase());
        double expected = 0;
        for (int i = 0; i < 5; i++) {
            expected += 1_000 * Math.pow(1.15, i);
        }
        assertClose(expected, result.cost());
        assertClose(1_000_000 - expected, data.coal());
        assertEquals(5, data.getToolCount("minecart"));
        assertClose(100 + 500, Formulas.coalPower(data.tools()));
    }

    @Test
    void 買える個数を数える() {
        // 発破：200 + 230 + 264.5 = 694.5
        assertEquals(2, Purchase.affordableCount(withCoal(694), Tool.BLASTING, 10));
        assertEquals(3, Purchase.affordableCount(withCoal(694.6), Tool.BLASTING, 10));
        assertEquals(0, Purchase.affordableCount(withCoal(199), Tool.BLASTING, 10));
        assertEquals(10, Purchase.affordableCount(withCoal(1e30), Tool.BLASTING, 10));
    }

    @Test
    void 個数は1以上() {
        assertThrows(IllegalArgumentException.class, () -> Purchase.tryBuy(withCoal(0), Tool.LAMP, 0));
    }
}
