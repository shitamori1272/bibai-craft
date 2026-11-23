package io.github.shitamori1272.coalclicker.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TrophyTest {

    @Test
    void 一万に届くと最初のトロフィー() {
        assertEquals(List.of(), Trophy.newlyReached(BigNum.of(9_999.9), Set.of()));
        assertEquals(List.of(4), Trophy.newlyReached(BigNum.of(10_000), Set.of()));
        assertEquals(List.of(), Trophy.newlyReached(BigNum.ZERO, Set.of()));
    }

    @Test
    void 桁が増えるたびに1つずつ増え_もらった分は出さない() {
        assertEquals(List.of(4, 5, 6), Trophy.newlyReached(BigNum.of(3_000_000), Set.of()));
        assertEquals(List.of(6), Trophy.newlyReached(BigNum.of(3_000_000), Set.of("trophy_4", "trophy_5")));
    }

    @Test
    void 最後の節目より先は増えない() {
        List<Integer> all = Trophy.newlyReached(BigNum.of(1, 5_000), Set.of());
        assertEquals(Trophy.MAX_DIGITS - Trophy.MIN_DIGITS + 1, all.size());
        assertEquals(Trophy.MAX_DIGITS, all.getLast());
    }

    @Test
    void IDの判定() {
        assertEquals("trophy_4", Trophy.id(4));
        assertTrue(Trophy.isId("trophy_4"));
        assertTrue(Trophy.isId("trophy_71"));
        assertFalse(Trophy.isId("trophy_3"));
        assertFalse(Trophy.isId("trophy_72"));
        assertFalse(Trophy.isId("trophy_04"));
        assertFalse(Trophy.isId("trophy_"));
        assertFalse(Trophy.isId("trophy_x"));
        assertFalse(Trophy.isId("lamp_1"));
        assertThrows(IllegalArgumentException.class, () -> Trophy.id(3));
    }

    @Test
    void 一つにつき生産が5パーセント増える() {
        Set<String> three = Set.of("trophy_4", "trophy_5", "trophy_6", "lamp_1");
        assertEquals(3, Trophy.count(three));
        assertEquals(1.15, Trophy.multiplier(three), 1e-12);
        assertEquals(1.0, Trophy.multiplier(Set.of()), 1e-12);
    }

    @Test
    void 毎秒の石炭と手掘りの両方にかかる() {
        Set<String> two = Set.of("trophy_4", "trophy_5");
        Map<String, Long> tools = Map.of("lamp", 10L);
        double plain = Formulas.coalPerSecond(tools, Set.of(), 0).toDouble();
        assertEquals(plain * 1.1, Formulas.coalPerSecond(tools, two, 0).toDouble(), 1e-9);
        assertEquals(11.0, Formulas.clickBase(two).toDouble(), 1e-9);
    }

    @Test
    void 節目の名前() {
        assertEquals("1万個", Trophy.milestoneName(4));
        assertEquals("10万個", Trophy.milestoneName(5));
        assertEquals("1億個", Trophy.milestoneName(8));
    }
}
