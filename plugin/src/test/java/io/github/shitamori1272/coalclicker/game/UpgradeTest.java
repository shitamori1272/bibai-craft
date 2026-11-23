package io.github.shitamori1272.coalclicker.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UpgradeTest {

    private static GameState state(Map<String, Long> tools, Set<String> upgrades, long clicks, double totalMined) {
        return GameState.of(tools, upgrades, clicks, BigNum.of(totalMined));
    }

    private static void assertClose(double expected, BigNum actual) {
        assertEquals(1.0, actual.divide(BigNum.of(expected)).toDouble(), 1e-12, () -> expected + " / " + actual);
    }

    @Test
    void 改良は全部で64個でIDは重ならない() {
        assertEquals(8 * 7 + 3 + 5, Upgrade.values().size());
        Set<String> ids = new HashSet<>();
        for (Upgrade upgrade : Upgrade.values()) {
            assertTrue(ids.add(upgrade.id()), upgrade.id());
            assertEquals(upgrade, Upgrade.byId(upgrade.id()).orElseThrow());
        }
        assertTrue(Upgrade.byId("lamp_8").isEmpty());
        assertTrue(Upgrade.byId("pickaxe").isEmpty());
    }

    @Test
    void 道具の改良の出現条件と価格() {
        long[] owned = {1, 5, 25, 50, 100, 150, 200};
        double[] factor = {10, 50, 500, 5e4, 5e6, 5e8, 5e10};
        for (Tool tool : Tool.values()) {
            for (int tier = 1; tier <= 7; tier++) {
                Upgrade upgrade = Upgrade.toolTier(tool, tier);
                assertEquals(tool.id() + "_" + tier, upgrade.id());
                assertEquals(Upgrade.Kind.TOOL, upgrade.kind());
                assertEquals(tool, upgrade.tool().orElseThrow());
                assertEquals(tier, upgrade.tier());
                assertEquals(tool.basePrice().multiply(factor[tier - 1]), upgrade.price());
                long need = owned[tier - 1];
                assertFalse(upgrade.isUnlocked(state(Map.of(tool.id(), need - 1), Set.of(), 0, 0)));
                assertTrue(upgrade.isUnlocked(state(Map.of(tool.id(), need), Set.of(), 0, 0)));
            }
        }
        assertClose(1_500, Upgrade.toolTier(Tool.LAMP, 1).price());
        assertClose(150 * 5e10, Upgrade.toolTier(Tool.LAMP, 7).price());
        assertEquals("安全灯の改良 I", Upgrade.toolTier(Tool.LAMP, 1).displayName());
        assertEquals("安全灯の改良 VII", Upgrade.toolTier(Tool.LAMP, 7).displayName());
        assertThrows(IllegalArgumentException.class, () -> Upgrade.toolTier(Tool.LAMP, 8));
    }

    @Test
    void ツルハシの改良は手掘りの回数で出る() {
        Upgrade p1 = Upgrade.byId("pickaxe_1").orElseThrow();
        Upgrade p3 = Upgrade.byId("pickaxe_3").orElseThrow();
        assertEquals(Upgrade.Kind.PICKAXE, p1.kind());
        assertClose(1_000, p1.price());
        assertClose(1e7, p3.price());
        assertFalse(p1.isUnlocked(state(Map.of(), Set.of(), 99, 1e100)));
        assertTrue(p1.isUnlocked(state(Map.of(), Set.of(), 100, 0)));
        assertFalse(p3.isUnlocked(state(Map.of(), Set.of(), 9_999, 0)));
        assertTrue(p3.isUnlocked(state(Map.of(), Set.of(), 10_000, 0)));
    }

    @Test
    void 熟練の改良は累計採掘量で出て価格も同じ() {
        double[] thresholds = {5e4, 5e6, 5e8, 5e10, 5e12};
        for (int tier = 1; tier <= 5; tier++) {
            Upgrade upgrade = Upgrade.byId("skill_" + tier).orElseThrow();
            assertEquals(Upgrade.Kind.SKILL, upgrade.kind());
            double t = thresholds[tier - 1];
            assertClose(t, upgrade.price());
            assertFalse(upgrade.isUnlocked(state(Map.of(), Set.of(), 1_000_000, Math.nextDown(t))));
            assertTrue(upgrade.isUnlocked(state(Map.of(), Set.of(), 0, t)));
        }
    }

    @Test
    void 買えるのは出現していてまだ買っていない改良() {
        GameState s = state(Map.of("lamp", 5L, "minecart", 1L), Set.of("lamp_1"), 150, 6e4);
        List<String> ids = Upgrade.available(s).stream().map(Upgrade::id).toList();
        assertEquals(List.of("lamp_2", "minecart_1", "pickaxe_1", "skill_1"), ids);
        Upgrade lamp1 = Upgrade.toolTier(Tool.LAMP, 1);
        assertTrue(lamp1.isUnlocked(s));
        assertTrue(lamp1.isOwned(s));
        assertFalse(lamp1.isAvailable(s));
        assertTrue(s.hasUpgrade(lamp1));
        assertTrue(Upgrade.available(state(Map.of(), Set.of(), 0, 0)).isEmpty());
    }
}
