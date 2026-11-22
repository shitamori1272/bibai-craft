package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.CoalClicker;
import org.bukkit.entity.Player;

/** ショップ機能をまとめて登録する。 */
public final class Shop {

    /** 開いているショップ画面を描き直す間隔（tick）。放置中に増えた石炭を反映する。 */
    private static final long REFRESH_TICKS = 20;

    private Shop() {
    }

    /** リスナー・{@code /coal shop}・画面の定期更新を登録する。 */
    public static ShopListener register(CoalClicker plugin) {
        ShopListener listener = new ShopListener(plugin.dataStore());
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.command().register(new ShopCommand(listener));
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                listener.refresh(player);
            }
        }, REFRESH_TICKS, REFRESH_TICKS);
        return listener;
    }
}
