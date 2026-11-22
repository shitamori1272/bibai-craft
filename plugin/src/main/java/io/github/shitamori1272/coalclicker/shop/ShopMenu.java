package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
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
 * <p>1 段目に掘進力・石炭・採炭力、2 段目に道具を並べる。{@link #render(PlayerData)} で開いたまま中身を書き換える。
 */
public final class ShopMenu implements InventoryHolder {

    public static final Component TITLE = Component.text("石炭クリッカー ショップ");
    /** まとめ買い（Shift+クリック）の最大個数。 */
    public static final int BULK_AMOUNT = 10;

    private static final int SIZE = 27;
    private static final int DIG_POWER_SLOT = 2;
    private static final int COAL_SLOT = 4;
    private static final int COAL_POWER_SLOT = 6;
    /** 道具を置く場所。{@link Tool#values()} の順。 */
    private static final int[] TOOL_SLOTS = {10, 11, 12, 14, 15, 16};

    private final Inventory inventory;

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

    /** プレイヤーの今のデータで中身を描き直す。 */
    public void render(PlayerData data) {
        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        BigNum digPower = Formulas.digPower(data.tools());
        BigNum coalPower = Formulas.coalPower(data.tools());
        double speed = Formulas.digSpeed(digPower);

        inventory.setItem(DIG_POWER_SLOT, icon(Material.RAIL, text("掘進力", NamedTextColor.AQUA), List.of(
                line("今の値：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(digPower), NamedTextColor.WHITE)),
                line("掘進速度：", NamedTextColor.GRAY)
                        .append(text(JapaneseFormat.format(BigNum.of(speed)) + " m/秒", NamedTextColor.WHITE)),
                Component.empty(),
                line("坑道を自動で掘り進める速さ。", NamedTextColor.GRAY),
                line("上げると自動で石炭が集まるようになる", NamedTextColor.GRAY))));
        inventory.setItem(COAL_SLOT, icon(Material.COAL, text("持っている石炭", NamedTextColor.GOLD), List.of(
                line(JapaneseFormat.format(data.coal()) + " 個", NamedTextColor.WHITE))));
        inventory.setItem(COAL_POWER_SLOT, icon(Material.COAL_ORE, text("採炭力", NamedTextColor.AQUA), List.of(
                line("今の値：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(coalPower), NamedTextColor.WHITE)),
                Component.empty(),
                line("1m 掘ったときに採れる石炭の量。", NamedTextColor.GRAY),
                line("手掘りにも自動にも効く", NamedTextColor.GRAY))));

        Tool[] tools = Tool.values();
        for (int i = 0; i < TOOL_SLOTS.length && i < tools.length; i++) {
            inventory.setItem(TOOL_SLOTS[i], toolIcon(tools[i], data));
        }
    }

    private static ItemStack toolIcon(Tool tool, PlayerData data) {
        long owned = data.getToolCount(tool.id());
        BigNum price = Formulas.price(tool, owned);
        boolean affordable = data.coal().isGreaterThanOrEqual(price);
        List<Component> lore = new ArrayList<>();
        lore.add(line("種類：" + tool.kind().displayName(), NamedTextColor.DARK_GRAY));
        lore.add(line("価格：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(price) + " 個",
                affordable ? NamedTextColor.YELLOW : NamedTextColor.RED)));
        lore.add(line("所持数：", NamedTextColor.GRAY).append(text(JapaneseFormat.format(BigNum.of(owned)),
                NamedTextColor.WHITE)));
        lore.add(line("効果：", NamedTextColor.GRAY).append(text(tool.effectDescription(), NamedTextColor.GREEN)));
        lore.add(Component.empty());
        lore.add(line(tool.shortDescription(), NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(line("クリックで購入", NamedTextColor.YELLOW));
        lore.add(line("Shift+クリックでまとめて購入（最大 " + BULK_AMOUNT + " 個）", NamedTextColor.DARK_GRAY));
        Material material = Optional.ofNullable(Material.matchMaterial(tool.iconMaterial())).orElse(Material.BARRIER);
        ItemStack item = icon(material, text(tool.displayName(), NamedTextColor.GOLD), lore);
        item.setAmount((int) Math.clamp(owned, 1, item.getMaxStackSize()));
        return item;
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
