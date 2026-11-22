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
 * ショップのチェスト画面（3 段）。{@link InventoryHolder} として画面を見分けるのにも使う。
 *
 * <p>1 段目に石炭・毎秒の石炭・手掘り 1 回の石炭、2 段目に道具 8 種類、3 段目に買える改良（先頭 9 個）を並べる。
 * {@link #render(PlayerData)} で開いたまま中身を書き換える。
 *
 * <p>TODO(#62): 新しいルールに合わせた最小限の書き直し。説明文（全体の何%など）・並べ方は #62 で作り直す。
 */
public final class ShopMenu implements InventoryHolder {

    public static final Component TITLE = Component.text("石炭クリッカー ショップ");
    /** まとめ買い（Shift+クリック）の最大個数。 */
    public static final int BULK_AMOUNT = 10;

    private static final int SIZE = 27;
    private static final int COAL_SLOT = 2;
    private static final int RATE_SLOT = 4;
    private static final int CLICK_SLOT = 6;
    /** 道具を置く場所。{@link Tool#values()} の順。 */
    private static final int[] TOOL_SLOTS = {9, 10, 11, 12, 14, 15, 16, 17};
    /** 改良を置く場所。{@link Upgrade#available} の順。 */
    private static final int[] UPGRADE_SLOTS = {18, 19, 20, 21, 22, 23, 24, 25, 26};

    private final Inventory inventory;
    /** 最後に描いたときの、改良のマスの中身。 */
    private final List<Upgrade> shownUpgrades = new ArrayList<>();

    public ShopMenu() {
        inventory = Bukkit.createInventory(this, SIZE, TITLE);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /** そのマスにある道具。道具のマスでなければ空。 */
    public Optional<Tool> toolAt(int slot) {
        Tool[] tools = Tool.values();
        for (int i = 0; i < TOOL_SLOTS.length && i < tools.length; i++) {
            if (TOOL_SLOTS[i] == slot) {
                return Optional.of(tools[i]);
            }
        }
        return Optional.empty();
    }

    /** そのマスにある改良。改良のマスでなければ空。 */
    public Optional<Upgrade> upgradeAt(int slot) {
        for (int i = 0; i < UPGRADE_SLOTS.length && i < shownUpgrades.size(); i++) {
            if (UPGRADE_SLOTS[i] == slot) {
                return Optional.of(shownUpgrades.get(i));
            }
        }
        return Optional.empty();
    }

    /** プレイヤーの今のデータで中身を描き直す。 */
    public void render(PlayerData data) {
        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        BigNum rate = Formulas.coalPerSecond(data);
        inventory.setItem(COAL_SLOT, icon(Material.COAL, text("持っている石炭", NamedTextColor.GOLD), List.of(
                line(JapaneseFormat.format(data.coal()) + " 個", NamedTextColor.WHITE))));
        inventory.setItem(RATE_SLOT, icon(Material.HOPPER, text("毎秒の石炭", NamedTextColor.AQUA), List.of(
                line(JapaneseFormat.format(rate) + " 個", NamedTextColor.WHITE))));
        inventory.setItem(CLICK_SLOT, icon(Material.WOODEN_PICKAXE, text("手掘り 1 回の石炭", NamedTextColor.AQUA),
                List.of(line(JapaneseFormat.format(Formulas.clickCoal(data.upgrades(), rate)) + " 個",
                        NamedTextColor.WHITE))));

        long level = Formulas.levelOf(data.totalMined());
        Tool[] tools = Tool.values();
        for (int i = 0; i < TOOL_SLOTS.length && i < tools.length; i++) {
            inventory.setItem(TOOL_SLOTS[i], toolIcon(tools[i], data, level));
        }
        shownUpgrades.clear();
        List<Upgrade> available = Upgrade.available(data);
        for (int i = 0; i < UPGRADE_SLOTS.length && i < available.size(); i++) {
            shownUpgrades.add(available.get(i));
            inventory.setItem(UPGRADE_SLOTS[i], upgradeIcon(available.get(i), data));
        }
    }

    private static ItemStack toolIcon(Tool tool, PlayerData data, long level) {
        long owned = data.getToolCount(tool.id());
        BigNum price = Formulas.price(tool, owned);
        boolean affordable = data.coal().isGreaterThanOrEqual(price);
        List<Component> lore = new ArrayList<>();
        lore.add(line("価格：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(price) + " 個",
                affordable ? NamedTextColor.YELLOW : NamedTextColor.RED)));
        lore.add(line("所持数：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(BigNum.of(owned)),
                NamedTextColor.WHITE)));
        lore.add(line("1 つ買うと毎秒 +", NamedTextColor.GRAY).append(text(
                JapaneseFormat.format(Formulas.marginalCoalPerSecond(tool, data.upgrades(), level)) + " 個",
                NamedTextColor.GREEN)));
        lore.add(line("この道具の合計：毎秒 ", NamedTextColor.GRAY).append(text(
                JapaneseFormat.format(Formulas.toolCoalPerSecond(tool, data.tools(), data.upgrades(), level)) + " 個",
                NamedTextColor.WHITE)));
        lore.add(Component.empty());
        lore.add(line(tool.shortDescription(), NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(line("クリックで購入", NamedTextColor.YELLOW));
        lore.add(line("Shift+クリックでまとめて購入（最大 " + BULK_AMOUNT + " 個）", NamedTextColor.DARK_GRAY));
        ItemStack item = icon(material(tool.iconMaterial()), text(tool.displayName(), NamedTextColor.GOLD), lore);
        item.setAmount((int) Math.clamp(owned, 1, item.getMaxStackSize()));
        return item;
    }

    private static ItemStack upgradeIcon(Upgrade upgrade, PlayerData data) {
        boolean affordable = data.coal().isGreaterThanOrEqual(upgrade.price());
        List<Component> lore = new ArrayList<>();
        lore.add(line("価格：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(upgrade.price()) + " 個",
                affordable ? NamedTextColor.YELLOW : NamedTextColor.RED)));
        lore.add(line("効果：", NamedTextColor.GRAY).append(text(upgrade.effectDescription(), NamedTextColor.GREEN)));
        lore.add(Component.empty());
        lore.add(line("クリックで購入", NamedTextColor.YELLOW));
        String icon = upgrade.tool().map(Tool::iconMaterial)
                .orElse(upgrade.kind() == Upgrade.Kind.PICKAXE ? "DIAMOND_PICKAXE" : "EXPERIENCE_BOTTLE");
        return icon(material(icon), text(upgrade.displayName(), NamedTextColor.LIGHT_PURPLE), lore);
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

    private static Component text(String content, NamedTextColor color) {
        return Component.text(content, color).decoration(TextDecoration.ITALIC, false);
    }

    /** 説明文の 1 行。斜体を切っておくと、後ろに append した文字にも効く。 */
    private static Component line(String content, NamedTextColor color) {
        return text(content, color);
    }
}
