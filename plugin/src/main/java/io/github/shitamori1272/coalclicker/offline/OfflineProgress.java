package io.github.shitamori1272.coalclicker.offline;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.dig.DigEngine;
import io.github.shitamori1272.coalclicker.hud.HudText;
import io.github.shitamori1272.coalclicker.number.JapaneseFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * ログアウト中の毎秒の石炭を、再ログイン時にまとめて反映する計算（企画書 3 章 6・5-5）。Bukkit に依存しない。
 *
 * <p>経過時間は {@code now − lastSeen}（上限 {@link #MAX_SECONDS} 秒）。毎秒の石炭 × 秒数を
 * {@link DigEngine#autoTick} で足す。毎秒の石炭は深さ倍率を含むので、留守の間に階層が進めば途中から増える。
 * {@link DigEngine#autoCoal} が階層の境目ごとに区切って足すので、オンラインで 1 秒ずつ足したときと同じになる。
 * 道具や改良は留守の間に増えないので、深さ倍率の前の毎秒の石炭は一定。
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
     * 例: {@code 留守の間に 3.6万個の石炭を掘りました。第5坑道まで進みました（2階層）。}
     * 何も採れなかった（道具がないなど）ときは空。
     */
    public static Optional<String> message(DigEngine.Change change, boolean capped) {
        if (change.coal().signum() <= 0) {
            return Optional.empty();
        }
        StringBuilder text = new StringBuilder("留守の間に ")
                .append(JapaneseFormat.format(change.coal()))
                .append("個の石炭を掘りました。");
        if (change.levelsAdvanced() > 0) {
            text.append(HudText.tunnelName(change.newLevel())).append("まで進みました（")
                    .append(String.format(Locale.ROOT, "%,d", change.levelsAdvanced())).append("階層）。");
        }
        if (capped) {
            text.append("（留守の分は 8 時間まで）");
        }
        return Optional.of(text.toString());
    }
}
