package io.github.shitamori1272.coalclicker.admin;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.mine.MineService;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /coal admin reset <player> confirm}。オンラインのプレイヤーのデータを初期化する（区画は維持）。
 * {@code confirm} を付けないと確認メッセージだけ出す。坑内にいる人は、先に地上へ戻してから初期化する
 * （描かれている坑道とデータが食い違わないように）。地上へ戻せなければ初期化しない。
 */
public final class ResetCommand implements SubCommand {

    private static final String CONFIRM = "confirm";

    private final DataStore dataStore;
    private final MineService mineService;

    public ResetCommand(DataStore dataStore, MineService mineService) {
        this.dataStore = dataStore;
        this.mineService = mineService;
    }

    @Override
    public String path() {
        return "admin reset";
    }

    @Override
    public String description() {
        return "データを初期化（区画は維持）";
    }

    @Override
    public String usage() {
        return "<player> confirm";
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
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(Component.text("使い方: /coal admin reset <player> confirm", NamedTextColor.RED));
            return;
        }
        Player target = OnlinePlayers.find(sender, args[0]);
        if (target == null) {
            return;
        }
        if (args.length == 1 || !args[1].equalsIgnoreCase(CONFIRM)) {
            sender.sendMessage(Component.text(target.getName() + " の石炭・累計採掘量・道具・改良・手掘りの回数・坑道の進み具合をすべて消します"
                    + "（区画は残します）。"
                    + "よければ /coal admin reset " + target.getName() + " confirm を実行してください。",
                    NamedTextColor.YELLOW));
            return;
        }
        // 坑内で消すと、描かれている坑道とデータが食い違うので、先に地上へ戻す
        boolean wasInMine = mineService.inMine(target);
        if (!mineService.returnToSurface(target)) {
            sender.sendMessage(Component.text(target.getName() + " を地上へ戻せなかったため、初期化をやめました。",
                    NamedTextColor.RED));
            return;
        }
        if (wasInMine) {
            target.sendMessage(Component.text("データの初期化のため、地上へ戻しました。", NamedTextColor.YELLOW));
        }
        AdminActions.reset(dataStore.get(target));
        sender.sendMessage(Component.text(target.getName() + " のデータを初期化しました（区画は維持）。", NamedTextColor.GREEN));
        if (target != sender) {
            target.sendMessage(Component.text("管理者があなたのデータを初期化しました。", NamedTextColor.YELLOW));
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return OnlinePlayers.suggest(args[0]);
        }
        if (args.length == 2 && CONFIRM.startsWith(args[1].toLowerCase(Locale.ROOT))) {
            return List.of(CONFIRM);
        }
        return List.of();
    }
}
