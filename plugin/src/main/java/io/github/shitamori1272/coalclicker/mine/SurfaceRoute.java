package io.github.shitamori1272.coalclicker.mine;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

/** 駅前のバスに乗ると坑口前へ。地上のアーチをくぐった本人だけが入坑する。 */
public final class SurfaceRoute implements Listener {
    private static final NamespacedKey OVERWORLD = NamespacedKey.minecraft("overworld");
    private final Plugin plugin;
    private final MineService mines;

    public SurfaceRoute(Plugin plugin, MineService mines) {
        this.plugin = plugin;
        this.mines = mines;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWalk(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!event.hasChangedBlock() || !world.getKey().equals(OVERWORLD)
                || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (insideBus(to) && !insideBus(from)
                && world.getBlockAt(to.getBlockX(), -53, to.getBlockZ()).getType() == Material.IRON_BLOCK) {
            // The two overlays can be installed separately on a development server.
            if (!hasEntrance(world) || world.getBlockAt(10188, 115, 1245).getType() != Material.COARSE_DIRT
                    || !world.getBlockAt(10188, 116, 1245).isPassable()
                    || !world.getBlockAt(10188, 117, 1245).isPassable()) {
                player.sendMessage("坑口前に降りられる場所がありません。");
                return;
            }
            event.setTo(new Location(world, 10188.5, 116, 1245.5, 180, -12));
            player.sendMessage("炭鉱メモリアル公園です。「竪坑々口」のアーチをくぐると採掘を始められます。");
        } else if (insideEntrance(to) && !insideEntrance(from) && hasEntrance(world)) {
            // Wait until Paper has committed the walk before saving the return point or teleporting.
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && player.getWorld().equals(world) && insideEntrance(player.getLocation())) {
                    mines.enter(player);
                }
            });
        }
    }

    private static boolean hasEntrance(World world) {
        return world.getBlockAt(10188, 120, 1243).getType() == Material.BIRCH_WALL_SIGN;
    }

    private static boolean insideBus(Location at) {
        int x = at.getBlockX(), z = at.getBlockZ();
        return x >= 1509 && x <= 1510 && z >= 1574 && z <= 1575 && at.getBlockY() == -52;
    }

    private static boolean insideEntrance(Location at) {
        int x = at.getBlockX(), y = at.getBlockY(), z = at.getBlockZ();
        return x >= 10187 && x <= 10189 && z >= 1235 && z <= 1241 && y >= 111 && y <= 119;
    }
}
