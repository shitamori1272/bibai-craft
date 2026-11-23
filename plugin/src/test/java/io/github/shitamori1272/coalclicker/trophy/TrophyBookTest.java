package io.github.shitamori1272.coalclicker.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.game.Trophy;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TrophyBookTest {

    @Test
    void トロフィーは68個で2ページに分かれる() {
        assertEquals(68, TrophyBook.TOTAL);
        assertEquals(2, TrophyBook.pageCount());
        assertEquals(45, TrophyBook.entries(0, Set.of(), BigNum.ZERO).size());
        assertEquals(23, TrophyBook.entries(1, Set.of(), BigNum.ZERO).size());
    }

    @Test
    void マスと節目の対応() {
        assertEquals(OptionalInt.of(4), TrophyBook.digitsAt(0, 0));
        assertEquals(OptionalInt.of(48), TrophyBook.digitsAt(0, 44));
        assertEquals(OptionalInt.of(49), TrophyBook.digitsAt(1, 0));
        assertEquals(OptionalInt.of(71), TrophyBook.digitsAt(1, 22));
        assertTrue(TrophyBook.digitsAt(1, 23).isEmpty());
        assertTrue(TrophyBook.digitsAt(0, TrophyBook.SUMMARY_SLOT).isEmpty());
        assertTrue(TrophyBook.digitsAt(0, -1).isEmpty());
    }

    @Test
    void もらったもの_次のもの_まだのものを見分ける() {
        Set<String> upgrades = Set.of("trophy_4", "trophy_5");
        List<TrophyBook.Entry> entries = TrophyBook.entries(0, upgrades, BigNum.of(300_000));

        TrophyBook.Entry earned = entries.get(0);
        assertEquals(TrophyBook.State.EARNED, earned.state());
        assertEquals("🏆 1万個 突破", earned.name());
        assertEquals(List.of("累計採掘量 1万個 で獲得", "生産 +5%"), earned.lore());

        TrophyBook.Entry next = entries.get(2);
        assertEquals(TrophyBook.State.NEXT, next.state());
        assertEquals("？ 100万個 突破", next.name());
        assertEquals(List.of("未獲得", "あと 70万個", "獲得すると 生産 +5%"), next.lore());

        TrophyBook.Entry locked = entries.get(3);
        assertEquals(TrophyBook.State.LOCKED, locked.state());
        assertEquals(List.of("未獲得"), locked.lore());
    }

    @Test
    void 残りの量は0未満にしない() {
        // 届いているがまだ渡していない（見回りの前）ときは「あと 0個」
        TrophyBook.Entry next = TrophyBook.entries(0, Set.of(), BigNum.of(20_000)).get(0);
        assertEquals("あと 0個", next.lore().get(1));
    }

    @Test
    void 全部もらうと次のものはない() {
        Set<String> all = new HashSet<>();
        for (int d = Trophy.MIN_DIGITS; d <= Trophy.MAX_DIGITS; d++) {
            all.add(Trophy.id(d));
        }
        assertTrue(TrophyBook.nextDigits(all).isEmpty());
        assertEquals("トロフィー 68/68 ・ 生産 +340%", TrophyBook.summary(all));
    }

    @Test
    void まとめの見出し() {
        assertEquals("トロフィー 0/68 ・ 生産 +0%", TrophyBook.summary(Set.of()));
        assertEquals("トロフィー 3/68 ・ 生産 +15%", TrophyBook.summary(Set.of("trophy_4", "trophy_5", "trophy_6")));
    }

    @Test
    void ページは範囲に収める() {
        assertEquals(0, TrophyBook.clampPage(-3));
        assertEquals(1, TrophyBook.clampPage(9));
    }
}
