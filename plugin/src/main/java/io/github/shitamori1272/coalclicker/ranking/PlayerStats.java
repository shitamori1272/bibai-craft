package io.github.shitamori1272.coalclicker.ranking;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Objects;
import java.util.UUID;

/**
 * ランキング用に抜き出した 1 人分の値。
 *
 * @param name 表示名。分からなければ null
 * @param deepestLevel 到達した一番深い階層（0 始まり）
 */
public record PlayerStats(UUID uuid, String name, BigNum totalMined, long deepestLevel, BigNum coalPerSecond) {

    public PlayerStats {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(totalMined);
        Objects.requireNonNull(coalPerSecond);
    }

    /** 保存データから作る。到達深さは「到達ボーナス済みの最深」と「今いる階層」の深いほう。 */
    public static PlayerStats from(PlayerData data, String name) {
        long level = Formulas.levelOf(data.totalMined());
        return new PlayerStats(data.uuid(), name, data.totalMined(),
                Math.max(data.mine().maxReachedLevel(), level), Formulas.coalPerSecond(data));
    }

    /** 表示名。名前が分からなければ UUID の先頭 8 文字。 */
    public String displayName() {
        return name != null && !name.isBlank() ? name : uuid.toString().substring(0, 8);
    }
}
