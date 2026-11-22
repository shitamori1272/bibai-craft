package io.github.shitamori1272.coalclicker.world;

/**
 * 階層ごとの地層（企画書 5-5）。Bukkit に依存しない。
 *
 * <p>階層 level は 0 始まり（第 1 坑道 = 0）。
 * <ul>
 *   <li>第 1〜3 坑道（level 0〜2）：石の岩盤・石炭鉱石の炭層・砂岩の天井（5-1 のまま）</li>
 *   <li>第 4〜6 坑道（level 3〜5）：岩盤と天井に凝灰岩がまだらに混ざる。炭層は石炭鉱石のまま</li>
 *   <li>第 7 坑道〜（level 6〜）：深層岩の岩盤・深層石炭鉱石の炭層。天井は凝灰岩
 *       （火山灰が積もってできた堆積岩で、深層岩の黒と色が分かれて層に見える）</li>
 * </ul>
 *
 * <p>ループ表示（5-3）で巻き戻しに気づかれないよう、結果は X について {@link MineLayout#LOOP_METERS}
 * の周期になる。凝灰岩の混ざり方は乱数の状態を持たず、(X mod 64, Y, Z) のハッシュだけで決める。
 */
public final class LevelStrata {

    /** ブロックの種類。Bukkit の Material への対応は呼ぶ側で行う。 */
    public enum Rock {
        STONE,
        COAL_ORE,
        SANDSTONE,
        TUFF,
        DEEPSLATE,
        DEEPSLATE_COAL_ORE;

        /** 炭層の鉱石（手掘りの切羽に見える石炭）か。 */
        public boolean isCoalOre() {
            return this == COAL_ORE || this == DEEPSLATE_COAL_ORE;
        }
    }

    /** 凝灰岩が混ざり始める階層（第 4 坑道）。 */
    public static final long TUFF_FROM_LEVEL = 3;
    /** 深層岩になる階層（第 7 坑道）。 */
    public static final long DEEPSLATE_FROM_LEVEL = 6;

    /** 凝灰岩が混ざる階層で、岩盤（石）のうち凝灰岩にする割合（/16）。 */
    static final int ROCK_TUFF_SIXTEENTHS = 5;
    /** 凝灰岩が混ざる階層で、天井（砂岩）のうち凝灰岩にする割合（/16）。 */
    static final int CEILING_TUFF_SIXTEENTHS = 8;

    private LevelStrata() {
    }

    /** 階層 level の (x, y, z) にある岩。 */
    public static Rock at(long level, int x, int y, int z) {
        if (level < 0) {
            throw new IllegalArgumentException("階層が不正です: " + level);
        }
        MineLayout.Stratum stratum = MineLayout.stratumAt(y);
        if (level >= DEEPSLATE_FROM_LEVEL) {
            return switch (stratum) {
                case STONE -> Rock.DEEPSLATE;
                case COAL_SEAM -> Rock.DEEPSLATE_COAL_ORE;
                case SANDSTONE -> Rock.TUFF;
            };
        }
        if (level >= TUFF_FROM_LEVEL) {
            return switch (stratum) {
                case STONE -> mixed(x, y, z, ROCK_TUFF_SIXTEENTHS) ? Rock.TUFF : Rock.STONE;
                case COAL_SEAM -> Rock.COAL_ORE;
                case SANDSTONE -> mixed(x, y, z, CEILING_TUFF_SIXTEENTHS) ? Rock.TUFF : Rock.SANDSTONE;
            };
        }
        return switch (stratum) {
            case STONE -> Rock.STONE;
            case COAL_SEAM -> Rock.COAL_ORE;
            case SANDSTONE -> Rock.SANDSTONE;
        };
    }

    /** (x mod 64, y, z) のハッシュで、16 分の sixteenths の割合で true になる。 */
    private static boolean mixed(int x, int y, int z, int sixteenths) {
        return (hash(Math.floorMod(x, MineLayout.LOOP_METERS), y, z) & 15) < sixteenths;
    }

    private static int hash(int x, int y, int z) {
        int h = x * 0x9E3779B1 ^ y * 0x85EBCA77 ^ z * 0xC2B2AE3D;
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        h *= 0x846CA68B;
        h ^= h >>> 16;
        return h;
    }
}
