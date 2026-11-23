package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.game.PickaxeTier;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

/**
 * 坑内で使うツルハシ（企画書 7 章）。入坑したときに渡し、地上へ戻ると回収する。
 * 手掘りはこのツルハシをメインハンドに持っているときだけ数える。
 *
 * <p>見た目（素材）は買ったツルハシの改良の数で上がる（{@link PickaxeTier}）。
 * 目印として、アイテムの PDC に {@code coalclicker:pickaxe} ＝ 段階 を書いておく。
 * サーバーのメインスレッドから使うこと。
 */
public final class MinerPickaxe {

    /** このプラグインのツルハシの目印（値は段階）。 */
    static final NamespacedKey KEY = new NamespacedKey("coalclicker", "pickaxe");

    private final DataStore dataStore;

    public MinerPickaxe(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /** このプラグインのツルハシか。 */
    public static boolean isPickaxe(ItemStack item) {
        return item != null && !item.isEmpty()
                && item.getPersistentDataContainer().has(KEY, PersistentDataType.INTEGER);
    }

    /** メインハンドにツルハシを持っているか。 */
    public boolean holds(Player player) {
        return isPickaxe(player.getInventory().getItemInMainHand());
    }

    /** 持ち物（手に持っているカーソルの分も含む）のどこかにツルハシがあるか。 */
    public boolean has(Player player) {
        if (isPickaxe(player.getItemOnCursor())) {
            return true;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPickaxe(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ツルハシを渡す。すでに持っていれば今の段階に合わせるだけ。
     * 手が空いていれば手に、ホットバーに空きがあればそこに置いて持ち替える。ホットバーが埋まっていれば
     * 手に持っていたアイテムを空いている枠へ移し、手にツルハシを持たせる。
     *
     * @return 渡せた（または持っていた）ら true。持ち物がいっぱいなら false
     */
    public boolean give(Player player) {
        if (has(player)) {
            refresh(player);
            return true;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack pickaxe = create(tierOf(player));
        int held = inventory.getHeldItemSlot();
        if (isEmpty(inventory.getItem(held))) {
            inventory.setItem(held, pickaxe);
            return true;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (isEmpty(inventory.getItem(slot))) {
                inventory.setItem(slot, pickaxe);
                inventory.setHeldItemSlot(slot);
                return true;
            }
        }
        int free = inventory.firstEmpty();
        if (free < 0) {
            player.sendMessage(Component.text("持ち物がいっぱいでツルハシを渡せません。空きを作ってから切羽を叩いてください。",
                    NamedTextColor.RED));
            return false;
        }
        inventory.setItem(free, inventory.getItem(held));
        inventory.setItem(held, pickaxe);
        return true;
    }

    /**
     * 持っているツルハシを、買った改良に合った段階に作り直す。
     *
     * @return 段階が上がって（変わって）作り直したら、その段階
     */
    public Optional<Integer> refresh(Player player) {
        int tier = tierOf(player);
        boolean changed = false;
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isPickaxe(contents[slot]) && storedTier(contents[slot]) != tier) {
                inventory.setItem(slot, create(tier));
                changed = true;
            }
        }
        ItemStack cursor = player.getItemOnCursor();
        if (isPickaxe(cursor) && storedTier(cursor) != tier) {
            player.setItemOnCursor(create(tier));
            changed = true;
        }
        return changed ? Optional.of(tier) : Optional.empty();
    }

    /** 持ち物とカーソルからツルハシをすべて取り除く。地上へ戻ったとき・ログインしたときに呼ぶ。 */
    public void removeAll(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isPickaxe(contents[slot])) {
                inventory.setItem(slot, null);
            }
        }
        if (isPickaxe(player.getItemOnCursor())) {
            player.setItemOnCursor(null);
        }
    }

    private int tierOf(Player player) {
        return PickaxeTier.of(dataStore.get(player).upgrades());
    }

    private static int storedTier(ItemStack item) {
        Integer tier = item.getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);
        return tier != null ? tier : -1;
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.isEmpty();
    }

    /** 段階 tier のツルハシを作る。 */
    static ItemStack create(int tier) {
        Material material = Optional.ofNullable(Material.matchMaterial(PickaxeTier.material(tier)))
                .orElse(Material.WOODEN_PICKAXE);
        ItemStack item = ItemStack.of(material);
        item.editMeta(meta -> {
            meta.displayName(Component.text("坑夫の" + PickaxeTier.displayName(tier), NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("切羽を叩くと石炭が掘れる", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("ツルハシの改良を買うと素材が上がる", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("地上へ戻ると返却します", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)));
            meta.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, tier);
        });
        // 攻撃力などの表示を隠す
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS));
        return item;
    }
}
