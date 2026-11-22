package io.github.shitamori1272.coalclicker.game;

import static io.github.shitamori1272.coalclicker.game.FormulasTest.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ProgressTest {

    private static final BigNum E100 = BigNum.of(100);

    @Test
    void 掘進力が0なら進まない() {
        Progress.Result r = Progress.advance(3, 0.25, BigNum.ZERO, E100, 8 * 3600);
        assertEquals(3, r.level());
        assertEquals(0.25, r.progress());
        assertTrue(r.coal().isZero());
        assertEquals(0, r.levelsCompleted());
        assertEquals(0, r.meters());
    }

    @Test
    void 経過時間が0なら進まない() {
        Progress.Result r = Progress.advance(0, 0.5, BigNum.ONE, E100, 0);
        assertEquals(0.5, r.progress());
        assertTrue(r.coal().isZero());
    }

    @Test
    void 発破1つなら10秒で1m進む() {
        Progress.Result r = Progress.advance(0, 0, BigNum.ONE, E100, 10);
        assertEquals(0, r.level());
        assertEquals(1.0 / 64, r.progress(), 1e-12);
        assertEquals(1.0, r.meters(), 1e-12);
        assertClose(100, r.coal());
    }

    @Test
    void 深い階層ほど1mあたりの石炭が多い() {
        Progress.Result r = Progress.advance(2, 0, BigNum.ONE, E100, 10);
        assertClose(121, r.coal());
    }

    @Test
    void 掘り切ったら次の階層を0mから掘り始め超えた分は捨てる() {
        // 残り 0.64m（6.4 秒）。その tick の残り 0.6 秒は捨て、7 秒目から第 2 坑道を 3 秒掘る
        Progress.Result r = Progress.advance(0, 0.99, BigNum.ONE, E100, 10);
        assertEquals(1, r.level());
        assertEquals(1, r.levelsCompleted());
        assertEquals(3.0 / 800, r.progress(), 1e-12);
        assertEquals(0.64 + 0.3, r.meters(), 1e-9);
        assertClose(0.64 * 100 + 0.3 * 110, r.coal());
    }

    @Test
    void 区切りを0にすると残り時間を持ち越す() {
        Progress.Result r = Progress.advance(0, 0.99, BigNum.ONE, E100, 10, 0);
        assertEquals(1, r.level());
        assertEquals(3.6 / 800, r.progress(), 1e-12);
        assertEquals(0.64 + 0.36, r.meters(), 1e-9);
    }

    @Test
    void 一つの階層は最短10秒() {
        // 速度 1 万 m/秒でも 1 階層 10 秒。100 秒でちょうど 10 階層
        BigNum d = BigNum.of(1, 10);
        Progress.Result r = Progress.advance(0, 0, d, E100, 100);
        assertEquals(10, r.level());
        assertEquals(10, r.levelsCompleted());
        assertEquals(0, r.progress());
        double meters = 0;
        BigNum coal = BigNum.ZERO;
        for (int level = 0; level < 10; level++) {
            meters += Formulas.tunnelLength(level);
            coal = coal.add(Formulas.coalPerMeter(E100, level).multiply(Formulas.tunnelLength(level)));
        }
        assertEquals(meters, r.meters(), 1e-9);
        assertClose(coal, r.coal());
    }

    @Test
    void 一秒ずつ進めても一度に進めても同じ() {
        // 速度 10m/秒：第 1 坑道は 6.4 秒、第 2 坑道は 10 秒（最短）、第 3 坑道は 10 秒…
        BigNum d = BigNum.of(10_000);
        BigNum e = BigNum.of(1330);
        long level = 0;
        double progress = 0.3;
        BigNum coal = BigNum.ZERO;
        for (int i = 0; i < 500; i++) {
            Progress.Result step = Progress.advance(level, progress, d, e, 1);
            level = step.level();
            progress = step.progress();
            coal = coal.add(step.coal());
        }
        Progress.Result once = Progress.advance(0, 0.3, d, e, 500);
        assertEquals(level, once.level());
        assertEquals(progress, once.progress(), 1e-9);
        assertClose(coal, once.coal());
    }

    @Test
    void 長い放置でも速く終わる() {
        // 8 時間・最短 10 秒の階層が続く → 2,880 階層
        Progress.Result r = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> Progress.advance(0, 0, BigNum.of(1, 30), E100, 8 * 3600));
        assertEquals(2_880, r.level());
        assertTrue(r.coal().log10() > 2_879 * Math.log10(1.1));
    }

    @Test
    void 企画書の掘進速度なら放置中に階層が進む() {
        // 4.0m/秒（D = 1600）で第 109 坑道あたり：1 階層 約 7 分
        long level = 108;
        Progress.Result r = Progress.advance(level, 0, BigNum.of(1600), E100, 3600);
        double first = Formulas.levelSeconds(level, BigNum.of(1600));
        assertEquals(1792 / 4.0, first, 1e-9);
        assertTrue(r.levelsCompleted() == 7, () -> "階層: " + r.levelsCompleted());
    }

    @Test
    void 掘り切ったまま保存されていたら次の階層から始める() {
        Progress.Result r = Progress.advance(4, 1.0, BigNum.ONE, E100, 10);
        assertEquals(5, r.level());
        assertEquals(1, r.levelsCompleted());
        assertEquals(1.0 / Formulas.tunnelLength(5), r.progress(), 1e-12);
    }

    @Test
    void 掘り切ったまま保存されていたら掘進力0でも時間0でも次の階層へ進める() {
        Progress.Result noPower = Progress.advance(4, 1.0, BigNum.ZERO, E100, 10);
        assertEquals(5, noPower.level());
        assertEquals(1, noPower.levelsCompleted());
        assertEquals(0, noPower.progress());
        assertTrue(noPower.coal().isZero());
        Progress.Result noTime = Progress.advance(4, 1.0, BigNum.ONE, E100, 0);
        assertEquals(5, noTime.level());
        assertEquals(0, noTime.progress());
    }

    @Test
    void とても深い階層でも1秒ごとに進み具合が増え最後は掘り切れる() {
        long level = 1_000_000_000_000_000_000L;
        // 1 秒の増分（約 6×10^-21）は割合の精度よりずっと小さい
        double progress = 0.5;
        for (int i = 0; i < 5; i++) {
            Progress.Result r = Progress.advance(level, progress, BigNum.ONE, E100, 1);
            assertEquals(level, r.level());
            assertTrue(r.progress() > progress, () -> "進んでいない: " + r.progress());
            assertTrue(r.coal().signum() > 0);
            progress = r.progress();
        }
        // 1 の直前から始めると、数回で掘り切って次の階層へ進む
        progress = Math.nextDown(Math.nextDown(Math.nextDown(1.0)));
        for (int i = 0; i < 10 && level == 1_000_000_000_000_000_000L; i++) {
            Progress.Result r = Progress.advance(level, progress, BigNum.ONE, E100, 1);
            level = r.level();
            progress = r.progress();
        }
        assertEquals(1_000_000_000_000_000_001L, level);
        assertEquals(0, progress);
    }

    @Test
    void 最後の階層は掘り切れず番号があふれない() {
        double last = Math.nextDown(1.0);
        Progress.Result r = Progress.advance(Long.MAX_VALUE, 0.999, BigNum.of(1, 300), E100, 3600);
        assertEquals(Long.MAX_VALUE, r.level());
        assertEquals(last, r.progress());
        assertEquals(0, r.levelsCompleted());
        Progress.Result again = Progress.advance(Long.MAX_VALUE, last, BigNum.of(1, 300), E100, 3600);
        assertEquals(Long.MAX_VALUE, again.level());
        assertEquals(last, again.progress());
        Progress.Result saved = Progress.advance(Long.MAX_VALUE, 1.0, BigNum.ZERO, E100, 0);
        assertEquals(Long.MAX_VALUE, saved.level());
        assertEquals(last, saved.progress());
        assertEquals(0, saved.levelsCompleted());
    }

    @Test
    void 不正な入力() {
        assertThrows(IllegalArgumentException.class, () -> Progress.advance(-1, 0, BigNum.ONE, E100, 1));
        assertThrows(IllegalArgumentException.class, () -> Progress.advance(0, Double.NaN, BigNum.ONE, E100, 1));
        assertThrows(IllegalArgumentException.class,
                () -> Progress.advance(0, 0, BigNum.ONE, E100, Double.POSITIVE_INFINITY));
    }

    // ---- 長さで進める（手掘り） ----

    @Test
    void 長さで進めると割合が増え石炭は掘った分() {
        Progress.Result r = Progress.advanceMeters(0, 0.5, 1, E100);
        assertEquals(0, r.level());
        assertEquals(0.5 + 1.0 / 64, r.progress(), 1e-12);
        assertEquals(1, r.meters(), 1e-12);
        assertEquals(0, r.levelsCompleted());
        assertClose(100, r.coal());
    }

    @Test
    void 長さで進めて掘り切ったら次の階層を0mから() {
        Progress.Result r = Progress.advanceMeters(1, 79.5 / 80, 1, E100);
        assertEquals(2, r.level());
        assertEquals(0, r.progress());
        assertEquals(1, r.levelsCompleted());
        assertEquals(0.5, r.meters(), 1e-9);
        assertClose(50, r.coal());
    }

    @Test
    void 長さで進めてちょうど掘り切る() {
        Progress.Result r = Progress.advanceMeters(0, 63.0 / 64, 1, E100);
        assertEquals(1, r.level());
        assertEquals(0, r.progress());
        assertClose(100, r.coal());
    }

    @Test
    void 長さ0なら進まない() {
        Progress.Result r = Progress.advanceMeters(0, 0.25, 0, E100);
        assertEquals(0.25, r.progress());
        assertTrue(r.coal().isZero());
    }

    @Test
    void 長さで進めるとき掘り切ったまま残っていたら先に次の階層へ() {
        Progress.Result r = Progress.advanceMeters(0, 1, 1, E100);
        assertEquals(1, r.level());
        assertEquals(1, r.levelsCompleted());
        assertEquals(1.0 / 80, r.progress(), 1e-12);
    }

    @Test
    void とても深い階層でも長さで進めると止まらない() {
        Progress.Result r = Progress.advanceMeters(Long.MAX_VALUE / 32, 0.5, 1, E100);
        assertEquals(Long.MAX_VALUE / 32, r.level());
        assertTrue(r.progress() > 0.5);
        assertEquals(1, r.meters());
        assertClose(100, r.coal());
    }

    @Test
    void とても深い階層で1の直前から進めても渡した長さ分しかもらえない() {
        // 残りは割合の最小単位 × 長さで何千 m にもなるが、1m 分の石炭だけで次の階層へ進む
        long level = Long.MAX_VALUE / 32;
        Progress.Result r = Progress.advanceMeters(level, Math.nextDown(1.0), 1, E100);
        assertEquals(level + 1, r.level());
        assertEquals(0, r.progress());
        assertEquals(1, r.meters());
        assertClose(100, r.coal());
    }

    @Test
    void 最後の階層は長さで進めても掘り切れない() {
        Progress.Result r = Progress.advanceMeters(Long.MAX_VALUE, Math.nextDown(1.0), 1, E100);
        assertEquals(Long.MAX_VALUE, r.level());
        assertEquals(Math.nextDown(1.0), r.progress());
        assertEquals(0, r.levelsCompleted());
        assertEquals(1, r.meters());
        assertClose(100, r.coal());
    }

    @Test
    void 長さで進めるときの不正な入力() {
        assertThrows(IllegalArgumentException.class, () -> Progress.advanceMeters(0, 0, -1, E100));
        assertThrows(IllegalArgumentException.class, () -> Progress.advanceMeters(-1, 0, 1, E100));
        assertThrows(IllegalArgumentException.class, () -> Progress.advanceMeters(0, Double.NaN, 1, E100));
    }
}
