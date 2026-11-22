package io.github.shitamori1272.coalclicker.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.world.MineLayout.Stratum;
import org.junit.jupiter.api.Test;

class MineLayoutTest {

    @Test
    void 地層() {
        assertEquals(Stratum.STONE, MineLayout.stratumAt(39));
        assertEquals(Stratum.STONE, MineLayout.stratumAt(40));
        assertEquals(Stratum.COAL_SEAM, MineLayout.stratumAt(41));
        assertEquals(Stratum.COAL_SEAM, MineLayout.stratumAt(42));
        assertEquals(Stratum.SANDSTONE, MineLayout.stratumAt(43));
        assertEquals(Stratum.SANDSTONE, MineLayout.stratumAt(44));
        assertEquals(Stratum.STONE, MineLayout.stratumAt(45));
        assertEquals(Stratum.STONE, MineLayout.stratumAt(-64));
        assertEquals(Stratum.STONE, MineLayout.stratumAt(319));
    }

    @Test
    void 坑道の空気は床の上で天井の砂岩にかかる() {
        assertEquals(MineLayout.FLOOR_Y + 1, MineLayout.TUNNEL_MIN_Y);
        assertEquals(3, MineLayout.TUNNEL_MAX_Y - MineLayout.TUNNEL_MIN_Y + 1);
        assertEquals(Stratum.SANDSTONE, MineLayout.stratumAt(MineLayout.TUNNEL_MAX_Y + 1));
    }

    @Test
    void 区画のZ() {
        assertEquals(0, MineLayout.sectionZ(0));
        assertEquals(16, MineLayout.sectionZ(1));
        assertEquals(160, MineLayout.sectionZ(10));
        assertEquals(159, MineLayout.tunnelMinZ(10));
        assertEquals(161, MineLayout.tunnelMaxZ(10));
        assertThrows(IllegalArgumentException.class, () -> MineLayout.sectionZ(-1));
        assertThrows(IllegalArgumentException.class, () -> MineLayout.sectionZ(MineLayout.MAX_SECTION + 1));
        assertTrue(MineLayout.tunnelMaxZ(MineLayout.MAX_SECTION) <= 29_999_984);
    }

    @Test
    void Zから区画を求める() {
        assertEquals(0, MineLayout.sectionAt(0));
        assertEquals(0, MineLayout.sectionAt(-8));
        assertEquals(0, MineLayout.sectionAt(7));
        assertEquals(1, MineLayout.sectionAt(8));
        assertEquals(1, MineLayout.sectionAt(17));
        assertEquals(-1, MineLayout.sectionAt(-9));
        for (int section : new int[] {0, 1, 5, 1000}) {
            assertEquals(section, MineLayout.sectionAt(MineLayout.tunnelMinZ(section)));
            assertEquals(section, MineLayout.sectionAt(MineLayout.tunnelMaxZ(section)));
        }
    }

    @Test
    void 坑道の断面() {
        assertTrue(MineLayout.inTunnelCrossSection(1, 41, 15));
        assertTrue(MineLayout.inTunnelCrossSection(1, 43, 17));
        assertFalse(MineLayout.inTunnelCrossSection(1, 40, 16));
        assertFalse(MineLayout.inTunnelCrossSection(1, 44, 16));
        assertFalse(MineLayout.inTunnelCrossSection(1, 42, 14));
        assertFalse(MineLayout.inTunnelCrossSection(1, 42, 18));
    }

    @Test
    void 切羽の位置_64m未満() {
        assertEquals(1, MineLayout.faceX(0));
        assertEquals(1, MineLayout.faceX(0.9));
        assertEquals(2, MineLayout.faceX(1));
        assertEquals(64, MineLayout.faceX(63));
        assertEquals(0, MineLayout.tunnelEndX(0));
        assertEquals(63, MineLayout.tunnelEndX(63.5));
    }

    @Test
    void 切羽の位置_64m以上はループする() {
        assertEquals(65, MineLayout.faceX(64));
        assertEquals(127, MineLayout.faceX(126));
        // X = 128 に達するところで X = 64 に巻き戻る
        assertEquals(64, MineLayout.faceX(127));
        assertEquals(65, MineLayout.faceX(128));
        assertEquals(64, MineLayout.faceX(191));
        for (int m = 63; m < 5000; m++) {
            int x = MineLayout.faceX(m);
            assertTrue(x >= MineLayout.LOOP_METERS && x < MineLayout.WINDOW_MAX_X, () -> "X = " + x);
            // 坑木（4m ごと）の位置が巻き戻しでずれない
            assertEquals(Math.floorMod(m + 1, MineLayout.TIMBER_INTERVAL), Math.floorMod(x, MineLayout.TIMBER_INTERVAL));
        }
    }

    @Test
    void 巻き戻しの回数() {
        assertEquals(0, MineLayout.loopCount(0));
        assertEquals(0, MineLayout.loopCount(126.9));
        assertEquals(1, MineLayout.loopCount(127));
        assertEquals(1, MineLayout.loopCount(190));
        assertEquals(2, MineLayout.loopCount(191));
        // 1m ずつ進めたとき、回数が増えるのは切羽が X = 64 に戻ったときだけ
        for (int m = 1; m < 2000; m++) {
            boolean looped = MineLayout.loopCount(m) != MineLayout.loopCount(m - 1);
            assertEquals(MineLayout.faceX(m) < MineLayout.faceX(m - 1), looped, "m = " + m);
        }
    }

    @Test
    void 不正な進み具合() {
        assertThrows(IllegalArgumentException.class, () -> MineLayout.faceX(-1));
        assertThrows(IllegalArgumentException.class, () -> MineLayout.faceX(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> MineLayout.loopCount(Double.POSITIVE_INFINITY));
    }

    @Test
    void 表示範囲と坑底() {
        assertTrue(MineLayout.inWindow(0));
        assertTrue(MineLayout.inWindow(128));
        assertFalse(MineLayout.inWindow(-1));
        assertFalse(MineLayout.inWindow(129));
        assertTrue(MineLayout.inShaftBottom(-8));
        assertTrue(MineLayout.inShaftBottom(0));
        assertFalse(MineLayout.inShaftBottom(-9));
        assertFalse(MineLayout.inShaftBottom(1));
    }

    @Test
    void 坑底の乗り場() {
        assertTrue(MineLayout.inShaftBottomRoom(0, -8, 41, -1));
        assertTrue(MineLayout.inShaftBottomRoom(0, 0, 43, 1));
        assertFalse(MineLayout.inShaftBottomRoom(0, -9, 42, 0));
        assertFalse(MineLayout.inShaftBottomRoom(0, 1, 42, 0));
        assertFalse(MineLayout.inShaftBottomRoom(0, -4, 40, 0));
        assertFalse(MineLayout.inShaftBottomRoom(0, -4, 44, 0));
        assertFalse(MineLayout.inShaftBottomRoom(0, -4, 42, 2));
    }

    @Test
    void 初期地点は坑底の乗り場の床の上() {
        // MineWorld.spawnIn と同じ計算: X = -3.5, Y = 41, Z = 0.5
        int x = (int) Math.floor((MineLayout.SHAFT_MIN_X + MineLayout.SHAFT_MAX_X) / 2.0 + 0.5);
        int y = MineLayout.FLOOR_Y + 1;
        int z = MineLayout.sectionZ(0);
        assertTrue(MineLayout.inShaftBottomRoom(0, x, y, z));
        assertTrue(MineLayout.inShaftBottomRoom(0, x, y + 1, z));
        assertFalse(MineLayout.inShaftBottomRoom(0, x, y - 1, z));
    }
}
