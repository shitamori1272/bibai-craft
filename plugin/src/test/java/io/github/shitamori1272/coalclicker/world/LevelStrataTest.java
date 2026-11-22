package io.github.shitamori1272.coalclicker.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.mine.SectionBounds;
import io.github.shitamori1272.coalclicker.world.LevelStrata.Rock;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LevelStrataTest {

    private static final int MIN_X = SectionBounds.REFILL_MIN_X;
    private static final int MAX_X = SectionBounds.REFILL_MAX_X;
    private static final int MIN_Y = SectionBounds.REFILL_MIN_Y;
    private static final int MAX_Y = SectionBounds.REFILL_MAX_Y;

    /** 階層 level の区画（Z = 0〜3 区画ぶん）に出てくる岩を、地層ごとに集める。 */
    private static Set<Rock> rocks(long level, MineLayout.Stratum stratum) {
        Set<Rock> found = EnumSet.noneOf(Rock.class);
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                if (MineLayout.stratumAt(y) != stratum) {
                    continue;
                }
                for (int z = -8; z < 56; z++) {
                    found.add(LevelStrata.at(level, x, y, z));
                }
            }
        }
        return found;
    }

    @Test
    void 第1から3坑道は石と砂岩() {
        for (long level = 0; level <= 2; level++) {
            assertEquals(EnumSet.of(Rock.STONE), rocks(level, MineLayout.Stratum.STONE));
            assertEquals(EnumSet.of(Rock.COAL_ORE), rocks(level, MineLayout.Stratum.COAL_SEAM));
            assertEquals(EnumSet.of(Rock.SANDSTONE), rocks(level, MineLayout.Stratum.SANDSTONE));
        }
    }

    @Test
    void 第4から6坑道は凝灰岩が混ざる() {
        for (long level = 3; level <= 5; level++) {
            assertEquals(EnumSet.of(Rock.STONE, Rock.TUFF), rocks(level, MineLayout.Stratum.STONE));
            assertEquals(EnumSet.of(Rock.COAL_ORE), rocks(level, MineLayout.Stratum.COAL_SEAM));
            assertEquals(EnumSet.of(Rock.SANDSTONE, Rock.TUFF), rocks(level, MineLayout.Stratum.SANDSTONE));
        }
    }

    @Test
    void 第7坑道からは深層岩() {
        for (long level : new long[] {6, 7, 50, Long.MAX_VALUE}) {
            assertEquals(EnumSet.of(Rock.DEEPSLATE), rocks(level, MineLayout.Stratum.STONE));
            assertEquals(EnumSet.of(Rock.DEEPSLATE_COAL_ORE), rocks(level, MineLayout.Stratum.COAL_SEAM));
            assertEquals(EnumSet.of(Rock.TUFF), rocks(level, MineLayout.Stratum.SANDSTONE));
        }
    }

    @Test
    void 階層の境目() {
        int y = MineLayout.FLOOR_Y;
        assertEquals(Rock.STONE, LevelStrata.at(2, 5, y, 0));
        assertEquals(Rock.DEEPSLATE, LevelStrata.at(6, 5, y, 0));
        assertEquals(Rock.SANDSTONE, LevelStrata.at(2, 5, MineLayout.CEILING_MAX_Y, 0));
        assertEquals(Rock.TUFF, LevelStrata.at(6, 5, MineLayout.CEILING_MAX_Y, 0));
        // 第 4 坑道で初めて凝灰岩が出る・第 6 坑道までは深層岩が出ない
        assertTrue(rocks(3, MineLayout.Stratum.STONE).contains(Rock.TUFF));
        assertTrue(!rocks(5, MineLayout.Stratum.STONE).contains(Rock.DEEPSLATE));
        assertThrows(IllegalArgumentException.class, () -> LevelStrata.at(-1, 0, y, 0));
    }

    @Test
    void Xについて64の周期() {
        for (long level = 0; level <= 8; level++) {
            for (int x = MIN_X - 64; x <= MAX_X + 64; x++) {
                for (int y = MIN_Y; y <= MAX_Y; y++) {
                    for (int z = -12; z < 40; z += 3) {
                        Rock rock = LevelStrata.at(level, x, y, z);
                        assertEquals(rock, LevelStrata.at(level, x + MineLayout.LOOP_METERS, y, z));
                        assertEquals(rock, LevelStrata.at(level, x + 5 * MineLayout.LOOP_METERS, y, z));
                    }
                }
            }
        }
    }

    @Test
    void 炭層はいつも石炭鉱石() {
        for (long level = 0; level <= 10; level++) {
            for (int x = MIN_X; x <= MAX_X; x++) {
                for (int y = MineLayout.SEAM_MIN_Y; y <= MineLayout.SEAM_MAX_Y; y++) {
                    for (int z = -8; z < 56; z++) {
                        assertTrue(LevelStrata.at(level, x, y, z).isCoalOre());
                    }
                }
            }
        }
    }

    @Test
    void 同じ座標ならいつも同じ岩() {
        for (int i = 0; i < 1000; i++) {
            int x = i * 7 - 300;
            int z = i * 13 - 500;
            assertEquals(LevelStrata.at(4, x, 39, z), LevelStrata.at(4, x, 39, z));
        }
    }
}
