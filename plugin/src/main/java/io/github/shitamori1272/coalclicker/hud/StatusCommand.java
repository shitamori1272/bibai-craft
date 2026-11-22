package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import io.github.shitamori1272.coalclicker.data.DataStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** 引数なしの {@code /coal}。自分の記録をチャットに出す。 */
public final class StatusCommand implements SubCommand {

    private final DataStore dataStore;

    public StatusCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String path() {
        return "";
    }

    @Override
    public String description() {
        return "自分の記録を表示";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.sendMessage(Component.text("── " + player.getName() + " の記録 ──", NamedTextColor.GOLD));
        for (HudText.Entry entry : HudText.record(dataStore.get(player))) {
            player.sendMessage(Component.text(entry.label() + "：", NamedTextColor.GRAY)
                    .append(Component.text(entry.value(), NamedTextColor.WHITE)));
        }
    }
}
