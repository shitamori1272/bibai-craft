package io.github.shitamori1272.coalclicker.trophy;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * トロフィーの一覧のチェスト画面（6 段）。{@link InventoryHolder} として画面を見分けるのにも使う。
 * マスの割り当てと文言は {@link TrophyBook}。見るだけの画面で、アイテムは取り出させない（{@link TrophyListener}）。
 */
public final class TrophyMenu implements InventoryHolder {

    public static final Component TITLE = Component.text("トロフィー");

    private final Inventory inventory;
    private int page;

    public TrophyMenu() {
        inventory = Bukkit.createInventory(this, TrophyBook.SIZE, TITLE);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /** 前・次のページのボタンなら、ページを動かして true。 */
    public boolean turnPage(int slot) {
        if (slot == TrophyBook.PREVIOUS_PAGE_SLOT && page > 0) {
            page--;
            return true;
        }
        if (slot == TrophyBook.NEXT_PAGE_SLOT && page < TrophyBook.pageCount() - 1) {
            page++;
            return true;
        }
        return false;
    }

    /** プレイヤーの今のデータで中身を描き直す。 */
    public void render(PlayerData data) {
        ItemStack[] items = new ItemStack[TrophyBook.SIZE];
        List<TrophyBook.Entry> entries = TrophyBook.entries(page, data.upgrades(), data.totalMined());
        for (int slot = 0; slot < entries.size(); slot++) {
            items[slot] = trophyIcon(entries.get(slot));
        }
        ItemStack filler = filler();
        for (int slot = TrophyBook.PER_PAGE; slot < TrophyBook.SIZE; slot++) {
            items[slot] = filler;
        }
        items[TrophyBook.SUMMARY_SLOT] = icon(Material.GOLD_INGOT,
                text(TrophyBook.summary(data.upgrades()), NamedTextColor.GOLD),
                List.of(text("累計採掘量の桁が 1 つ増えるたびに 1 つもらえる", NamedTextColor.GRAY),
                        text("1 つにつき毎秒の石炭と手掘りが +5%", NamedTextColor.GRAY)));
        if (page > 0) {
            items[TrophyBook.PREVIOUS_PAGE_SLOT] = icon(Material.ARROW, text("前のページ", NamedTextColor.YELLOW),
                    List.of());
        }
        if (page < TrophyBook.pageCount() - 1) {
            items[TrophyBook.NEXT_PAGE_SLOT] = icon(Material.ARROW, text("次のページ", NamedTextColor.YELLOW),
                    List.of());
        }
        inventory.setContents(items);
    }

    private static ItemStack trophyIcon(TrophyBook.Entry entry) {
        return switch (entry.state()) {
            case EARNED -> icon(Material.GOLD_BLOCK, text(entry.name(), NamedTextColor.GOLD),
                    lore(entry.lore(), NamedTextColor.YELLOW));
            case NEXT -> icon(Material.GRAY_DYE, text(entry.name(), NamedTextColor.WHITE),
                    lore(entry.lore(), NamedTextColor.YELLOW));
            case LOCKED -> icon(Material.GRAY_STAINED_GLASS, text(entry.name(), NamedTextColor.GRAY),
                    lore(entry.lore(), NamedTextColor.DARK_GRAY));
        };
    }

    private static List<Component> lore(List<String> lines, NamedTextColor color) {
        List<Component> list = new ArrayList<>(lines.size());
        for (String line : lines) {
            list.add(text(line, color));
        }
        return list;
    }

    private static ItemStack icon(Material material, Component name, List<Component> lore) {
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(lore);
        });
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS));
        return item;
    }

    private static ItemStack filler() {
        ItemStack item = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE);
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true));
        return item;
    }

    /** 斜体を切った文字。説明文は既定で斜体になるため。 */
    private static Component text(String content, NamedTextColor color) {
        return Component.text(content, color).decoration(TextDecoration.ITALIC, false);
    }
}
