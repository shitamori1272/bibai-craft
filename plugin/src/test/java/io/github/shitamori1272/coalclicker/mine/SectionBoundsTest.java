package io.github.shitamori1272.coalclicker.mine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.mine.SectionBounds.ChunkPos;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import java.util.List;
import org.junit.jupiter.api.Test;

class SectionBoundsTest {

    @Test
    void 当たり判定が新しい切羽にかかる人を移す() {
        assertFalse(SectionBounds.needsLoopShift(0.5));
        assertFalse(SectionBounds.needsLoopShift(63.5));
        assertFalse(SectionBounds.needsLoopShift(63.69));
        assertTrue(SectionBounds.needsLoopShift(63.7));
        assertTrue(SectionBounds.needsLoopShift(63.999));
        assertTrue(SectionBounds.needsLoopShift(64.0));
        assertTrue(SectionBounds.needsLoopShift(126.7));
    }

    @Test
    void 巻き戻しの後に誰も岩に埋まらない() {
        // 巻き戻しの直前（126m）にいられる範囲を細かく調べ、巻き戻した後（127m、切羽 X = 64）でも掘ってある範囲に収まる
        int section = 3;
        double z = MineLayout.sectionZ(section) + 0.5;
        assertEquals(64, MineLayout.faceX(127));
        for (double x = MineLayout.SHAFT_MIN_X + 0.3; x <= 126.7; x += 0.01) {
            assertTrue(SectionBounds.inOpenArea(section, 126, x, z), "x=" + x);
            double after = SectionBounds.needsLoopShift(x) ? x - MineLayout.LOOP_METERS : x;
            assertTrue(SectionBounds.inOpenArea(section, 127, after, z), "x=" + x + " → " + after);
        }
    }

    @Test
    void 掘ってある範囲() {
        int section = 2;
        double z = MineLayout.sectionZ(section) + 0.5;
        // 10m：X = 0〜10 が空気、切羽は X = 11
        assertTrue(SectionBounds.inOpenArea(section, 10, 10.7, z));
        assertFalse(SectionBounds.inOpenArea(section, 10, 10.71, z));
        assertTrue(SectionBounds.inOpenArea(section, 10, -7.7, z));
        assertFalse(SectionBounds.inOpenArea(section, 10, -7.71, z));
        // 幅 3（Z = 中心 - 1 〜 中心 + 1）
        assertTrue(SectionBounds.inOpenArea(section, 10, 5, MineLayout.tunnelMinZ(section) + 0.3));
        assertFalse(SectionBounds.inOpenArea(section, 10, 5, MineLayout.tunnelMinZ(section) + 0.29));
        assertTrue(SectionBounds.inOpenArea(section, 10, 5, MineLayout.tunnelMaxZ(section) + 0.7));
        assertFalse(SectionBounds.inOpenArea(section, 10, 5, MineLayout.tunnelMaxZ(section) + 0.71));
        // 隣の区画
        assertFalse(SectionBounds.inOpenArea(section + 1, 10, 5, z));
    }

    @Test
    void 区画の坑道の範囲() {
        int section = 1;
        double z = MineLayout.sectionZ(section) + 0.5;
        assertTrue(SectionBounds.inSectionTunnel(section, -8, z));
        assertTrue(SectionBounds.inSectionTunnel(section, 128.9, z));
        assertFalse(SectionBounds.inSectionTunnel(section, -8.1, z));
        assertFalse(SectionBounds.inSectionTunnel(section, 129, z));
        assertFalse(SectionBounds.inSectionTunnel(section, 5, z + 2));
        assertFalse(SectionBounds.inSectionTunnel(0, 5, z));
    }

    @Test
    void 作り直しで触るチャンク() {
        // 区画 0：Z = -3〜3 → チャンク Z = -1, 0。X = -9〜129 → チャンク X = -1〜8
        List<ChunkPos> chunks = SectionBounds.chunks(0);
        assertEquals(10 * 2, chunks.size());
        assertTrue(chunks.contains(new ChunkPos(-1, -1)));
        assertTrue(chunks.contains(new ChunkPos(8, 0)));
        // 区画 5：Z = 77〜83 → チャンク Z = 4, 5
        List<ChunkPos> five = SectionBounds.chunks(5);
        assertTrue(five.contains(new ChunkPos(0, 4)));
        assertTrue(five.contains(new ChunkPos(0, 5)));
        assertFalse(five.contains(new ChunkPos(0, 6)));
        // 埋め直す範囲の四隅がすべて含まれる
        for (int section : new int[] {0, 1, 7, 1000}) {
            int c = MineLayout.sectionZ(section);
            List<ChunkPos> list = SectionBounds.chunks(section);
            for (int x : new int[] {SectionBounds.REFILL_MIN_X, SectionBounds.REFILL_MAX_X}) {
                for (int dz : new int[] {-SectionBounds.REFILL_HALF_WIDTH, SectionBounds.REFILL_HALF_WIDTH}) {
                    assertTrue(list.contains(new ChunkPos(x >> 4, (c + dz) >> 4)), "section=" + section);
                }
            }
        }
    }
}
