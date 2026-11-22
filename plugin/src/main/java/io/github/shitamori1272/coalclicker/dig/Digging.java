package io.github.shitamori1272.coalclicker.dig;

import io.github.shitamori1272.coalclicker.CoalClicker;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.mine.MineService;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * 掘進の機能（自動の掘進 #15・手掘り #14）をまとめて登録し、無効化のときに片付ける。
 *
 * <p>自動の掘進は 1 秒（20 tick）ごとに、オンラインの全員を {@link DigEngine#autoTick} で 1 秒進める。
 * 坑外にいる人は数値だけ進み、坑道のブロックは持ち主が自分の坑道にいるときだけ {@link MineService} が直す。
 * オフライン中の分はここでは扱わない（#27）。
 */
public final class Digging {

    private static final long PERIOD_TICKS = 20;

    private final ManualDigListener manual;
    private final BukkitTask task;

    private Digging(ManualDigListener manual, BukkitTask task) {
        this.manual = manual;
        this.task = task;
    }

    /** 手掘りのリスナーと、自動の掘進のタスクを登録する。onEnable から呼ぶ。 */
    public static Digging register(CoalClicker plugin) {
        DataStore dataStore = plugin.dataStore();
        MineService mineService = plugin.mineService();
        ManualDigListener manual = new ManualDigListener(dataStore, mineService);
        plugin.getServer().getPluginManager().registerEvents(manual, plugin);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                DigEngine.Change change = DigEngine.autoTick(dataStore.get(player), dataStore.server(),
                        PERIOD_TICKS / 20.0);
                showProgress(mineService, player.getUniqueId(), change);
                manual.refresh(player);
            }
        }, PERIOD_TICKS, PERIOD_TICKS);
        return new Digging(manual, task);
    }

    /** タスクを止め、表示中のヒビを消す。 */
    public void close() {
        task.cancel();
        manual.clearAll();
    }

    /**
     * 進んだ分の坑道のブロックを直すよう {@link MineService} に知らせる。
     * 階層を掘り切ったときは、置いてある坑道を掘り切った状態まで進める。次の階層の坑道はケージで降りたときに作る（#22）。
     */
    static void showProgress(MineService mineService, UUID playerId, DigEngine.Change change) {
        if (!change.moved()) {
            return;
        }
        if (change.completedOldLevel()) {
            mineService.onProgress(playerId, change.oldLevel(), change.oldMeters(), change.oldLevelLength());
            // 置いてある坑道は古い階層のままなので、ふつうは何もしない
            mineService.onProgress(playerId, change.newLevel(), 0, change.newMeters());
        } else {
            mineService.onProgress(playerId, change.newLevel(), change.oldMeters(), change.newMeters());
        }
    }
}
