package io.github.shitamori1272.coalclicker;

import io.github.shitamori1272.coalclicker.command.CoalCommand;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

/** 石炭クリッカーのエントリポイント。各機能はここから登録する。 */
public final class CoalClicker extends JavaPlugin {

    private final CoalCommand command = new CoalCommand();
    private DataStore dataStore;

    @Override
    public void onEnable() {
        dataStore = new DataStore(this);
        dataStore.start();
        // サブコマンドは各機能がここで command.register(...) する
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register("coal", "石炭クリッカー", List.of(), command));
        getLogger().info("石炭クリッカーを有効にしました");
    }

    @Override
    public void onDisable() {
        if (dataStore != null) {
            dataStore.saveAll();
        }
        getLogger().info("石炭クリッカーを無効にしました");
    }

    /** {@code /coal} コマンド。サブコマンドの登録先。 */
    public CoalCommand command() {
        return command;
    }

    /** 保存データの係。 */
    public DataStore dataStore() {
        return dataStore;
    }
}
