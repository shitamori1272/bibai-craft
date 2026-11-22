package io.github.shitamori1272.coalclicker.boost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class BoostTest {

    private static void assertClose(double expected, BigNum actual) {
        assertEquals(1.0, actual.divide(BigNum.of(expected)).toDouble(), 1e-9,
                () -> "期待 " + expected + " / 実際 " + actual);
    }

    // ---- 抽選 ----

    @Test
    void 重みの幅の境目で効果が変わる() {
        assertEquals(100, BoostEffect.totalWeight());
        assertEquals(BoostEffect.RICH_OUTPUT, BoostEffect.pick(0.0));
        assertEquals(BoostEffect.RICH_OUTPUT, BoostEffect.pick(0.4499));
        assertEquals(BoostEffect.JACKPOT, BoostEffect.pick(0.45));
        assertEquals(BoostEffect.JACKPOT, BoostEffect.pick(0.8999));
        assertEquals(BoostEffect.HAND_FESTIVAL, BoostEffect.pick(0.9));
        assertEquals(BoostEffect.HAND_FESTIVAL, BoostEffect.pick(0.99999));
        assertThrows(IllegalArgumentException.class, () -> BoostEffect.pick(1.0));
        assertThrows(IllegalArgumentException.class, () -> BoostEffect.pick(-0.1));
    }

    @Test
    void 種を決めた乱数ではおおむね重みどおりに出て同じ種なら同じ並び() {
        Random random = new Random(63);
        Map<BoostEffect, Integer> counts = new EnumMap<>(BoostEffect.class);
        int n = 20_000;
        for (int i = 0; i < n; i++) {
            counts.merge(BoostEffect.pick(random), 1, Integer::sum);
        }
        assertEquals(0.45, counts.get(BoostEffect.RICH_OUTPUT) / (double) n, 0.02);
        assertEquals(0.45, counts.get(BoostEffect.JACKPOT) / (double) n, 0.02);
        assertEquals(0.10, counts.get(BoostEffect.HAND_FESTIVAL) / (double) n, 0.01);

        Random a = new Random(7);
        Random b = new Random(7);
        for (int i = 0; i < 50; i++) {
            assertEquals(BoostEffect.pick(a), BoostEffect.pick(b));
        }
    }

    @Test
    void 次の炭層までは2分から5分() {
        Random random = new Random(1);
        for (int i = 0; i < 1_000; i++) {
            long interval = RichSeamRules.nextIntervalMillis(random);
            assertTrue(interval >= 120_000 && interval <= 300_000, () -> "範囲外: " + interval);
        }
    }

    // ---- 大当たり ----

    @Test
    void 大当たりは石炭の15パーセントに13を足す() {
        // 石炭 1000・毎秒 100：min(150, 90000) + 13
        assertClose(163, RichSeamRules.jackpotCoal(BigNum.of(1_000), BigNum.of(100)));
    }

    @Test
    void 大当たりは毎秒の900秒分で頭打ち() {
        // 石炭 1e9・毎秒 10：min(1.5e8, 9000) + 13
        assertClose(9_013, RichSeamRules.jackpotCoal(BigNum.of(1e9), BigNum.of(10)));
        // 大きな数でも同じ
        assertClose(9e300, RichSeamRules.jackpotCoal(BigNum.of(1, 400), BigNum.of(1, 298)));
    }

    @Test
    void 何もなくても大当たりは13() {
        assertClose(13, RichSeamRules.jackpotCoal(BigNum.ZERO, BigNum.ZERO));
    }

    // ---- 時間と倍率 ----

    @Test
    void 効果がなければ倍率は1() {
        BoostService service = new BoostService(() -> 0L);
        UUID id = UUID.randomUUID();
        assertEquals(1.0, service.productionMultiplier(id));
        assertEquals(1.0, service.clickMultiplier(id));
        assertEquals(0, service.remainingMillis(id, BoostEffect.RICH_OUTPUT));
    }

    @Test
    void 豊炭は77秒だけ毎秒の石炭が7倍() {
        AtomicLong clock = new AtomicLong(1_000);
        BoostService service = new BoostService(clock::get);
        UUID id = UUID.randomUUID();
        service.activate(id, BoostEffect.RICH_OUTPUT);
        assertEquals(7.0, service.productionMultiplier(id));
        assertEquals(1.0, service.clickMultiplier(id));
        assertEquals(77_000, service.remainingMillis(id, BoostEffect.RICH_OUTPUT));

        clock.addAndGet(76_999);
        assertEquals(7.0, service.productionMultiplier(id));
        assertEquals(1, service.remainingMillis(id, BoostEffect.RICH_OUTPUT));

        clock.addAndGet(1);
        assertEquals(1.0, service.productionMultiplier(id));
        assertEquals(0, service.remainingMillis(id, BoostEffect.RICH_OUTPUT));
    }

    @Test
    void 手掘り祭りは13秒だけ手掘りが777倍() {
        AtomicLong clock = new AtomicLong(0);
        BoostService service = new BoostService(clock::get);
        UUID id = UUID.randomUUID();
        service.activate(id, BoostEffect.HAND_FESTIVAL);
        assertEquals(777.0, service.clickMultiplier(id));
        assertEquals(1.0, service.productionMultiplier(id));
        clock.set(13_000);
        assertEquals(1.0, service.clickMultiplier(id));
    }

    @Test
    void 同じ効果は重ねず残り時間だけ戻る_違う効果は同時に続く() {
        AtomicLong clock = new AtomicLong(0);
        BoostService service = new BoostService(clock::get);
        UUID id = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        service.activate(id, BoostEffect.RICH_OUTPUT);
        clock.set(50_000);
        service.activate(id, BoostEffect.RICH_OUTPUT);
        service.activate(id, BoostEffect.HAND_FESTIVAL);
        assertEquals(7.0, service.productionMultiplier(id));
        assertEquals(777.0, service.clickMultiplier(id));
        assertEquals(77_000, service.remainingMillis(id, BoostEffect.RICH_OUTPUT));
        // ほかの人には掛からない
        assertEquals(1.0, service.productionMultiplier(other));

        clock.set(63_000);
        service.purgeExpired();
        assertEquals(1.0, service.clickMultiplier(id));
        assertEquals(7.0, service.productionMultiplier(id));

        service.clear(id);
        assertEquals(1.0, service.productionMultiplier(id));
    }

    @Test
    void 大当たりは時間のある効果として始められない() {
        BoostService service = new BoostService(() -> 0L);
        assertThrows(IllegalArgumentException.class,
                () -> service.activate(UUID.randomUUID(), BoostEffect.JACKPOT));
    }
}
