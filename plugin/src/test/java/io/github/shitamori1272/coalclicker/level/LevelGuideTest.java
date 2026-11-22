package io.github.shitamori1272.coalclicker.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.List;
import org.junit.jupiter.api.Test;

class LevelGuideTest {

    @Test
    void 数値上の階層が進んでいるときだけケージ待ち() {
        assertTrue(LevelGuide.awaitingCage(1, 0));
        assertTrue(LevelGuide.awaitingCage(Long.MAX_VALUE, 0));
        assertFalse(LevelGuide.awaitingCage(3, 3));
        assertFalse(LevelGuide.awaitingCage(0, 0));
    }

    @Test
    void ボスバーとタイトル() {
        assertEquals("第3坑道まで掘り進んでいます。ケージで降りよう", LevelGuide.bossBarText(2));
        assertEquals("第3坑道", LevelGuide.descentTitle(2));
        assertEquals("深さ300m", LevelGuide.descentSubtitle(2));
        assertEquals("深さ100m", LevelGuide.descentSubtitle(0));
    }

    @Test
    void 大きな階層でもあふれない() {
        assertEquals("第9,223,372,036,854,775,808坑道", LevelGuide.descentTitle(Long.MAX_VALUE));
        assertEquals("深さ922,337,203,685,477,580,800m", LevelGuide.descentSubtitle(Long.MAX_VALUE));
    }

    @Test
    void 看板は4行() {
        List<String> lines = LevelGuide.endSignLines(4);
        assertEquals(4, lines.size());
        assertEquals("第5坑道へは", lines.get(1));
    }

    @Test
    void 到達ボーナスのメッセージ() {
        assertEquals("到達ボーナス +2,640個（1階層ぶん）",
                LevelGuide.bonusMessage(new ArrivalBonus.Result(0, 1, BigNum.of(2_640))));
        assertEquals("到達ボーナス +1×10^100個（1,000,000階層ぶん）",
                LevelGuide.bonusMessage(new ArrivalBonus.Result(0, 1_000_000, BigNum.of(1, 100))));
    }

    @Test
    void 道しるべは足元の先からケージの方へ並ぶ() {
        List<Double> xs = LevelGuide.guidePointXs(50.5, -8);
        assertEquals(LevelGuide.GUIDE_MAX_POINTS, xs.size());
        assertEquals(49.5, xs.get(0), 1e-9);
        for (int i = 1; i < xs.size(); i++) {
            assertEquals(-LevelGuide.GUIDE_STEP, xs.get(i) - xs.get(i - 1), 1e-9);
        }
    }

    @Test
    void ケージの手前で止まる() {
        List<Double> xs = LevelGuide.guidePointXs(-3.5, -8);
        assertTrue(xs.stream().allMatch(x -> x >= -7.5), xs::toString);
        assertEquals(List.of(-4.5, -6.0, -7.5), xs);
    }

    @Test
    void ケージのそばでは1つだけ出す() {
        assertEquals(List.of(-6.5), LevelGuide.guidePointXs(-7.2, -8));
    }
}
