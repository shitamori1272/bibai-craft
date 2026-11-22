package io.github.shitamori1272.coalclicker.offline;

import io.github.shitamori1272.coalclicker.data.DataStore;
import java.time.Instant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
 * /reload でもともとオンラインだった人にはログインイベントが来ないので反映しない。
 */
public final class OfflineListener implements Listener {

    private final DataStore dataStore;

    public OfflineListener(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        OfflineProgress.grant(dataStore.get(player), dataStore.server(), Instant.now().getEpochSecond())
                .ifPresent(text -> player.sendMessage(Component.text(text, NamedTextColor.GOLD)));
    }
}
