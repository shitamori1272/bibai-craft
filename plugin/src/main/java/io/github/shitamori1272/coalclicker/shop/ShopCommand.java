package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal shop}：ショップ画面を開く。 */
public final class ShopCommand implements SubCommand {

    private final ShopListener shop;

    public ShopCommand(ShopListener shop) {
        this.shop = shop;
    }

    @Override
    public String path() {
        return "shop";
    }

    @Override
    public String description() {
        return "道具を買うショップを開く";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        shop.open((Player) sender);
    }
}
