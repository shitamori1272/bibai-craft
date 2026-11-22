package io.github.shitamori1272.coalclicker.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RankingTest {

    private static UUID id(int n) {
        return new UUID(0, n);
    }

    private static PlayerStats mined(int n, String name, double total) {
        return new PlayerStats(id(n), name, BigNum.of(total), 0, BigNum.ZERO);
    }

    private static List<String> names(List<Ranking.Ranked> rows) {
        return rows.stream().map(r -> r.rank() + ":" + r.stats().displayName()).toList();
    }

    @Test
    void 値の大きい順に並べる() {
        Ranking ranking = Ranking.of(Category.MINED,
                List.of(mined(1, "a", 10), mined(2, "b", 1e30), mined(3, "c", 500)));
        assertEquals(List.of("1:b", "2:c", "3:a"), names(ranking.top(10)));
    }

    @Test
    void 同じ値は同じ順位で次の順位は飛ばす() {
        Ranking ranking = Ranking.of(Category.MINED, List.of(
                mined(1, "d", 5), mined(2, "c", 100), mined(3, "b", 100), mined(4, "a", 1)));
        // 同じ値どうしは名前順
        assertEquals(List.of("1:b", "1:c", "3:d", "4:a"), names(ranking.top(10)));
    }

    @Test
    void 上位だけを切り出す() {
        List<PlayerStats> players = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            players.add(mined(i, "p" + (char) ('a' + i), i));
        }
        Ranking ranking = Ranking.of(Category.MINED, players);
        assertEquals(10, ranking.top(10).size());
        assertEquals(15, ranking.size());
        assertEquals("po", ranking.top(1).getFirst().stats().displayName());
        assertEquals(0, Ranking.of(Category.MINED, List.of()).top(10).size());
    }

    @Test
    void 自分の順位を探せる() {
        List<PlayerStats> players = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            players.add(mined(i, "p" + i, i));
        }
        Ranking ranking = Ranking.of(Category.MINED, players);
        assertEquals(20, ranking.rankOf(id(0)).orElseThrow().rank());
        assertEquals(1, ranking.rankOf(id(19)).orElseThrow().rank());
        assertTrue(ranking.rankOf(id(99)).isEmpty());
    }

    @Test
    void 同じUUIDは後ろのデータを使う() {
        Ranking ranking = Ranking.of(Category.MINED, List.of(mined(1, "old", 1), mined(2, "x", 5), mined(1, "new", 9)));
        assertEquals(List.of("1:new", "2:x"), names(ranking.top(10)));
    }

    @Test
    void 名前が分からなければUUIDの先頭を出し後ろに並べる() {
        Ranking ranking = Ranking.of(Category.MINED, List.of(mined(1, null, 3), mined(2, "z", 3)));
        assertEquals(List.of("1:z", "1:00000000"), names(ranking.top(10)));
    }

    @Test
    void 到達深さは最深と今の階層の深いほう() {
        PlayerData a = new PlayerData(id(1));
        a.mine().setMaxReachedLevel(5);
        a.setTotalMined(BigNum.of(3_000)); // 階層 2
        PlayerData b = new PlayerData(id(2));
        b.setTotalMined(BigNum.of(127_000)); // 階層 7
        PlayerStats sa = PlayerStats.from(a, "a");
        PlayerStats sb = PlayerStats.from(b, "b");
        assertEquals(5, sa.deepestLevel());
        assertEquals(7, sb.deepestLevel());
        Ranking ranking = Ranking.of(Category.DEPTH, List.of(sa, sb));
        assertEquals(List.of("1:b", "2:a"), names(ranking.top(10)));
        assertEquals("第8坑道（深さ800m）", Category.DEPTH.format(sb));
    }

    @Test
    void 毎秒採掘量で並べる() {
        PlayerStats slow = new PlayerStats(id(1), "slow", BigNum.of(1e9), 0, BigNum.of(1));
        PlayerStats fast = new PlayerStats(id(2), "fast", BigNum.ZERO, 0, BigNum.of(12345));
        Ranking ranking = Ranking.of(Category.RATE, List.of(slow, fast));
        assertEquals(List.of("1:fast", "2:slow"), names(ranking.top(10)));
        assertEquals("毎秒 1.234万個", Category.RATE.format(fast));
        assertEquals("10億個", Category.MINED.format(slow));
    }

    @Test
    void カテゴリを名前で探す() {
        assertEquals(Category.RATE, Category.byId("RATE").orElseThrow());
        assertEquals(Category.MINED, Category.byId("mined").orElseThrow());
        assertTrue(Category.byId("coal").isEmpty());
    }

    @Test
    void キャッシュは期限まで使い回す() {
        long[] now = {0};
        int[] loads = {0};
        ExpiringValue<Integer> value = new ExpiringValue<>(() -> ++loads[0], () -> now[0], 30_000);
        assertEquals(1, value.get());
        now[0] = 29_999;
        assertEquals(1, value.get());
        now[0] = 30_000;
        assertEquals(2, value.get());
        assertEquals(2, loads[0]);
    }

    @Test
    void キャッシュを捨てると期限前でも作り直す() {
        int[] loads = {0};
        ExpiringValue<Integer> value = new ExpiringValue<>(() -> ++loads[0], () -> 0, 30_000);
        assertEquals(1, value.get());
        value.invalidate();
        assertEquals(2, value.get());
        assertEquals(2, value.get());
    }

    @Test
    void サイドバーの行() {
        assertEquals("全体の出炭量 0個", SidebarText.serverTotal(BigNum.ZERO));
        assertEquals("全体の出炭量 1.234万個", SidebarText.serverTotal(BigNum.of(12345)));
    }
}
