package io.github.shitamori1272.coalclicker.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 1 種類のランキング。Bukkit に依存しない。
 *
 * <p>値の大きい順に並べ、同じ値は同じ順位にする（例: 1 位・2 位・2 位・4 位）。
 * 同じ値どうしの表示順は名前順（名前不明は後ろ）、さらに UUID 順で固定する。
 */
public final class Ranking {

    /** 順位付きの 1 行。rank は 1 始まり。 */
    public record Ranked(int rank, PlayerStats stats) {
    }

    private final Category category;
    private final List<Ranked> rows;

    private Ranking(Category category, List<Ranked> rows) {
        this.category = category;
        this.rows = rows;
    }

    /** 全員の値から作る。同じ UUID が複数あれば後ろのもの（新しいデータ）を使う。 */
    public static Ranking of(Category category, Collection<PlayerStats> players) {
        Map<UUID, PlayerStats> unique = new LinkedHashMap<>();
        for (PlayerStats stats : players) {
            unique.put(stats.uuid(), stats);
        }
        Comparator<PlayerStats> byValue = category.ascending().reversed();
        Comparator<PlayerStats> order = byValue
                .thenComparing(PlayerStats::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(PlayerStats::uuid);
        List<PlayerStats> sorted = new ArrayList<>(unique.values());
        sorted.sort(order);
        List<Ranked> rows = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            PlayerStats stats = sorted.get(i);
            boolean tie = i > 0 && byValue.compare(sorted.get(i - 1), stats) == 0;
            rows.add(new Ranked(tie ? rows.get(i - 1).rank() : i + 1, stats));
        }
        return new Ranking(category, List.copyOf(rows));
    }

    public Category category() {
        return category;
    }

    /** 並べた先頭から limit 人。同じ順位が境目をまたいでも limit 人で切る。 */
    public List<Ranked> top(int limit) {
        return rows.subList(0, Math.clamp(limit, 0, rows.size()));
    }

    /** そのプレイヤーの順位。載っていなければ空。 */
    public Optional<Ranked> rankOf(UUID uuid) {
        return rows.stream().filter(row -> row.stats().uuid().equals(uuid)).findFirst();
    }

    /** 載っている人数。 */
    public int size() {
        return rows.size();
    }
}
