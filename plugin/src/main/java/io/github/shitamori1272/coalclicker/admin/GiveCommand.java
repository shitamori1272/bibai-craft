package io.github.shitamori1272.coalclicker.admin;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal admin give <player> <量>}。オンラインのプレイヤーに石炭を付与する（デモ用）。 */
public final class GiveCommand implements SubCommand {

    private final DataStore dataStore;

    public GiveCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String path() {
        return "admin give";
    }

    @Override
    public String description() {
        return "石炭を付与（デモ用・累計採掘量には数えない）";
    }

    @Override
    public String usage() {
        return "<player> <量>";
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("使い方: /coal admin give <player> <量>（例: 1e100）", NamedTextColor.RED));
            return;
        }
        Optional<BigNum> amount = AdminActions.parseAmount(args[1]);
        if (amount.isEmpty()) {
            sender.sendMessage(Component.text("量は 0 より大きい数で指定してください（例: 1000・1e100）: " + args[1],
                    NamedTextColor.RED));
            return;
        }
        Player target = OnlinePlayers.find(sender, args[0]);
        if (target == null) {
            return;
        }
        Optional<BigNum> after = AdminActions.give(dataStore.get(target), amount.get());
        if (after.isEmpty()) {
            sender.sendMessage(Component.text("付与後の石炭が大きすぎて扱えません。量を減らしてください。", NamedTextColor.RED));
            return;
        }
        String given = JapaneseFormat.format(amount.get());
        sender.sendMessage(Component.text(target.getName() + " に石炭 " + given + "個を付与しました（現在 "
                + JapaneseFormat.format(after.get()) + "個）。", NamedTextColor.GREEN));
        if (target != sender) {
            target.sendMessage(Component.text("管理者から石炭 " + given + "個を受け取りました。", NamedTextColor.GREEN));
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        return args.length == 1 ? OnlinePlayers.suggest(args[0]) : List.of();
    }
}
