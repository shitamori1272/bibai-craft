package io.github.shitamori1272.coalclicker.data;

/**
 * プレイヤー 1 人分の坑道の保存データ。値を変えると持ち主の {@link PlayerData} が保存対象になる。
 *
 * <p>数値上の階層と進み具合は累計採掘量から計算する（{@code game.Formulas#levelOf}・{@code progressOf}）ので持たない。
 *
 * <p>サーバーのメインスレッドからだけ触ること。
 */
public final class MineState {

    /** 区画をまだ割り当てていないことを表す値。 */
    public static final int NO_SECTION = -1;

    private final Runnable onChange;

    private int section = NO_SECTION;
    private long maxReachedLevel;

    MineState(Runnable onChange) {
        this.onChange = onChange;
    }

    /** 変更通知を出さずに other の値を写す。 */
    void copyFrom(MineState other) {
        section = other.section;
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
