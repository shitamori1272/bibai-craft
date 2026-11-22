package io.github.shitamori1272.coalclicker;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.world.MineWorld;
import org.bukkit.plugin.java.JavaPlugin;

/** 石炭クリッカーのエントリポイント。各機能はここから登録する。 */
public final class CoalClicker extends JavaPlugin {

    private MineWorld mineWorld;
    private DataStore dataStore;

    @Override
    public void onEnable() {
        // 主ワールドの読み込み後（POSTWORLD）に呼ばれるので、ここで坑道ワールドを作れる
        mineWorld = MineWorld.load(this);
        dataStore = new DataStore(this);
        dataStore.start();
        getLogger().info("石炭クリッカーを有効にしました");
    }

    /** 坑道の専用ワールド。 */
    public MineWorld mineWorld() {
        return mineWorld;
    }

    @Override
    public void onDisable() {
        if (dataStore != null) {
            dataStore.saveAll();
        }
        getLogger().info("石炭クリッカーを無効にしました");
    }

    /** 保存データの係。 */
    public DataStore dataStore() {
        return dataStore;
    }
}
