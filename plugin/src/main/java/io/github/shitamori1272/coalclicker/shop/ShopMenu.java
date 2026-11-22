package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * ショップのチェスト画面（6 段）。{@link InventoryHolder} として画面を見分けるのにも使う。
 * マスの割り当ては {@link ShopLayout}、説明文は {@link ShopText}。
 *
 * <p>{@link #render(PlayerData)} で開いたまま中身を書き換える。前と同じ中身のマスは置き直さないので、
 * 毎秒描き直してもちらつかない。
 */
public final class ShopMenu implements InventoryHolder {

    public static final Component TITLE = Component.text("石炭クリッカー ショップ");
    /** まとめ買い（Shift+クリック）の最大個数。 */
    public static final int BULK_AMOUNT = 10;

    private final Inventory inventory;
    /** 改良のページ（0 始まり）。 */
    private int page;
    /** 最後に描いたときの、改良のマスの中身（ページの中の順）。 */
    private final List<Upgrade> shownUpgrades = new ArrayList<>();
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public ShopMenu() {
        inventory = Bukkit.createInventory(this, ShopLayout.SIZE, TITLE);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /** そのマスにある道具。道具のマスでなければ空。 */
    public Optional<Tool> toolAt(int slot) {
        int index = ShopLayout.toolIndexAt(slot);
        Tool[] tools = Tool.values();
        return index >= 0 && index < tools.length ? Optional.of(tools[index]) : Optional.empty();
    }

    /** そのマスにある改良。改良のマスでなければ空。 */
    public Optional<Upgrade> upgradeAt(int slot) {
        int index = ShopLayout.upgradeIndexAt(slot);
        return index >= 0 && index < shownUpgrades.size() ? Optional.of(shownUpgrades.get(index)) : Optional.empty();
    }

    /** 前・次のページのボタンなら、ページを動かして true。 */
    public boolean turnPage(int slot) {
        if (slot == ShopLayout.PREVIOUS_PAGE_SLOT && hasPreviousPage) {
            page--;
            return true;
        }
        if (slot == ShopLayout.NEXT_PAGE_SLOT && hasNextPage) {
            page++;
            return true;
        }
        return false;
    }

    /** プレイヤーの今のデータで中身を描き直す。 */
    public void render(PlayerData data) {
        ItemStack[] items = new ItemStack[ShopLayout.SIZE];
        ItemStack filler = filler();
        for (int i = 0; i < items.length; i++) {
            items[i] = filler;
        }

        BigNum coal = data.coal();
        BigNum rate = Formulas.coalPerSecond(data);
        items[ShopLayout.COAL_SLOT] = icon(Material.COAL, text("石炭の残高", NamedTextColor.GOLD),
                List.of(text(JapaneseFormat.format(coal) + " 個", NamedTextColor.WHITE)));
        items[ShopLayout.RATE_SLOT] = icon(Material.HOPPER, text("毎秒の石炭", NamedTextColor.AQUA),
                List.of(text(JapaneseFormat.format(rate) + " 個", NamedTextColor.WHITE)));
        items[ShopLayout.CLICK_SLOT] = icon(Material.WOODEN_PICKAXE, text("手掘り 1 回の石炭", NamedTextColor.AQUA),
                List.of(text(JapaneseFormat.format(Formulas.clickCoal(data.upgrades(), rate)) + " 個",
                        NamedTextColor.WHITE)));

        Tool[] tools = Tool.values();
        for (int i = 0; i < ShopLayout.toolSlotCount() && i < tools.length; i++) {
            items[ShopLayout.toolSlot(i)] = toolIcon(tools[i], data, coal);
        }

        List<Upgrade> available = ShopText.availableByPrice(data);
        page = ShopLayout.clampPage(page, available.size());
        hasPreviousPage = ShopLayout.hasPreviousPage(page, available.size());
        hasNextPage = ShopLayout.hasNextPage(page, available.size());
        items[ShopLayout.UPGRADE_HEADER_SLOT] = icon(Material.ENCHANTED_BOOK, text("改良", NamedTextColor.LIGHT_PURPLE),
                components(ShopText.upgradeHeaderLore(available.size(), page)));
        shownUpgrades.clear();
        shownUpgrades.addAll(ShopLayout.pageOf(available, page));
        for (int i = 0; i < shownUpgrades.size(); i++) {
            items[ShopLayout.upgradeSlot(i)] = upgradeIcon(shownUpgrades.get(i), coal);
        }
        for (int i = shownUpgrades.size(); i < ShopLayout.UPGRADES_PER_PAGE; i++) {
            items[ShopLayout.upgradeSlot(i)] = null;
        }

        int pages = ShopLayout.pageCount(available.size());
        if (hasPreviousPage) {
            items[ShopLayout.PREVIOUS_PAGE_SLOT] = icon(Material.ARROW, text("前のページ", NamedTextColor.YELLOW),
                    List.of(text((page + 1) + " / " + pages, NamedTextColor.GRAY)));
        }
        if (hasNextPage) {
            items[ShopLayout.NEXT_PAGE_SLOT] = icon(Material.ARROW, text("次のページ", NamedTextColor.YELLOW),
                    List.of(text((page + 1) + " / " + pages, NamedTextColor.GRAY)));
        }
        ShopText.nextLockedHint(data).ifPresent(hint -> items[ShopLayout.LOCKED_HINT_SLOT] = icon(Material.GRAY_DYE,
                text("次の改良", NamedTextColor.DARK_GRAY), List.of(text(hint, NamedTextColor.GRAY))));

        // 中身が変わったマスだけ置き直す（毎秒の描き直しでちらつかないように）
        for (int i = 0; i < items.length; i++) {
            if (!Objects.equals(normalize(inventory.getItem(i)), items[i])) {
                inventory.setItem(i, items[i]);
            }
        }
    }

    private static ItemStack normalize(ItemStack item) {
        return item == null || item.isEmpty() ? null : item;
    }

    private static ItemStack toolIcon(Tool tool, PlayerData data, BigNum coal) {
        long owned = data.getToolCount(tool.id());
        ItemStack item = icon(material(tool.iconMaterial()), text(tool.displayName(), NamedTextColor.GOLD),
                components(ShopText.toolLore(tool, data, coal)));
        item.setAmount((int) Math.clamp(owned, 1, item.getMaxStackSize()));
        return item;
    }

    private static ItemStack upgradeIcon(Upgrade upgrade, BigNum coal) {
        String icon = upgrade.tool().map(Tool::iconMaterial)
                .orElse(upgrade.kind() == Upgrade.Kind.PICKAXE ? "DIAMOND_PICKAXE" : "EXPERIENCE_BOTTLE");
        return icon(material(icon), text(upgrade.displayName(), NamedTextColor.LIGHT_PURPLE),
                components(ShopText.upgradeLore(upgrade, coal)));
    }

    private static List<Component> components(List<ShopText.Line> lines) {
        List<Component> lore = new ArrayList<>(lines.size());
        for (ShopText.Line line : lines) {
            Component component = Component.empty().decoration(TextDecoration.ITALIC, false);
            for (ShopText.Segment segment : line.segments()) {
                component = component.append(Component.text(segment.text(), color(segment.tone())));
            }
            lore.add(component);
        }
        return lore;
    }

    private static NamedTextColor color(ShopText.Tone tone) {
        return switch (tone) {
            case LABEL, DESCRIPTION -> NamedTextColor.GRAY;
            case VALUE -> NamedTextColor.WHITE;
            case AFFORDABLE, HINT -> NamedTextColor.YELLOW;
            case UNAFFORDABLE -> NamedTextColor.RED;
            case GAIN -> NamedTextColor.GREEN;
            case MUTED -> NamedTextColor.DARK_GRAY;
        };
    }

    private static Material material(String name) {
        return Optional.ofNullable(Material.matchMaterial(name)).orElse(Material.BARRIER);
    }

    private static ItemStack icon(Material material, Component name, List<Component> lore) {
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(lore);
        });
        // ツルハシの攻撃力などの表示を隠す
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS));
        return item;
    }

    private static ItemStack filler() {
        ItemStack item = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true));
        return item;
    }

    /** 斜体を切った文字。説明文は既定で斜体になるため。 */
    private static Component text(String content, NamedTextColor color) {
        return Component.text(content, color).decoration(TextDecoration.ITALIC, false);
    }
}
