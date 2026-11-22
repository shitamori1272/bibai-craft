package io.github.shitamori1272.coalclicker;

import io.github.shitamori1272.coalclicker.admin.GiveCommand;
import io.github.shitamori1272.coalclicker.admin.ResetCommand;
import io.github.shitamori1272.coalclicker.command.CoalCommand;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.dig.Digging;
import io.github.shitamori1272.coalclicker.hud.ActionBarTask;
import io.github.shitamori1272.coalclicker.hud.StatusCommand;
import io.github.shitamori1272.coalclicker.mine.MineCommand;
import io.github.shitamori1272.coalclicker.mine.MineService;
import io.github.shitamori1272.coalclicker.mine.SurfaceCommand;
import io.github.shitamori1272.coalclicker.offline.OfflineListener;
import io.github.shitamori1272.coalclicker.shop.Shop;
import io.github.shitamori1272.coalclicker.world.MineWorld;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

/** 石炭クリッカーのエントリポイント。各機能はここから登録する。 */
public final class CoalClicker extends JavaPlugin {

    private final CoalCommand command = new CoalCommand();
    private MineWorld mineWorld;
    private DataStore dataStore;
    private MineService mineService;
    private Shop shop;
    private Digging digging;

    @Override
    public void onEnable() {
        // 主ワールドの読み込み後（POSTWORLD）に呼ばれるので、ここで坑道ワールドを作れる
        mineWorld = MineWorld.load(this);
        dataStore = new DataStore(this);
        dataStore.start();
        mineService = new MineService(this, dataStore, mineWorld);
        getServer().getPluginManager().registerEvents(mineService, this);
        command.register(new MineCommand(mineService));
        command.register(new SurfaceCommand(mineService));
        // サブコマンドは各機能がここで command.register(...) する
        shop = Shop.register(this);
        digging = Digging.register(this);
        OfflineListener offline = new OfflineListener(dataStore);
        getServer().getPluginManager().registerEvents(offline, this);
        offline.grantOnlinePlayers();
        ActionBarTask.start(this, dataStore);
        command.register(new StatusCommand(dataStore));
        command.register(new GiveCommand(dataStore));
        command.register(new ResetCommand(dataStore));
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register("coal", "石炭クリッカー", List.of(), command));
        getLogger().info("石炭クリッカーを有効にしました");
    }

    /** 坑道の専用ワールド。 */
    public MineWorld mineWorld() {
        return mineWorld;
    }

    @Override
    public void onDisable() {
        if (shop != null) {
            shop.close();
        }
        if (digging != null) {
            digging.close();
        }
        if (mineService != null) {
            mineService.shutdown();
        }
        if (dataStore != null) {
            dataStore.saveAll();
        }
        getLogger().info("石炭クリッカーを無効にしました");
    }

    /** {@code /coal} コマンド。サブコマンドの登録先。 */
    public CoalCommand command() {
        return command;
    }

    /** 入坑・坑道の作り直しの係。 */
    public MineService mineService() {
        return mineService;
    }

    /** 保存データの係。 */
    public DataStore dataStore() {
        return dataStore;
    }
}
