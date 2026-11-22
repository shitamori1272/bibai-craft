package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.CoalClicker;
import io.github.shitamori1272.coalclicker.boost.BoostService;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.mine.MineService;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * 毎秒の石炭（企画書 4-1）と手掘り（4-2）をまとめて登録し、無効化のときに片付ける。
 *
 * <p>1 秒（20 tick）ごとに、オンラインの全員へ {@link DigEngine#autoTick} で 1 秒ぶんの石炭（良質な炭層の倍率込み）を足し、
 * {@link MineService#syncProgress} で坑道を累計採掘量に追いつかせる（1 秒に何 m 進んでもまとめて直す。企画書 5-3）。
 * 坑外にいる人は数値だけ進み、坑道のブロックは持ち主が自分の坑道にいるときだけ直す。
 * ログアウト中の分はログイン時に {@code offline.OfflineListener} がまとめて反映する。
 */
public final class Digging {

    private static final long PERIOD_TICKS = 20;

    private final ManualDigListener manual;
    private final BukkitTask task;

    private Digging(ManualDigListener manual, BukkitTask task) {
        this.manual = manual;
        this.task = task;
    }

    /** 手掘りのリスナーと、毎秒の石炭のタスクを登録する。onEnable から呼ぶ。 */
    public static Digging register(CoalClicker plugin) {
        DataStore dataStore = plugin.dataStore();
        MineService mineService = plugin.mineService();
        BoostService boosts = plugin.boostService();
        ManualDigListener manual = new ManualDigListener(plugin, dataStore, mineService, boosts);
        plugin.getServer().getPluginManager().registerEvents(manual, plugin);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                DigEngine.autoTick(dataStore.get(player), dataStore.server(), PERIOD_TICKS / 20.0,
                        boosts.productionMultiplier(player.getUniqueId()));
                mineService.syncProgress(player);
                manual.refresh(player);
            }
        }, PERIOD_TICKS, PERIOD_TICKS);
        return new Digging(manual, task);
    }

    /** タスクを止め、表示中のヒビと「+◯個」を消す。 */
    public void close() {
        task.cancel();
        manual.clearAll();
    }
}
