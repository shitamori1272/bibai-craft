package io.github.shitamori1272.coalclicker.mine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.mine.TunnelPlan.Kind;
import io.github.shitamori1272.coalclicker.world.MineLayout;
import org.junit.jupiter.api.Test;

class TunnelPlanTest {

    private static TunnelPlan carve(int from, int to) {
        return new TunnelPlan(Kind.CARVE, from, to, false);
    }

    private static final TunnelPlan NONE = new TunnelPlan(Kind.NONE, 0, 0, false);
    private static final TunnelPlan LOOP = new TunnelPlan(Kind.REBUILD, 0, 0, true);
    private static final TunnelPlan REBUILD = new TunnelPlan(Kind.REBUILD, 0, 0, false);

    @Test
    void 一m進むと切羽の列を一列掘る() {
        assertEquals(carve(1, 1), TunnelPlan.plan(0, 1));
        assertEquals(carve(2, 2), TunnelPlan.plan(1, 2));
        assertEquals(carve(1, 5), TunnelPlan.plan(0, 5));
    }

    @Test
    void 一m未満の増加では何もしない() {
        assertEquals(NONE, TunnelPlan.plan(0, 0.9));
        assertEquals(NONE, TunnelPlan.plan(10.1, 10.8));
        assertEquals(NONE, TunnelPlan.plan(5, 5));
        assertEquals(carve(11, 11), TunnelPlan.plan(10.8, 11.2));
    }

    @Test
    void 六十四mの手前と境目() {
        // 62m → 63m：切羽が X = 63 から X = 64 へ（巻き戻しはまだ）
        assertEquals(carve(63, 63), TunnelPlan.plan(62, 63));
        // 63m → 64m：切羽は X = 64 → 65
        assertEquals(carve(64, 64), TunnelPlan.plan(63, 64));
        assertEquals(carve(1, 64), TunnelPlan.plan(0, 64));
    }

    @Test
    void 切羽がX128に達したら作り直してプレイヤーを移す() {
        assertEquals(127, MineLayout.faceX(126));
        assertEquals(carve(126, 126), TunnelPlan.plan(125, 126));
        // 126m → 127m：切羽が X = 128 に達するので X = 64 に巻き戻す
        assertEquals(LOOP, TunnelPlan.plan(126, 127));
        assertEquals(64, MineLayout.faceX(127));
        // 127m → 128m：巻き戻した後はまた一列ずつ
        assertEquals(carve(64, 64), TunnelPlan.plan(127, 128));
        // 2 回目の巻き戻し
        assertEquals(carve(126, 126), TunnelPlan.plan(189, 190));
        assertEquals(LOOP, TunnelPlan.plan(190, 191));
    }

    @Test
    void 一度に大きく進んだら作り直す() {
        assertEquals(LOOP, TunnelPlan.plan(0, 200));
        assertEquals(LOOP, TunnelPlan.plan(100, 1_000));
        assertEquals(LOOP, TunnelPlan.plan(64, 64 + 64));
        // 同じ周回の中なら 64 列未満の差分なので掘るだけ
        assertEquals(carve(1, 63), TunnelPlan.plan(0, 63));
        assertEquals(carve(65, 126), TunnelPlan.plan(64, 126));
    }

    @Test
    void 進み具合が減ったら作り直すがプレイヤーは移さない() {
        assertEquals(REBUILD, TunnelPlan.plan(10, 0));
        assertEquals(REBUILD, TunnelPlan.plan(130, 100));
        assertEquals(REBUILD, TunnelPlan.plan(200, 0));
    }

    @Test
    void 何km進んでも掘る列は窓の中() {
        for (int m = 0; m < 2_000; m++) {
            TunnelPlan plan = TunnelPlan.plan(m, m + 1);
            if (plan.kind() == Kind.CARVE) {
                assertTrue(plan.fromX() >= MineLayout.TUNNEL_START_X);
                assertTrue(plan.toX() < MineLayout.WINDOW_MAX_X, "m=" + m);
                assertEquals(plan.fromX(), plan.toX());
            } else {
                assertEquals(LOOP, plan, "m=" + m);
            }
        }
    }

    @Test
    void 坑木と明かりはループ周期で同じ位置() {
        assertFalse(TunnelPlan.isTimberX(0));
        assertTrue(TunnelPlan.isTimberX(4));
        assertFalse(TunnelPlan.isTimberX(5));
        assertTrue(TunnelPlan.isTimberX(64));
        for (int x = 1; x < MineLayout.LOOP_METERS; x++) {
            int shifted = x + MineLayout.LOOP_METERS;
            assertEquals(TunnelPlan.isTimberX(x), TunnelPlan.isTimberX(shifted), "x=" + x);
            assertEquals(TunnelPlan.isLightX(x), TunnelPlan.isLightX(shifted), "x=" + x);
            assertFalse(TunnelPlan.isTimberX(x) && TunnelPlan.isLightX(x), "x=" + x);
        }
    }

    @Test
    void 割合からmへの換算は整数の近くを丸める() {
        assertEquals(16.0, TunnelPlan.meters(0.25, 64));
        assertEquals(37.0, TunnelPlan.meters(37.0 / 80.0, 80));
        for (int m = 0; m <= 1_648; m++) {
            assertEquals(m, TunnelPlan.meters(m / 1_648.0, 1_648), "m=" + m);
        }
        assertEquals(0.0, TunnelPlan.meters(-0.5, 64));
        assertEquals(64.0, TunnelPlan.meters(1.5, 64));
        assertEquals(10.5, TunnelPlan.meters(10.5 / 64, 64), 1e-9);
    }
}
