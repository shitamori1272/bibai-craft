package io.github.shitamori1272.coalclicker.boost;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * 良質な炭層（#63）の時間のある効果（豊炭・手掘り祭り）を、プレイヤーごとに覚えておく係。Bukkit に依存しない。
 *
 * <p>時計（ミリ秒）は差し替えられる（テスト用）。効果はメモリだけに持ち、保存しない（再起動で消える）。
 * 同じ効果がもう一度起きたら、倍率は重ねず、残り時間をその効果の長さに戻す。違う効果は同時に続く
 * （豊炭と手掘り祭りは掛かる先が違うので、それぞれの倍率が別々に掛かる）。
 *
 * <p><b>使う側</b>：石炭を足すところで次のように掛ける。
 * <ul>
 *   <li>自動（毎秒の石炭）：{@code Formulas.coalPerSecond(data).multiply(boost.productionMultiplier(uuid))}
 *   <li>手掘り：{@code Formulas.clickCoal(data.upgrades(), 毎秒の石炭 × productionMultiplier).multiply(boost.clickMultiplier(uuid))}
 *       （熟練の改良で毎秒の石炭の一部が手掘りに乗るので、豊炭中は手掘りも少し増える）
 * </ul>
 * サーバーのメインスレッドから使うこと。
 */
public final class BoostService {

    private final LongSupplier clock;
    /** プレイヤー → 効果 → 終わる時刻（ミリ秒）。 */
    private final Map<UUID, Map<BoostEffect, Long>> active = new HashMap<>();

    /** 実際の時計（{@link System#currentTimeMillis}）を使う。 */
    public BoostService() {
        this(System::currentTimeMillis);
    }

    public BoostService(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /** 今の時刻（ミリ秒）。 */
    public long now() {
        return clock.getAsLong();
    }

    /**
     * 時間のある効果を始める（続いていれば残り時間を戻す）。
     *
     * @throws IllegalArgumentException 時間のない効果（大当たり）のとき
     */
    public void activate(UUID player, BoostEffect effect) {
        if (!effect.timed()) {
            throw new IllegalArgumentException("時間のない効果です: " + effect);
        }
        active.computeIfAbsent(player, id -> new EnumMap<>(BoostEffect.class))
                .put(effect, now() + effect.durationMillis());
    }

    /** 毎秒の石炭に掛ける倍率。効果がなければ 1。 */
    public double productionMultiplier(UUID player) {
        double multiplier = 1;
        for (BoostEffect effect : BoostEffect.values()) {
            if (remainingMillis(player, effect) > 0) {
                multiplier *= effect.productionMultiplier();
            }
        }
        return multiplier;
    }

    /** 手掘り 1 回の石炭に掛ける倍率。効果がなければ 1。 */
    public double clickMultiplier(UUID player) {
        double multiplier = 1;
        for (BoostEffect effect : BoostEffect.values()) {
            if (remainingMillis(player, effect) > 0) {
                multiplier *= effect.clickMultiplier();
            }
        }
        return multiplier;
    }

    /** effect の残り時間（ミリ秒）。続いていなければ 0。 */
    public long remainingMillis(UUID player, BoostEffect effect) {
        Map<BoostEffect, Long> effects = active.get(player);
        if (effects == null) {
            return 0;
        }
        Long until = effects.get(effect);
        return until == null ? 0 : Math.max(0, until - now());
    }

    /** 終わった効果を忘れる。定期的に呼ぶ。 */
    public void purgeExpired() {
        long now = now();
        active.values().removeIf(effects -> {
            effects.values().removeIf(until -> until <= now);
            return effects.isEmpty();
        });
    }

    /** player の効果をすべて消す（管理者のリセットなど）。 */
    public void clear(UUID player) {
        active.remove(player);
    }
}
