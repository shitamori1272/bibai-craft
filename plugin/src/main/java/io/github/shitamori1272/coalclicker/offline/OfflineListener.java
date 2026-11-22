package io.github.shitamori1272.coalclicker.offline;

import io.github.shitamori1272.coalclicker.data.DataStore;
import java.time.Instant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * ログイン時に留守の分を反映して知らせる（#27）。計算は {@link OfflineProgress}。
 *
 * <p>DataStore が LOWEST で読み込んだあと（NORMAL）に、同じ tick の中で 1 回だけ反映する。
 * 毎秒の自動の掘進はこのあとの tick から始まるので、その間の時間を二重に数えることはない。
 * 反映したらすぐ保存し、自動保存の前に落ちても同じ時間をもう一度付与しないようにする。
 */
public final class OfflineListener implements Listener {

    private final DataStore dataStore;

    public OfflineListener(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /** /reload などで有効化時にすでにオンラインの人へも反映する。DataStore.start のあとに呼ぶ。 */
    public void grantOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            grant(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        grant(event.getPlayer());
    }

    private void grant(Player player) {
        OfflineProgress.grant(dataStore.get(player), dataStore.server(), Instant.now().getEpochSecond())
                .ifPresent(text -> {
                    dataStore.saveNow(player.getUniqueId());
                    player.sendMessage(Component.text(text, NamedTextColor.GOLD));
                });
    }
}
