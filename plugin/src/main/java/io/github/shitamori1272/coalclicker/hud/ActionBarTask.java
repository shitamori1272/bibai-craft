package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.data.DataStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/** オンラインの全員に、1 秒ごとにアクションバーで状況を出す。 */
public final class ActionBarTask implements Runnable {

    private static final long PERIOD_TICKS = 20L;

    private final DataStore dataStore;

    private ActionBarTask(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /** タスクを始める。onEnable から呼ぶ。プラグインの停止時に自動で止まる。 */
    public static BukkitTask start(JavaPlugin plugin, DataStore dataStore) {
        return Bukkit.getScheduler().runTaskTimer(plugin, new ActionBarTask(dataStore), PERIOD_TICKS, PERIOD_TICKS);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(Component.text(HudText.actionBar(dataStore.get(player)), NamedTextColor.GOLD));
        }
    }
}
