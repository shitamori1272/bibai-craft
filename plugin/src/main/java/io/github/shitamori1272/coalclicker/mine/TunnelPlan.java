package io.github.shitamori1272.coalclicker.mine;

import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.world.MineLayout;

/**
 * 進み具合が変わったときに、坑道のブロックをどう直すかの計画（企画書 5-3）。Bukkit に依存しない。
 *
 * <ul>
 *   <li>{@link Kind#NONE}：見た目は変わらない（同じ 1m の中での増減など）</li>
 *   <li>{@link Kind#CARVE}：X = {@link #fromX()}〜{@link #toX()} の列を新しく掘る</li>
 *   <li>{@link Kind#REBUILD}：区画を作り直す。{@link #shiftPlayers()} なら、切羽を巻き戻したので
 *       当たり判定が X = {@link MineLayout#LOOP_METERS} の列にかかるプレイヤーを {@link MineLayout#LOOP_METERS} ブロック手前へ移す
 *       （{@link SectionBounds#needsLoopShift(double)}）</li>
 * </ul>
 *
 * @param fromX 掘る列の西端（CARVE のときだけ意味がある）
 * @param toX 掘る列の東端（CARVE のときだけ意味がある）
 */
public record TunnelPlan(Kind kind, int fromX, int toX, boolean shiftPlayers) {

    /** 計画の種類。 */
    public enum Kind {
        NONE,
        CARVE,
        REBUILD
    }

    /** 坑木を立てる X か。坑底側の入口（X = 0）には立てない。 */
    public static boolean isTimberX(int x) {
        return x > MineLayout.TUNNEL_START_X && x % MineLayout.TIMBER_INTERVAL == 0;
    }

    /** 天井に見えない明かり（ライトブロック）を置く X か。坑木の間に 8m ごと。ループ周期の約数にしておく。 */
    public static boolean isLightX(int x) {
        return x > MineLayout.TUNNEL_START_X && x % LIGHT_INTERVAL == LIGHT_OFFSET;
    }

    /** 明かりの間隔（m）。{@link MineLayout#LOOP_METERS} の約数。 */
    static final int LIGHT_INTERVAL = 8;
    /** 明かりを置く位置（坑木の間）。 */
    static final int LIGHT_OFFSET = 2;

    /**
     * 進み具合（割合）と坑道の長さから、数値上の進み具合（m）を出す。
     * 割合の掛け算の誤差で整数の m をわずかに下回り、切羽が 1 列手前に見えるのを防ぐため、整数のごく近くは整数に丸める。
     */
    public static double meters(double progress, double tunnelLength) {
        double meters = Math.clamp(progress, 0.0, 1.0) * tunnelLength;
        double rounded = Math.rint(meters);
        if (Math.abs(meters - rounded) < 1e-6 * Math.max(1.0, rounded)) {
            return rounded;
        }
        return meters;
    }

    /**
     * 階層 renderedLevel の坑道を置いているときに見せる進み具合（m）。累計採掘量 totalMined から決まる数値上の階層が
     * <ul>
     *   <li>同じなら、その階層の進み具合 × L
     *   <li>進んでいれば（ケージ待ち）、置いてある坑道を掘り切った長さ L
     *   <li>戻っていれば（ふつうは起きない）、0
     * </ul>
     */
    public static double displayedMeters(long renderedLevel, BigNum totalMined) {
        long numeric = Formulas.levelOf(totalMined);
        double length = Formulas.tunnelLength(renderedLevel);
        if (numeric > renderedLevel) {
            return length;
        }
        if (numeric < renderedLevel) {
            return 0;
        }
        return meters(Formulas.progressOf(totalMined), length);
    }

    /**
     * 進み具合が oldMeters から newMeters に変わったときの計画。
     *
     * <ul>
     *   <li>巻き戻しの回数（{@link MineLayout#loopCount}）が増えたら作り直して、プレイヤーを手前へ移す
     *       （一度に 64m 以上進んだ場合もこれに含まれる）</li>
     *   <li>進み具合が減ったら作り直す（プレイヤーは移さない）</li>
     *   <li>同じ周回の中で切羽が進んだら、増えた列だけ掘る</li>
     * </ul>
     */
    public static TunnelPlan plan(double oldMeters, double newMeters) {
        long oldLoop = MineLayout.loopCount(oldMeters);
        long newLoop = MineLayout.loopCount(newMeters);
        if (newLoop > oldLoop) {
            return new TunnelPlan(Kind.REBUILD, 0, 0, true);
        }
        if (newLoop < oldLoop) {
            return new TunnelPlan(Kind.REBUILD, 0, 0, false);
        }
        int oldEnd = MineLayout.tunnelEndX(oldMeters);
        int newEnd = MineLayout.tunnelEndX(newMeters);
        if (newEnd == oldEnd) {
            return new TunnelPlan(Kind.NONE, 0, 0, false);
        }
        if (newEnd < oldEnd) {
            return new TunnelPlan(Kind.REBUILD, 0, 0, false);
        }
        return new TunnelPlan(Kind.CARVE, oldEnd + 1, newEnd, false);
    }
}
