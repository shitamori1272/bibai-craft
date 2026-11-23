package io.github.shitamori1272.coalclicker.trophy;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal trophy}：トロフィーの一覧を開く。 */
public final class TrophyCommand implements SubCommand {

    private final TrophyListener trophies;

    public TrophyCommand(TrophyListener trophies) {
        this.trophies = trophies;
    }

    @Override
    public String path() {
        return "trophy";
    }

    @Override
    public String description() {
        return "もらったトロフィーとまだのトロフィーを見る";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        trophies.open((Player) sender);
    }
}
