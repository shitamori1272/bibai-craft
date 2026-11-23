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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
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
    /** 開いている間は動かさない改良の並び。 */
    private final UpgradeOrder order = new UpgradeOrder();
    /** 次に描くときに並びを作り直すか（開いたとき・ページを変えたとき）。 */
    private boolean resetOrder = true;
    /** 最後に描いたときの、改良のマスの中身（ページの中の順）。 */
    private final List<UpgradeOrder.Cell> shownUpgrades = new ArrayList<>();
    /** 中身が変わったばかりのマスのクリックを止める。 */
    private final SlotGuard guard = new SlotGuard(ShopLayout.SIZE);
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

    /** そのマスにある、買える改良。改良のマスでない・購入済みなら空。 */
    public Optional<Upgrade> upgradeAt(int slot) {
        int index = ShopLayout.upgradeIndexAt(slot);
        if (index < 0 || index >= shownUpgrades.size()) {
            return Optional.empty();
        }
        UpgradeOrder.Cell cell = shownUpgrades.get(index);
        return cell.status() == UpgradeOrder.Status.AVAILABLE ? Optional.of(cell.upgrade()) : Optional.empty();
    }

    /** そのマスの中身が別のものに変わったばかりで、クリックを受け付けないか。 */
    public boolean isRecentlyChanged(int slot) {
        return guard.isGuarded(slot, System.currentTimeMillis());
    }

    /** 前・次のページのボタンなら、ページを動かして true。 */
    public boolean turnPage(int slot) {
        if (slot == ShopLayout.PREVIOUS_PAGE_SLOT && hasPreviousPage) {
            page--;
            resetOrder = true;
            return true;
        }
        if (slot == ShopLayout.NEXT_PAGE_SLOT && hasNextPage) {
            page++;
            resetOrder = true;
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

        String[] keys = new String[ShopLayout.SIZE];
        for (int i = 0; i < ShopLayout.toolSlotCount() && i < tools.length; i++) {
            keys[ShopLayout.toolSlot(i)] = "tool:" + tools[i].id();
        }

        List<Upgrade> available = ShopText.availableByPrice(data);
        if (resetOrder) {
            order.reset(available);
            resetOrder = false;
        } else {
            // 開いている間は並びを動かさず、新しく出た改良だけ最後に足す
            order.update(available);
        }
        int slotsUsed = order.size();
        page = ShopLayout.clampPage(page, slotsUsed);
        hasPreviousPage = ShopLayout.hasPreviousPage(page, slotsUsed);
        hasNextPage = ShopLayout.hasNextPage(page, slotsUsed);
        items[ShopLayout.UPGRADE_HEADER_SLOT] = icon(Material.ENCHANTED_BOOK, text("改良", NamedTextColor.LIGHT_PURPLE),
                components(ShopText.upgradeHeaderLore(available.size(), page, slotsUsed)));
        shownUpgrades.clear();
        shownUpgrades.addAll(order.page(page, data));
        for (int i = 0; i < ShopLayout.UPGRADES_PER_PAGE; i++) {
            int slot = ShopLayout.upgradeSlot(i);
            if (i >= shownUpgrades.size()) {
                items[slot] = null;
                continue;
            }
            UpgradeOrder.Cell cell = shownUpgrades.get(i);
            switch (cell.status()) {
                case AVAILABLE -> {
                    items[slot] = upgradeIcon(cell.upgrade(), coal);
                    keys[slot] = "upgrade:" + cell.upgrade().id();
                }
                case PURCHASED -> {
                    items[slot] = icon(Material.LIME_STAINED_GLASS_PANE, text("購入済み", NamedTextColor.GREEN),
                            List.of(text(cell.upgrade().displayName(), NamedTextColor.GRAY)));
                    keys[slot] = "sold:" + cell.upgrade().id();
                }
                case GONE -> items[slot] = null;
            }
        }

        int pages = ShopLayout.pageCount(slotsUsed);
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

        if (hasPreviousPage) {
            keys[ShopLayout.PREVIOUS_PAGE_SLOT] = "previous";
        }
        if (hasNextPage) {
            keys[ShopLayout.NEXT_PAGE_SLOT] = "next";
        }
        long now = System.currentTimeMillis();
        // 中身が変わったマスだけ置き直す（毎秒の描き直しでちらつかないように）
        for (int i = 0; i < items.length; i++) {
            guard.record(i, keys[i], now);
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
                component = component.append(segment.tone() == ShopText.Tone.PHOTO
                        ? photo(segment.text())
                        : Component.text(segment.text(), color(segment.tone())));
            }
            lore.add(component);
        }
        return lore;
    }

    /** 写真の文字。白にしないと写真に色が掛かり、影を消さないと写真の形の黒い影が出る。 */
    private static Component photo(String glyph) {
        return Component.text(glyph, NamedTextColor.WHITE)
                .font(Key.key(ToolPhoto.FONT_NAMESPACE, ToolPhoto.FONT_KEY))
                .shadowColor(ShadowColor.none());
    }

    private static NamedTextColor color(ShopText.Tone tone) {
        return switch (tone) {
            case LABEL, DESCRIPTION -> NamedTextColor.GRAY;
            case VALUE, PHOTO -> NamedTextColor.WHITE;
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
