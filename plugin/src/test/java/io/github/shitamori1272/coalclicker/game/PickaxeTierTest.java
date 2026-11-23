package io.github.shitamori1272.coalclicker.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PickaxeTierTest {

    @Test
    void 改良がなければ木のツルハシ() {
        assertEquals(0, PickaxeTier.of(Set.of()));
        assertEquals("WOODEN_PICKAXE", PickaxeTier.material(0));
        assertEquals("木のツルハシ", PickaxeTier.displayName(0));
    }

    @Test
    void ツルハシの改良の数だけ素材が上がる() {
        assertEquals(1, PickaxeTier.of(Set.of("pickaxe_1")));
        assertEquals(2, PickaxeTier.of(Set.of("pickaxe_1", "pickaxe_2")));
        assertEquals(3, PickaxeTier.of(Set.of("pickaxe_1", "pickaxe_2", "pickaxe_3")));
        assertEquals("STONE_PICKAXE", PickaxeTier.material(1));
        assertEquals("IRON_PICKAXE", PickaxeTier.material(2));
        assertEquals("DIAMOND_PICKAXE", PickaxeTier.material(3));
    }

    @Test
    void ほかの改良では素材は変わらない() {
        assertEquals(0, PickaxeTier.of(Set.of("lamp_1", "skill_1", "skill_2")));
        assertEquals(1, PickaxeTier.of(Set.of("pickaxe_2", "lamp_1")));
    }

    @Test
    void 段階は改良の数と同じだけある() {
        assertEquals(Upgrade.values().stream().filter(u -> u.kind() == Upgrade.Kind.PICKAXE).count(),
                PickaxeTier.MAX);
    }

    @Test
    void 範囲の外の段階は端に寄せる() {
        assertEquals("WOODEN_PICKAXE", PickaxeTier.material(-1));
        assertEquals("DIAMOND_PICKAXE", PickaxeTier.material(99));
    }
}
