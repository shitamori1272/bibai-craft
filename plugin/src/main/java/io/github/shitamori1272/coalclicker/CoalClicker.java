package io.github.shitamori1272.coalclicker;

import org.bukkit.plugin.java.JavaPlugin;

/** 石炭クリッカーのエントリポイント。各機能はここから登録する。 */
public final class CoalClicker extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("石炭クリッカーを有効にしました");
    }

    @Override
    public void onDisable() {
        getLogger().info("石炭クリッカーを無効にしました");
    }
}
