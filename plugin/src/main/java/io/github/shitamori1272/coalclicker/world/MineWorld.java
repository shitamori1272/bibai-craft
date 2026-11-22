package io.github.shitamori1272.coalclicker.world;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

/**
 * 坑道の専用ワールド {@code bibai_mine}（企画書 5-1）。プラグインの起動時に作るか読み込む。
 *
 * <p>Paper 26.1 以降、追加のワールドは主ワールドのフォルダの中、
 * {@code <主ワールド>/dimensions/<namespace>/<key>/} に保存される。キーを {@link #KEY} に固定しているので、
 * 保存先は {@code server-data/bibai/dimensions/coalclicker/bibai_mine/}。
 * {@code server/World.java} の publish はこのフォルダを原本にコピーしない。
 */
public final class MineWorld {

    /** ワールドのキー。保存先のフォルダ名にもなる。 */
    public static final NamespacedKey KEY = new NamespacedKey("coalclicker", "bibai_mine");

    /** 坑内は昼も夜もないので、時刻は正午に止める（空は見えないが、時刻依存の処理を安定させる）。 */
    private static final long FIXED_TIME = 6000;

    private final World world;

    private MineWorld(World world) {
        this.world = world;
    }

    /**
     * 坑道ワールドを作るか読み込む。主ワールドの読み込みが終わってから呼ぶこと
     * （paper-plugin.yml の既定の {@code load: POSTWORLD} なら onEnable で呼べる）。
     */
    public static MineWorld load(Plugin plugin) {
        World world = Bukkit.getWorld(KEY);
        if (world == null) {
            world = WorldCreator.ofKey(KEY)
                    .environment(World.Environment.NORMAL)
                    .type(WorldType.NORMAL)
                    .generator(new MineChunkGenerator())
                    .generateStructures(false)
                    .bonusChest(false)
                    .createWorld();
        }
        if (world == null) {
            throw new IllegalStateException("坑道ワールドを作れませんでした: " + KEY);
        }
        configure(world);
        plugin.getLogger().info("坑道ワールドを読み込みました: " + KEY + " (" + world.getWorldPath() + ")");
        return new MineWorld(world);
    }

    /** 静かな地下にするための設定。ワールドを読み込むたびにかけ直す。 */
    private static void configure(World world) {
        set(world, GameRules.ADVANCE_TIME, false);
        set(world, GameRules.ADVANCE_WEATHER, false);
        set(world, GameRules.SPAWN_MOBS, false);
        set(world, GameRules.SPAWN_MONSTERS, false);
        set(world, GameRules.SPAWN_PHANTOMS, false);
        set(world, GameRules.SPAWN_PATROLS, false);
        set(world, GameRules.SPAWN_WANDERING_TRADERS, false);
        set(world, GameRules.SPAWN_WARDENS, false);
        set(world, GameRules.RAIDS, false);
        set(world, GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
        set(world, GameRules.MOB_GRIEFING, false);
        set(world, GameRules.KEEP_INVENTORY, true);
        world.setTime(FIXED_TIME);
        world.setStorm(false);
        world.setThundering(false);
        world.setAllowMonsterSpawning(false);
        world.setSpawnLocation(spawnIn(world));
    }

    private static <T> void set(World world, GameRule<T> rule, T value) {
        world.setGameRule(rule, value);
    }

    /** ワールド world の中の、区画 0 の坑底の乗り場の中央で床の上に立つ位置（東向き）。 */
    static Location spawnIn(World world) {
        double x = (MineLayout.SHAFT_MIN_X + MineLayout.SHAFT_MAX_X) / 2.0 + 0.5;
        double z = MineLayout.sectionZ(0) + 0.5;
        return new Location(world, x, MineLayout.FLOOR_Y + 1, z, -90f, 0f);
    }

    /** 坑道ワールド。 */
    public World world() {
        return world;
    }

    /** ワールドの初期地点（区画 0 の坑底）。 */
    public Location spawn() {
        return spawnIn(world);
    }
}
