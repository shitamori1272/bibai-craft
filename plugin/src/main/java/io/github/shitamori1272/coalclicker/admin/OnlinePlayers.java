package io.github.shitamori1272.coalclicker.admin;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** 管理コマンドで使う、オンラインのプレイヤーの検索と補完。 */
final class OnlinePlayers {

    private OnlinePlayers() {
    }

    /** 名前の前方一致で補完候補を出す。 */
    static List<String> suggest(String partial) {
        String lower = partial.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lower))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /** 名前の完全一致でオンラインのプレイヤーを探す。見つからなければ sender に伝えて null を返す。 */
    static Player find(CommandSender sender, String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            sender.sendMessage(Component.text(
                    name + " はオンラインではありません（オンラインのプレイヤーだけ指定できます）。",
                    NamedTextColor.RED));
        }
        return player;
    }
}
