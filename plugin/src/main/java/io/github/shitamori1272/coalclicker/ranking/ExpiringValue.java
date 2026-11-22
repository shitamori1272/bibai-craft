package io.github.shitamori1272.coalclicker.ranking;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** 一定時間だけ使い回す値。期限が切れていたら次の {@link #get()} で作り直す。スレッドセーフではない。 */
final class ExpiringValue<T> {

    private final Supplier<T> loader;
    private final LongSupplier clockMillis;
    private final long ttlMillis;
    private T value;
    private long loadedAt;

    ExpiringValue(Supplier<T> loader, LongSupplier clockMillis, long ttlMillis) {
        this.loader = Objects.requireNonNull(loader);
        this.clockMillis = Objects.requireNonNull(clockMillis);
        this.ttlMillis = ttlMillis;
    }

    T get() {
        long now = clockMillis.getAsLong();
        if (value == null || now - loadedAt >= ttlMillis) {
            value = Objects.requireNonNull(loader.get());
            loadedAt = now;
        }
        return value;
    }

    /** 次の {@link #get()} で必ず作り直す。 */
    void invalidate() {
        value = null;
    }
}
