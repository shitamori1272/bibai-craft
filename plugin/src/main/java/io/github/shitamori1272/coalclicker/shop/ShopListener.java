package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/** ショップ画面のクリックを受け取って購入する。画面からアイテムは一切取り出させない。 */
public final class ShopListener implements Listener {

    private final DataStore dataStore;

    public ShopListener(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /** ショップ画面を開く。 */
    public void open(Player player) {
        ShopMenu menu = new ShopMenu();
        menu.render(dataStore.get(player));
        player.openInventory(menu.getInventory());
    }

    /** 開いているショップ画面があれば描き直す（石炭が増えたときなど用）。 */
    public void refresh(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof ShopMenu menu) {
            menu.render(dataStore.get(player));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof ShopMenu menu)) {
            return;
        }
        // 下の自分の持ち物でのクリック（Shift で送る・数字キー・まとめ拾いなど）も、
        // クリエイティブでのクリック（InventoryCreativeEvent も InventoryClickEvent の仲間）も含めてすべて止める
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != top) {
            return;
        }
        if (menu.isRecentlyChanged(event.getRawSlot())) {
            // 中身が変わった直後のクリックは、見ていたものと違うものを買いかねないので無視する
            return;
        }
        if (menu.turnPage(event.getRawSlot())) {
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            menu.render(dataStore.get(player));
            return;
        }
        Optional<Upgrade> upgrade = menu.upgradeAt(event.getRawSlot());
        if (upgrade.isPresent()) {
            ClickType click = event.getClick();
            if (click == ClickType.LEFT || click == ClickType.RIGHT || click.isShiftClick()) {
                buy(player, menu, upgrade.get());
            }
            return;
        }
        Optional<Tool> tool = menu.toolAt(event.getRawSlot());
        if (tool.isEmpty()) {
            return;
        }
        ClickType click = event.getClick();
        if (click.isShiftClick()) {
            buy(player, menu, tool.get(), true);
        } else if (click == ClickType.LEFT || click == ClickType.RIGHT) {
            buy(player, menu, tool.get(), false);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof ShopMenu) {
            event.setCancelled(true);
        }
    }

    private void buy(Player player, ShopMenu menu, Tool tool, boolean bulk) {
        PlayerData data = dataStore.get(player);
        long amount = 1;
        if (bulk) {
            amount = Math.max(1, Purchase.affordableCount(data, tool, ShopMenu.BULK_AMOUNT));
        }
        Purchase.Result result = Purchase.tryBuy(data, tool, amount);
        if (result.success()) {
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            if (result.firstPurchase()) {
                sendExplanation(player, tool);
            }
        } else {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(Component.text("石炭が足りません（あと " + JapaneseFormat.format(result.shortage())
                    + " 個）", NamedTextColor.RED));
        }
        menu.render(data);
    }

    private void buy(Player player, ShopMenu menu, Upgrade upgrade) {
        PlayerData data = dataStore.get(player);
        Purchase.UpgradeResult result = Purchase.tryBuy(data, upgrade);
        if (result.success()) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            if (result.firstPurchase()) {
                sendExplanation(player, upgrade.displayName(), upgrade.explanation(), upgrade.effectDescription());
            }
        } else {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            if (result.shortage().signum() > 0) {
                player.sendMessage(Component.text("石炭が足りません（あと " + JapaneseFormat.format(result.shortage())
                        + " 個）", NamedTextColor.RED));
            }
        }
        menu.render(data);
    }

    /** はじめて買った道具の詳しい解説をチャットに出す。 */
    static void sendExplanation(Player player, Tool tool) {
        sendExplanation(player, tool.displayName(), tool.explanation(),
                "1 つにつき毎秒 +" + JapaneseFormat.format(tool.baseCoalPerSecond()) + " 個（改良・深さの倍率の前）");
    }

    private static void sendExplanation(Player player, String name, String explanation, String effect) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━ ", NamedTextColor.DARK_GRAY)
                .append(Component.text("解説：" + name, NamedTextColor.GOLD))
                .append(Component.text(" ━━", NamedTextColor.DARK_GRAY)));
        player.sendMessage(Component.text(explanation, NamedTextColor.WHITE));
        player.sendMessage(Component.text("効果：" + effect, NamedTextColor.GREEN));
        player.sendMessage(Component.empty());
    }
}
