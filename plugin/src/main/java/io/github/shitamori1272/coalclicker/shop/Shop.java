package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.CoalClicker;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** ショップ機能をまとめて登録し、無効化のときに片付ける。 */
public final class Shop {

    /** 開いているショップ画面を描き直す間隔（tick）。放置中に増えた石炭を反映する。 */
    private static final long REFRESH_TICKS = 20;

    private final CoalClicker plugin;
    private final ShopListener listener;
    private final BukkitTask refreshTask;

    private Shop(CoalClicker plugin, ShopListener listener, BukkitTask refreshTask) {
        this.plugin = plugin;
        this.listener = listener;
        this.refreshTask = refreshTask;
    }

    /** リスナー・{@code /coal shop}・画面の定期更新を登録する。 */
    public static Shop register(CoalClicker plugin) {
        ShopListener listener = new ShopListener(plugin.dataStore(), plugin.mineService().pickaxe(),
                new FirstPurchaseEffects(plugin));
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.command().register(new ShopCommand(listener));
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                listener.refresh(player);
            }
        }, REFRESH_TICKS, REFRESH_TICKS);
        return new Shop(plugin, listener, task);
    }

    public ShopListener listener() {
        return listener;
    }

    /**
     * 定期更新を止め、開いているショップ画面をすべて閉じる。
     * リスナーが外れたあとに画面が残ると、中のアイテムを取り出せてしまうため。
     */
    public void close() {
        refreshTask.cancel();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof ShopMenu) {
                player.closeInventory();
            }
        }
    }
}
