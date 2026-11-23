package io.github.shitamori1272.coalclicker.effect;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 鳴っている音を、ぶつっと切らずにフェードアウトさせる。
 *
 * <p>Minecraft では鳴り始めた音の音量を後から変えられないので、{@code interval} tick ごとに
 * 「止めて、少し小さい音量で鳴らし直す」を {@link FadeCurve} の段だけ繰り返し、最後に止める。
 * トロッコの走行音のような、ざらざらと続く音で使う（鳴らし直しの継ぎ目が目立たない）。
 *
 * <p>音はプレイヤーに付けて鳴らす（{@code playSound(Entity, ...)}）。テレポートしても置いていかれない。
 * サーバーのメインスレッドから使うこと。
 */
public final class SoundFade {

    private SoundFade() {
    }

    /** sound をプレイヤーに付けて鳴らす（{@link #fadeOut} と同じ鳴らし方）。 */
    public static void play(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player, sound, volume, pitch);
    }

    /**
     * 鳴っている sound を、今から steps × interval tick かけてフェードアウトさせる。
     *
     * @param volume いま鳴っている音量
     * @param pitch いま鳴っている高さ（鳴らし直しも同じ高さにする）
     */
    public static void fadeOut(Plugin plugin, Player player, Sound sound, float volume, float pitch, int steps,
            long interval) {
        UUID id = player.getUniqueId();
        for (int step = 1; step <= steps; step++) {
            float next = FadeCurve.volume(volume, step, steps);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player online = Bukkit.getPlayer(id);
                if (online == null || !online.isOnline()) {
                    return;
                }
                online.stopSound(sound);
                if (next > 0) {
                    play(online, sound, next, pitch);
                }
            }, step * interval);
        }
    }
}
