package io.github.shitamori1272.coalclicker.world;

import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

/**
 * 坑道ワールドの地形。全部岩で、空も洞窟も構造物もない（企画書 5-1）。
 *
 * <p>坑道の高さだけ地層を変える（Y40 石・Y41〜42 炭層・Y43〜44 砂岩）。最下段は岩盤。
 * バニラの地形・地表・洞窟・装飾・構造物・モブの生成はすべて止める。
 * 階層ごとの地層（5-5）は、入坑時に坑道側のコードが区画を埋め直して表現する。
 */
public final class MineChunkGenerator extends ChunkGenerator {

    /**
     * 唯一のバイオーム。the_void はモブの湧き設定が空なので、坑道の暗がりにも敵が湧かない
     * （ゲームルールの spawnMobs も切っているが、二重に防ぐ）。
     */
    private static final Biome BIOME = Biome.THE_VOID;

    private static final BiomeProvider BIOME_PROVIDER = new BiomeProvider() {
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return BIOME;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(BIOME);
        }
    };

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData data) {
        int min = data.getMinHeight();
        int max = data.getMaxHeight();
        data.setRegion(0, min, 0, 16, max, 16, Material.STONE);
        data.setRegion(0, MineLayout.SEAM_MIN_Y, 0, 16, MineLayout.SEAM_MAX_Y + 1, 16, Material.COAL_ORE);
        data.setRegion(0, MineLayout.CEILING_MIN_Y, 0, 16, MineLayout.CEILING_MAX_Y + 1, 16, Material.SANDSTONE);
        data.setRegion(0, min, 0, 16, min + 1, 16, Material.BEDROCK);
        carveSpawnRoom(chunkX, chunkZ, data);
    }

    /**
     * 区画 0 の坑底の乗り場を空気にする。初期地点が岩に埋まらないよう、坑道のコードより先に用意しておく。
     * 床（Y40）は石のまま残る。
     */
    private static void carveSpawnRoom(int chunkX, int chunkZ, ChunkData data) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minX = Math.max(MineLayout.SHAFT_MIN_X, baseX);
        int maxX = Math.min(MineLayout.SHAFT_MAX_X, baseX + 15);
        int minZ = Math.max(MineLayout.tunnelMinZ(0), baseZ);
        int maxZ = Math.min(MineLayout.tunnelMaxZ(0), baseZ + 15);
        if (minX > maxX || minZ > maxZ) {
            return;
        }
        data.setRegion(minX - baseX, MineLayout.TUNNEL_MIN_Y, minZ - baseZ,
                maxX - baseX + 1, MineLayout.TUNNEL_MAX_Y + 1, maxZ - baseZ + 1, Material.AIR);
    }

    /** 地層の種類に対応するブロック（第 1〜3 坑道の地層。階層ごとの地層は LevelStrata）。 */
    public static Material material(MineLayout.Stratum stratum) {
        return switch (stratum) {
            case STONE -> Material.STONE;
            case COAL_SEAM -> Material.COAL_ORE;
            case SANDSTONE -> Material.SANDSTONE;
        };
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return BIOME_PROVIDER;
    }

    /** 区画 0 の坑底の乗り場の中央、床の上に立つ位置。 */
    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return MineWorld.spawnIn(world);
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}
