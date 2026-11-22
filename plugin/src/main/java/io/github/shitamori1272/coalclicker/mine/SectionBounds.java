package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.world.MineLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * 区画の中で、ブロックを置き直す範囲・プレイヤーが居てよい範囲の決まり。Bukkit に依存しない。
 */
public final class SectionBounds {

    /** 埋め直す範囲の、坑道の中心から Z 方向への幅（坑道の壁より外側まで）。 */
    public static final int REFILL_HALF_WIDTH = MineLayout.TUNNEL_HALF_WIDTH + 2;
    /** 埋め直す範囲の下端（床の下まで）。 */
    public static final int REFILL_MIN_Y = MineLayout.FLOOR_Y - 2;
    /** 埋め直す範囲の上端（天井の上まで）。 */
    public static final int REFILL_MAX_Y = MineLayout.CEILING_MAX_Y + 2;
    /** 埋め直す範囲の西端（坑底の奥の壁まで）。 */
    public static final int REFILL_MIN_X = MineLayout.SHAFT_MIN_X - 1;
    /** 埋め直す範囲の東端（ループの東端の先まで）。 */
    public static final int REFILL_MAX_X = MineLayout.WINDOW_MAX_X + 1;

    /** プレイヤーの当たり判定の半幅（幅 0.6）。 */
    public static final double PLAYER_HALF_WIDTH = 0.3;
    private static final double EPSILON = 1e-6;

    private SectionBounds() {
    }

    /** チャンクの座標。 */
    public record ChunkPos(int x, int z) {
    }

    /** 区画 section を作り直すときに触るチャンクの一覧。 */
    public static List<ChunkPos> chunks(int section) {
        int centerZ = MineLayout.sectionZ(section);
        int minCx = REFILL_MIN_X >> 4;
        int maxCx = REFILL_MAX_X >> 4;
        int minCz = (centerZ - REFILL_HALF_WIDTH) >> 4;
        int maxCz = (centerZ + REFILL_HALF_WIDTH) >> 4;
        List<ChunkPos> result = new ArrayList<>();
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                result.add(new ChunkPos(cx, cz));
            }
        }
        return result;
    }

    /**
     * 切羽を巻き戻すとき、X 座標 x に立つプレイヤーを 64 ブロック手前へ移すか。
     * 当たり判定が新しい切羽（X = 64 の列）に少しでもかかる人は移す。移した先（X ≥ -0.3）は必ず掘ってある。
     */
    public static boolean needsLoopShift(double x) {
        return x + PLAYER_HALF_WIDTH > MineLayout.LOOP_METERS - EPSILON;
    }

    /**
     * プレイヤー（足元 (x, z)）の当たり判定が、区画 section の掘ってある範囲（坑底〜X = tunnelEndX）に収まるか。
     * 高さは見ない（坑道の中なら床と天井で決まる）。
     */
    public static boolean inOpenArea(int section, double meters, double x, double z) {
        double minX = x - PLAYER_HALF_WIDTH;
        double maxX = x + PLAYER_HALF_WIDTH;
        double minZ = z - PLAYER_HALF_WIDTH;
        double maxZ = z + PLAYER_HALF_WIDTH;
        return Math.floor(minX) >= MineLayout.SHAFT_MIN_X
                && Math.floor(maxX - EPSILON) <= MineLayout.tunnelEndX(meters)
                && Math.floor(minZ) >= MineLayout.tunnelMinZ(section)
                && Math.floor(maxZ - EPSILON) <= MineLayout.tunnelMaxZ(section);
    }

    /** (x, z) が区画 section の、坑道を置く範囲（坑底〜ループの東端 × 坑道の幅）に入るか。 */
    public static boolean inSectionTunnel(int section, double x, double z) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        return bx >= MineLayout.SHAFT_MIN_X && bx <= MineLayout.WINDOW_MAX_X
                && bz >= MineLayout.tunnelMinZ(section) && bz <= MineLayout.tunnelMaxZ(section);
    }
}
