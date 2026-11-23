package io.github.shitamori1272.coalclicker.ranking;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal top [mined|depth|rate]}。上位 10 人と、プレイヤーなら自分の順位を出す。 */
public final class TopCommand implements SubCommand {

    static final int LIMIT = 10;

    private final Leaderboard leaderboard;

    public TopCommand(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public String path() {
        return "top";
    }

    @Override
    public String description() {
        return "ランキング（累計採掘量・到達深さ・毎秒採掘量）";
    }

    @Override
    public String usage() {
        return "[mined|depth|rate]";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Optional<Category> category = args.length == 0 ? Optional.of(Category.MINED) : Category.byId(args[0]);
        if (category.isEmpty() || args.length > 1) {
            sender.sendMessage(Component.text("使い方: /coal top " + usage(), NamedTextColor.RED));
            return;
        }
        Ranking ranking = leaderboard.ranking(category.get());
        sender.sendMessage(Component.text("── ランキング：" + ranking.category().displayName() + " ──",
                NamedTextColor.GOLD));
        if (ranking.size() == 0) {
            sender.sendMessage(Component.text("まだ記録がありません。", NamedTextColor.GRAY));
            return;
        }
        for (Ranking.Ranked row : ranking.top(LIMIT)) {
            sender.sendMessage(line(ranking, row, false));
        }
        if (sender instanceof Player player) {
            ranking.rankOf(player.getUniqueId()).ifPresent(row -> sender.sendMessage(
                    Component.text("あなた（" + ranking.size() + "人中）：", NamedTextColor.AQUA)
                            .append(line(ranking, row, true))));
        }
    }

    private static Component line(Ranking ranking, Ranking.Ranked row, boolean self) {
        NamedTextColor rankColor = switch (row.rank()) {
            case 1 -> NamedTextColor.GOLD;
            case 2, 3 -> NamedTextColor.YELLOW;
            default -> NamedTextColor.GRAY;
        };
        return Component.text(row.rank() + "位 ", rankColor)
                .append(Component.text(row.stats().displayName() + " ", self ? NamedTextColor.AQUA : NamedTextColor.WHITE))
                .append(Component.text(ranking.category().format(row.stats()), NamedTextColor.GRAY));
    }

    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String partial = args[0].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (Category category : Category.values()) {
            if (category.id().startsWith(partial)) {
                result.add(category.id());
            }
        }
        return result;
    }
}
