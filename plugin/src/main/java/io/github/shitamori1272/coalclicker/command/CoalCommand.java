package io.github.shitamori1272.coalclicker.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal} コマンド。引数の先頭からサブコマンドを探して実行する。 */
public final class CoalCommand implements BasicCommand {

    public static final String ADMIN_PERMISSION = "coalclicker.admin";

    private final Map<String, SubCommand> commands = new LinkedHashMap<>();

    public void register(SubCommand command) {
        String path = normalize(command.path());
        if (commands.putIfAbsent(path, command) != null) {
            throw new IllegalArgumentException("サブコマンドが重複しています: " + path);
        }
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        Match match = find(args);
        // 引数なしの /coal 用のサブコマンドに、知らない引数が付いていたらヘルプにする
        if (match == null || (match.command().path().isEmpty() && match.rest().length > 0)) {
            sendHelp(sender);
            return;
        }
        SubCommand command = match.command();
        if (command.adminOnly() && !isAdmin(sender)) {
            sender.sendMessage(Component.text("このコマンドを使う権限がありません。", NamedTextColor.RED));
            return;
        }
        if (command.playerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーだけが使えます。", NamedTextColor.RED));
            return;
        }
        command.execute(sender, match.rest());
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        String[] typed = args.length == 0 ? new String[] {""} : args;
        List<String> result = new ArrayList<>();
        // 入力中の語に続くサブコマンド名の候補
        String prefix = String.join(" ", Arrays.copyOf(typed, typed.length - 1));
        String partial = typed[typed.length - 1].toLowerCase(Locale.ROOT);
        for (Map.Entry<String, SubCommand> entry : commands.entrySet()) {
            if (entry.getValue().adminOnly() && !isAdmin(sender)) {
                continue;
            }
            String[] words = entry.getKey().isEmpty() ? new String[0] : entry.getKey().split(" ");
            if (words.length < typed.length) {
                continue;
            }
            String head = String.join(" ", Arrays.copyOf(words, typed.length - 1));
            String next = words[typed.length - 1];
            if (head.equals(prefix) && next.startsWith(partial) && !result.contains(next)) {
                result.add(next);
            }
        }
        // サブコマンドが確定していれば、その引数の候補
        Match match = find(Arrays.copyOf(typed, typed.length - 1));
        if (match != null && !match.command().path().isEmpty()
                && (!match.command().adminOnly() || isAdmin(sender))) {
            String[] rest = Arrays.copyOfRange(typed, typed.length - 1 - match.rest().length, typed.length);
            result.addAll(match.command().suggest(sender, rest));
        }
        return result;
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    private Match find(String[] args) {
        // 長い名前から順に一致を探す（"admin give" を "admin" より優先）
        for (int length = Math.min(args.length, 3); length >= 0; length--) {
            String path = normalize(String.join(" ", Arrays.copyOf(args, length)));
            SubCommand command = commands.get(path);
            if (command != null) {
                return new Match(command, Arrays.copyOfRange(args, length, args.length));
            }
        }
        return null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("石炭クリッカーのコマンド", NamedTextColor.GOLD));
        for (SubCommand command : commands.values()) {
            if (command.adminOnly() && !isAdmin(sender)) {
                continue;
            }
            String line = ("/coal " + command.path() + " " + command.usage()).replaceAll(" +", " ").trim();
            sender.sendMessage(Component.text(line, NamedTextColor.YELLOW)
                    .append(Component.text(" - " + command.description(), NamedTextColor.GRAY)));
        }
    }

    static boolean isAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission(ADMIN_PERMISSION);
    }

    private static String normalize(String path) {
        return path.trim().replaceAll(" +", " ").toLowerCase(Locale.ROOT);
    }

    private record Match(SubCommand command, String[] rest) {
    }
}
