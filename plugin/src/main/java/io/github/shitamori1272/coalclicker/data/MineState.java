package io.github.shitamori1272.coalclicker.data;

/**
 * プレイヤー 1 人分の坑道の進み具合。値を変えると持ち主の {@link PlayerData} が保存対象になる。
 *
 * <p>サーバーのメインスレッドからだけ触ること。
 */
public final class MineState {

    /** 区画をまだ割り当てていないことを表す値。 */
    public static final int NO_SECTION = -1;

    private final Runnable onChange;

    private int section = NO_SECTION;
    private long level;
    private double progress;
    private long maxReachedLevel;

    MineState(Runnable onChange) {
        this.onChange = onChange;
    }

    /** 変更通知を出さずに other の値を写す。 */
    void copyFrom(MineState other) {
        section = other.section;
        level = other.level;
        progress = other.progress;
        maxReachedLevel = other.maxReachedLevel;
    }

    /** 区画番号。未割り当てなら {@link #NO_SECTION}。 */
    public int section() {
        return section;
    }

    public boolean hasSection() {
        return section != NO_SECTION;
    }

    public void setSection(int section) {
        this.section = section;
        onChange.run();
    }

    /** 数値上の階層（0 始まり）。 */
    public long level() {
        return level;
    }

    public void setLevel(long level) {
        if (level < 0) {
            throw new IllegalArgumentException("階層は 0 以上にしてください: " + level);
        }
        this.level = level;
        onChange.run();
    }

    /** 今の階層での進み具合（割合 0〜1）。 */
    public double progress() {
        return progress;
    }

    /** 進み具合を設定する。0〜1 の範囲外は丸める。 */
    public void setProgress(double progress) {
        if (Double.isNaN(progress)) {
            throw new IllegalArgumentException("進み具合が数ではありません");
        }
        this.progress = Math.clamp(progress, 0.0, 1.0);
        onChange.run();
    }

    /** 到達ボーナスを受け取った一番深い階層。 */
    public long maxReachedLevel() {
        return maxReachedLevel;
    }

    public void setMaxReachedLevel(long maxReachedLevel) {
        if (maxReachedLevel < 0) {
            throw new IllegalArgumentException("階層は 0 以上にしてください: " + maxReachedLevel);
        }
        this.maxReachedLevel = maxReachedLevel;
        onChange.run();
    }
}
