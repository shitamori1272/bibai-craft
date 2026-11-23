package io.github.shitamori1272.coalclicker.trophy;

import io.github.shitamori1272.coalclicker.data.DataStore;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/** トロフィーの一覧画面を開き、ページ送りだけ受け付ける。画面からアイテムは一切取り出させない。 */
public final class TrophyListener implements Listener {

    private final DataStore dataStore;

    public TrophyListener(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /** トロフィーの一覧を開く。 */
    public void open(Player player) {
        TrophyMenu menu = new TrophyMenu();
        menu.render(dataStore.get(player));
        player.openInventory(menu.getInventory());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof TrophyMenu menu)) {
            return;
        }
        // 自分の持ち物でのクリック（Shift で送る・数字キーなど）も含めてすべて止める
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != top) {
            return;
        }
        if (menu.turnPage(event.getRawSlot())) {
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            menu.render(dataStore.get(player));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof TrophyMenu) {
            event.setCancelled(true);
        }
    }
}
