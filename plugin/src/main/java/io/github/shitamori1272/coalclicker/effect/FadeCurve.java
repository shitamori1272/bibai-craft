package io.github.shitamori1272.coalclicker.effect;

/**
 * フェードアウトの音量の段階（{@link SoundFade}）。Bukkit に依存しない。
 *
 * <p>最初の音量 {@code volume} から、{@code steps} 段で 0 まで下げる。i 段目（1 始まり）の音量は
 * volume × ((steps − i) ÷ steps)^2。耳には音量の比で聞こえるので、2 乗にして最後のほうをなだらかにする。
 * 最後の段（i ＝ steps）は 0（止めるだけ）。
 */
public final class FadeCurve {

    private FadeCurve() {
    }

    /** i 段目（1〜steps）の音量。 */
    public static float volume(float volume, int step, int steps) {
        if (steps <= 0) {
            throw new IllegalArgumentException("段の数は 1 以上にしてください: " + steps);
        }
        if (step < 1 || step > steps) {
            throw new IllegalArgumentException("段は 1〜" + steps + " にしてください: " + step);
        }
        float rest = (float) (steps - step) / steps;
        return volume * rest * rest;
    }
}
