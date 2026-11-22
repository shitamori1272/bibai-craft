package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /coal surface}：地上へ戻る。 */
public final class SurfaceCommand implements SubCommand {

    private final MineService service;

    public SurfaceCommand(MineService service) {
        this.service = service;
    }

    @Override
    public String path() {
        return "surface";
    }

    @Override
    public String description() {
        return "地上へ戻る";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        service.leave((Player) sender);
    }
}
