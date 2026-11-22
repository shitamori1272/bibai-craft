package io.github.shitamori1272.coalclicker.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
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
        PlayerData data = withCoal(1_200);
        Purchase.Result result = Purchase.tryBuy(data, Tool.MINECART, 1);
        assertTrue(result.success());
        assertClose(1_000, result.cost());
        assertClose(200, data.coal());
        assertEquals(1, data.getToolCount("minecart"));
        assertClose(10, Formulas.coalPerSecond(data));
    }

    @Test
    void ちょうどの額でも買える() {
        PlayerData data = withCoal(150);
        assertTrue(Purchase.tryBuy(data, Tool.LAMP, 1).success());
        assertTrue(data.coal().isZero() || data.coal().toDouble() < 1e-9);
    }

    @Test
    void 足りなければ何も変わらない() {
        PlayerData data = withCoal(999);
        Purchase.Result result = Purchase.tryBuy(data, Tool.MINECART, 1);
        assertFalse(result.success());
        assertFalse(result.firstPurchase());
        assertClose(1, result.shortage());
        assertClose(999, data.coal());
        assertEquals(0, data.getToolCount("minecart"));
        assertFalse(data.isExplained("minecart"));
    }

    @Test
    void 買うたびに価格が上がる() {
        PlayerData data = withCoal(10_000);
        assertClose(1_000, Purchase.tryBuy(data, Tool.MINECART, 1).cost());
        assertClose(1_150, Purchase.tryBuy(data, Tool.MINECART, 1).cost());
        assertClose(150, Purchase.tryBuy(data, Tool.LAMP, 1).cost());
        assertClose(150 * 1.15, Purchase.tryBuy(data, Tool.LAMP, 1).cost());
        assertClose(10_000 - 1_000 - 1_150 - 150 - 150 * 1.15, data.coal());
    }

    @Test
    void 解説ははじめて買ったときだけ() {
        PlayerData data = withCoal(10_000);
        assertTrue(Purchase.tryBuy(data, Tool.LAMP, 1).firstPurchase());
        assertTrue(data.isExplained("lamp"));
        assertFalse(Purchase.tryBuy(data, Tool.LAMP, 1).firstPurchase());
        assertTrue(Purchase.tryBuy(data, Tool.MINECART, 1).firstPurchase());
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
        assertClose(50, Formulas.coalPerSecond(data));
    }

    @Test
    void 買える個数を数える() {
        // 炭車：1,000 + 1,150 + 1,322.5 = 3,472.5
        assertEquals(2, Purchase.affordableCount(withCoal(3_472), Tool.MINECART, 10));
        assertEquals(3, Purchase.affordableCount(withCoal(3_472.6), Tool.MINECART, 10));
        assertEquals(0, Purchase.affordableCount(withCoal(999), Tool.MINECART, 10));
        assertEquals(10, Purchase.affordableCount(withCoal(1e30), Tool.MINECART, 10));
    }

    @Test
    void 改良は出現していて足りれば買える() {
        PlayerData data = withCoal(2_000);
        Upgrade lamp1 = Upgrade.toolTier(Tool.LAMP, 1);
        // 安全灯を持っていないので出現していない
        Purchase.UpgradeResult hidden = Purchase.tryBuy(data, lamp1);
        assertFalse(hidden.success());
        assertTrue(hidden.shortage().isZero());
        assertClose(2_000, data.coal());

        data.setToolCount("lamp", 1);
        Purchase.UpgradeResult bought = Purchase.tryBuy(data, lamp1);
        assertTrue(bought.success());
        assertTrue(bought.firstPurchase());
        assertClose(1_500, bought.cost());
        assertClose(500, data.coal());
        assertTrue(data.hasUpgrade("lamp_1"));
        assertTrue(data.isExplained("lamp_1"));
        assertClose(2, Formulas.coalPerSecond(data));

        // 2 回目は買えない
        data.setCoal(BigNum.of(1e6));
        assertFalse(Purchase.tryBuy(data, lamp1).success());
        assertClose(1e6, data.coal());
    }

    @Test
    void 改良の石炭が足りなければ不足分を返す() {
        PlayerData data = withCoal(1_000);
        data.setToolCount("lamp", 1);
        Purchase.UpgradeResult result = Purchase.tryBuy(data, Upgrade.toolTier(Tool.LAMP, 1));
        assertFalse(result.success());
        assertClose(500, result.shortage());
        assertFalse(data.hasUpgrade("lamp_1"));
    }

    @Test
    void 個数は1以上() {
        assertThrows(IllegalArgumentException.class, () -> Purchase.tryBuy(withCoal(0), Tool.LAMP, 0));
    }
}
