package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal mine}：自分の坑道へ入坑する。 */
public final class MineCommand implements SubCommand {

    private final MineService service;

    public MineCommand(MineService service) {
        this.service = service;
    }

    @Override
    public String path() {
        return "mine";
    }

    @Override
    public String description() {
        return "自分の坑道へ入坑する";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        service.enter((Player) sender);
    }
}
