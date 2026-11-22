package io.github.shitamori1272.coalclicker.ranking;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * ランキングの元になる全員分の値。オフラインの人も含める。
 *
 * <ul>
 *   <li>オフラインの人の値は保存ファイルを全部読んで作り、{@value #REFRESH_MILLIS} ミリ秒は使い回す
 *   <li>オンラインの人の値は毎回メモリ上の最新データで上書きする
 * </ul>
 *
 * <p>{@link DataStore} はメインスレッド専用なので、ファイルの読み込みもメインスレッドで同期的に行う。
 * 1 人 1 ファイルの小さな YAML で、読むのは 30 秒に 1 回まで（しかも /coal top を打ったときだけ）なので、
 * ハッカソン規模（数十〜数百人）なら問題ない。人数が増えたら非同期の集計に変えること。
 */
public final class Leaderboard {

    static final long REFRESH_MILLIS = 30_000;

    private final DataStore dataStore;
    private final ExpiringValue<Map<UUID, PlayerStats>> offline;

    public Leaderboard(DataStore dataStore) {
        this.dataStore = dataStore;
        this.offline = new ExpiringValue<>(this::loadOffline, System::currentTimeMillis, REFRESH_MILLIS);
    }

    /** 全員分の値（オンラインの人は最新）。 */
    public List<PlayerStats> stats() {
        Map<UUID, PlayerStats> all = new LinkedHashMap<>(offline.get());
        for (Player player : Bukkit.getOnlinePlayers()) {
            all.put(player.getUniqueId(), PlayerStats.from(dataStore.get(player), player.getName()));
        }
        return new ArrayList<>(all.values());
    }

    /** そのカテゴリのランキング。 */
    public Ranking ranking(Category category) {
        return Ranking.of(category, stats());
    }

    /** 保存ファイルから全員分を読む。オンラインの人は後で上書きするので読まない。 */
    private Map<UUID, PlayerStats> loadOffline() {
        Map<UUID, PlayerStats> result = new LinkedHashMap<>();
        for (UUID uuid : dataStore.savedPlayerIds()) {
            if (Bukkit.getPlayer(uuid) != null) {
                continue;
            }
            Optional<PlayerData> data = dataStore.loadOffline(uuid);
            data.ifPresent(d -> result.put(uuid, PlayerStats.from(d, Bukkit.getOfflinePlayer(uuid).getName())));
        }
        return result;
    }
}
