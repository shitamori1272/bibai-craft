package io.github.shitamori1272.coalclicker.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FadeCurveTest {

    @Test
    void 段ごとに小さくなり最後は0() {
        float previous = 0.8f;
        for (int step = 1; step <= 5; step++) {
            float v = FadeCurve.volume(0.8f, step, 5);
            assertTrue(v < previous, "段 " + step);
            previous = v;
        }
        assertEquals(0f, FadeCurve.volume(0.8f, 5, 5));
    }

    @Test
    void 二乗でなだらかに下げる() {
        assertEquals(0.8f * 0.64f, FadeCurve.volume(0.8f, 1, 5), 1e-6);
        assertEquals(0.8f * 0.04f, FadeCurve.volume(0.8f, 4, 5), 1e-6);
    }

    @Test
    void 範囲の外は断る() {
        assertThrows(IllegalArgumentException.class, () -> FadeCurve.volume(1f, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> FadeCurve.volume(1f, 6, 5));
        assertThrows(IllegalArgumentException.class, () -> FadeCurve.volume(1f, 1, 0));
    }
}
