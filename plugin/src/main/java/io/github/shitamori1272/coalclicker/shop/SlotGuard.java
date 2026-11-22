package io.github.shitamori1272.coalclicker.shop;

import java.util.Arrays;
import java.util.Objects;

/**
 * 中身が別のものに変わったばかりのマスのクリックを、しばらく受け付けないための記録。Bukkit に依存しない。
 * 見ていたものと違うものを、クリックの遅れで買ってしまわないようにする念のための守り。
 *
 * <p>「別のもの」かどうかはマスの鍵（例: {@code upgrade:lamp_1}）で見る。説明文の数字が変わっただけでは変わらない。
 */
final class SlotGuard {

    /** 中身が変わってからクリックを受け付けない時間（ミリ秒）。 */
    static final long GUARD_MILLIS = 300;

    private final String[] keys;
    private final long[] changedAt;

    SlotGuard(int size) {
        keys = new String[size];
        changedAt = new long[size];
        Arrays.fill(changedAt, Long.MIN_VALUE);
    }

    /** そのマスの今の鍵を記録する。前と違えば、変わった時刻を {@code now} にする。 */
    void record(int slot, String key, long now) {
        if (!Objects.equals(keys[slot], key)) {
            keys[slot] = key;
            changedAt[slot] = now;
        }
    }

    /** {@code now} の時点で、そのマスのクリックを止めるべきか。 */
    boolean isGuarded(int slot, long now) {
        if (slot < 0 || slot >= keys.length || changedAt[slot] == Long.MIN_VALUE) {
            return false;
        }
        return now - changedAt[slot] < GUARD_MILLIS;
    }
}
