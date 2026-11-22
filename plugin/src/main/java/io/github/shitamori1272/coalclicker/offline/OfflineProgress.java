package io.github.shitamori1272.coalclicker.offline;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.dig.DigEngine;
import io.github.shitamori1272.coalclicker.hud.HudText;
import io.github.shitamori1272.coalclicker.number.BigNum;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.Optional;

/**
 * ログアウト中の自動の掘進を、再ログイン時にまとめて反映する計算（#27）。Bukkit に依存しない。
 *
 * <p>経過時間は {@code now − lastSeen}（上限 {@link #MAX_SECONDS} 秒）。階層をまたぐ分は
 * {@link DigEngine#autoTick} が階層ごとに長さと深さ倍率を変えて進める。
 *
 * <p>二重付与を防ぐため、{@link #grant} は付与の有無にかかわらず最後に {@code lastSeen = now} にする。
 * {@code lastSeen} はほかに DataStore がログアウト時・自動保存時・停止時に「今」へ進めるだけなので、
 * 次のログイン時に数えられるのは、最後にオンラインだった時刻からの分だけになる。
 */
public final class OfflineProgress {

    /** まとめて反映する時間の上限（秒）。8 時間。 */
    public static final long MAX_SECONDS = 8 * 60 * 60;

    /** これより短い留守は反映しない（秒）。すぐ入り直したときに通知を出さないため。 */
    public static final long MIN_SECONDS = 5;

    /** m 数を切り捨てるときの丸め誤差の余裕。 */
    private static final double METERS_EPSILON = 1e-6;

    private OfflineProgress() {
    }

    /**
     * 反映する秒数。記録なし（{@code lastSeen <= 0}）・時計が戻った（負）・{@link #MIN_SECONDS} 秒未満なら 0。
     * 上限は {@link #MAX_SECONDS} 秒。
     */
    public static long grantSeconds(long lastSeen, long now) {
        if (lastSeen <= 0) {
            return 0;
        }
        long elapsed = now - lastSeen;
        if (elapsed < MIN_SECONDS) {
            return 0;
        }
        return Math.min(elapsed, MAX_SECONDS);
    }

    /** 上限で切り詰めたか。 */
    public static boolean capped(long lastSeen, long now) {
        return lastSeen > 0 && now - lastSeen > MAX_SECONDS;
    }

    /**
     * 留守の分を反映し、{@code lastSeen} を {@code now} にする。ログイン時に 1 回だけ呼ぶ。
     *
     * @return プレイヤーへの通知。何も進まなかったときは空
     */
    public static Optional<String> grant(PlayerData data, ServerData server, long now) {
        long lastSeen = data.lastSeen();
        long seconds = grantSeconds(lastSeen, now);
        Optional<String> message = Optional.empty();
        if (seconds > 0) {
            DigEngine.Change change = DigEngine.autoTick(data, server, seconds);
            message = message(change, capped(lastSeen, now));
        }
        // 付与したかどうかにかかわらず、同じ時間を二度数えないよう今にそろえる
        data.setLastSeen(now);
        return message;
    }

    /**
     * 例: {@code 留守の間に 360m 掘り進み、3.6万個の石炭を掘りました。第5坑道まで進みました。}
     * 進まなかった（掘進力 0 など）ときは空。
     */
    public static Optional<String> message(DigEngine.Change change, boolean capped) {
        if (!change.moved() && change.coal().isZero()) {
            return Optional.empty();
        }
        StringBuilder text = new StringBuilder("留守の間に ")
                .append(JapaneseFormat.format(BigNum.of(Math.floor(change.meters() + METERS_EPSILON))))
                .append("m 掘り進み、")
                .append(JapaneseFormat.format(change.coal()))
                .append("個の石炭を掘りました。");
        if (change.levelsCompleted() > 0) {
            text.append(HudText.tunnelName(change.newLevel())).append("まで進みました。");
        }
        if (capped) {
            text.append("（留守の分は 8 時間まで）");
        }
        return Optional.of(text.toString());
    }
}
