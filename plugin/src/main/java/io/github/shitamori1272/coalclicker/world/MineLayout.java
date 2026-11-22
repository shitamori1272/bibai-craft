package io.github.shitamori1272.coalclicker.world;

/**
 * 坑道ワールドの座標と地層の決まり（企画書 5 章）。Bukkit に依存しない。
 *
 * <p>区画 n の坑道は Z = n × 16 を中心に、X = 0 から +X 方向へ伸びる。断面は幅 3（Z = 中心 ± 1）×
 * 高さ 3（Y41〜43）。X = -8〜0 は坑底の乗り場。ワールド上に置くのは X = 0〜128 だけで、
 * 64m 以上進んだ坑道は切羽が X = 64〜127 の間をループする（5-3）。
 */
public final class MineLayout {

    /** 坑道の床（石）。 */
    public static final int FLOOR_Y = 40;
    /** 炭層（石炭鉱石）の下端。 */
    public static final int SEAM_MIN_Y = 41;
    /** 炭層（石炭鉱石）の上端。 */
    public static final int SEAM_MAX_Y = 42;
    /** 天井（砂岩）の下端。 */
    public static final int CEILING_MIN_Y = 43;
    /** 天井（砂岩）の上端。 */
    public static final int CEILING_MAX_Y = 44;

    /** 坑道の空気の下端。 */
    public static final int TUNNEL_MIN_Y = 41;
    /** 坑道の空気の上端。 */
    public static final int TUNNEL_MAX_Y = 43;
    /** 坑道の中心から左右に広げる幅（中心 ± 1 で幅 3）。 */
    public static final int TUNNEL_HALF_WIDTH = 1;

    /** 区画どうしの Z 方向の間隔。 */
    public static final int SECTION_SPACING = 16;
    /** 坑道の入口（区画の中で坑道が始まる X）。 */
    public static final int TUNNEL_START_X = 0;
    /** 坑底の乗り場の西端。 */
    public static final int SHAFT_MIN_X = -8;
    /** 坑底の乗り場の東端。 */
    public static final int SHAFT_MAX_X = 0;

    /** ループ表示の周期（m）。切羽はこの分だけ手前へ巻き戻る。 */
    public static final int LOOP_METERS = 64;
    /** ワールド上に坑道を置く範囲の東端。切羽がここに達したら巻き戻す。 */
    public static final int WINDOW_MAX_X = 128;
    /** 坑木を立てる間隔（m）。ループ周期の約数なので巻き戻しても見た目が変わらない。 */
    public static final int TIMBER_INTERVAL = 4;

    /**
     * 区画番号の上限。坑道の端（Z = 中心 + 1）がワールドボーダーの最大値 29,999,984 を超えない範囲。
     */
    public static final int MAX_SECTION = (29_999_984 - TUNNEL_HALF_WIDTH) / SECTION_SPACING;

    /** 坑道の高さにある地層の種類。 */
    public enum Stratum {
        /** 石。床とそれ以外のすべて。 */
        STONE,
        /** 炭層（石炭鉱石）。 */
        COAL_SEAM,
        /** 砂岩の天井。 */
        SANDSTONE
    }

    private MineLayout() {
    }

    /** 高さ y にある地層。 */
    public static Stratum stratumAt(int y) {
        if (y >= SEAM_MIN_Y && y <= SEAM_MAX_Y) {
            return Stratum.COAL_SEAM;
        }
        if (y >= CEILING_MIN_Y && y <= CEILING_MAX_Y) {
            return Stratum.SANDSTONE;
        }
        return Stratum.STONE;
    }

    /** 区画 section の坑道の中心の Z。 */
    public static int sectionZ(int section) {
        if (section < 0 || section > MAX_SECTION) {
            throw new IllegalArgumentException("区画番号が範囲外です: " + section);
        }
        return section * SECTION_SPACING;
    }

    /** 区画 section の坑道の北端（空気になる最小の Z）。 */
    public static int tunnelMinZ(int section) {
        return sectionZ(section) - TUNNEL_HALF_WIDTH;
    }

    /** 区画 section の坑道の南端（空気になる最大の Z）。 */
    public static int tunnelMaxZ(int section) {
        return sectionZ(section) + TUNNEL_HALF_WIDTH;
    }

    /**
     * Z 座標 z がどの区画の範囲（中心 - 8 〜 中心 + 7）に入るか。区画 0 より北なら -1。
     */
    public static int sectionAt(int z) {
        int section = Math.floorDiv(z + SECTION_SPACING / 2, SECTION_SPACING);
        return section < 0 || section > MAX_SECTION ? -1 : section;
    }

    /** (y, z) が区画 section の坑道の断面（幅 3 × 高さ 3）に入るか。 */
    public static boolean inTunnelCrossSection(int section, int y, int z) {
        return y >= TUNNEL_MIN_Y && y <= TUNNEL_MAX_Y
                && z >= tunnelMinZ(section) && z <= tunnelMaxZ(section);
    }

    /**
     * 進み具合 meters の坑道で、切羽（まだ掘っていない最初の列）の X。
     *
     * <p>64m 未満では「進み具合 + 1」。64m 以上では X = 64〜127 をループし、
     * 切羽が X = 128 に達するところで X = 64 に巻き戻る。端数の m は切り捨てる。
     */
    public static int faceX(double meters) {
        double whole = wholeMeters(meters);
        if (whole < LOOP_METERS - 1) {
            return (int) whole + 1;
        }
        // whole = 63 で X = 64、whole = 126 で X = 127、whole = 127 で X = 64 に戻る
        return LOOP_METERS + (int) ((whole - (LOOP_METERS - 1)) % LOOP_METERS);
    }

    /** 進み具合 meters の坑道で、空気になっている最も東の X（X = 0〜この値が坑道）。 */
    public static int tunnelEndX(double meters) {
        return faceX(meters) - 1;
    }

    /**
     * 進み具合 meters までに切羽を巻き戻した回数。
     *
     * <p>前後の回数が違えば、坑道を作り直してプレイヤーを {@link #LOOP_METERS} ブロック手前へ移す。
     */
    public static long loopCount(double meters) {
        double whole = wholeMeters(meters);
        if (whole < LOOP_METERS - 1) {
            return 0;
        }
        return (long) ((whole - (LOOP_METERS - 1)) / LOOP_METERS);
    }

    /** X が坑道を置く範囲（X = 0〜128）に入るか。 */
    public static boolean inWindow(int x) {
        return x >= TUNNEL_START_X && x <= WINDOW_MAX_X;
    }

    /** X が坑底の乗り場（X = -8〜0）に入るか。 */
    public static boolean inShaftBottom(int x) {
        return x >= SHAFT_MIN_X && x <= SHAFT_MAX_X;
    }

    /** (x, y, z) が区画 section の坑底の乗り場（X = -8〜0 × 坑道の断面）に入るか。 */
    public static boolean inShaftBottomRoom(int section, int x, int y, int z) {
        return inShaftBottom(x) && inTunnelCrossSection(section, y, z);
    }

    private static double wholeMeters(double meters) {
        if (!Double.isFinite(meters) || meters < 0) {
            throw new IllegalArgumentException("進み具合が不正です: " + meters);
        }
        return Math.floor(meters);
    }
}
