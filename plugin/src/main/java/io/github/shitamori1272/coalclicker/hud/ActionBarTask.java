package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.level.LevelGuide;
import io.github.shitamori1272.coalclicker.mine.MineService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * オンラインの全員に、1 秒ごとにアクションバーで状況を出す。
 * 坑内で置いてある坑道より数値上の階層が進んでいれば、ケージで降りるよう一言添える（#22）。
 */
public final class ActionBarTask implements Runnable {

    private static final long PERIOD_TICKS = 20L;

    private final DataStore dataStore;
    private final MineService mineService;

    private ActionBarTask(DataStore dataStore, MineService mineService) {
        this.dataStore = dataStore;
        this.mineService = mineService;
    }

    /** タスクを始める。onEnable から呼ぶ。プラグインの停止時に自動で止まる。 */
    public static BukkitTask start(JavaPlugin plugin, DataStore dataStore, MineService mineService) {
        return Bukkit.getScheduler().runTaskTimer(plugin, new ActionBarTask(dataStore, mineService),
                PERIOD_TICKS, PERIOD_TICKS);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = dataStore.get(player);
            boolean awaiting = mineService.renderedLevel(player)
                    .map(rendered -> LevelGuide.awaitingCage(data.mine().level(), rendered))
                    .orElse(false);
            player.sendActionBar(Component.text(HudText.actionBar(data, awaiting), NamedTextColor.GOLD));
        }
    }
}
