package io.github.shitamori1272.coalclicker.dig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * 叩いたところに浮かんで消える小さな文字（{@code +◯個}・ケージへの一言）。
 *
 * <p>アクションバーは 1 秒ごとの状況表示（hud）が使い、タイトル・サブタイトルはケージで降りたときや到達ボーナスが使う。
 * 1 秒に最大 20 回出る {@code +◯個} でそれらを上書きしないよう、叩いた切羽の手前に {@link TextDisplay} を出す。
 * 本人にだけ見せ（{@link Display#setVisibleByDefault}）、保存せず（{@link Display#setPersistent}）、
 * 少し上へ動かしてから {@link #LIFETIME_TICKS} tick で消す。サーバーのメインスレッドから使うこと。
 */
final class HitPopup {

    /** 出してから消すまで（tick）。 */
    static final int LIFETIME_TICKS = 14;
    /** 消えるまでに上へ動く距離（ブロック）。 */
    private static final float RISE = 0.6f;
    /** 文字の大きさ。 */
    private static final float SCALE = 0.6f;
    /** 叩いたブロックの中心から、プレイヤーの方へ出す距離（ブロック）。 */
    private static final double TOWARD_PLAYER = 0.75;
    /** 出す位置のばらつき（ブロック）。続けて出ても重ならないようにする。 */
    private static final double JITTER = 0.25;

    private final Plugin plugin;
    private final Set<TextDisplay> alive = new HashSet<>();

    HitPopup(Plugin plugin) {
        this.plugin = plugin;
    }

    /** blockCenter（叩いたブロックの中心）の player 側に text を出す。 */
    void show(Player player, Location blockCenter, Component text) {
        Location eye = player.getEyeLocation();
        Location at = blockCenter.clone();
        org.bukkit.util.Vector toward = eye.toVector().subtract(blockCenter.toVector());
        if (toward.lengthSquared() > 1e-6) {
            at.add(toward.normalize().multiply(TOWARD_PLAYER));
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        at.add(0, random.nextDouble(-JITTER, JITTER), random.nextDouble(-JITTER, JITTER));
        TextDisplay display = at.getWorld().spawn(at, TextDisplay.class, d -> {
            d.setPersistent(false);
            d.setVisibleByDefault(false);
            d.text(text);
            d.setBillboard(Display.Billboard.CENTER);
            d.setShadowed(true);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            d.setBrightness(new Display.Brightness(15, 15));
            d.setTransformation(transformation(0));
        });
        player.showEntity(plugin, display);
        alive.add(display);
        // 次の tick から上へ動かす（出した tick に変えると補間されない）
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (display.isValid()) {
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(LIFETIME_TICKS - 1);
                display.setTransformation(transformation(RISE));
            }
        }, 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> remove(display), LIFETIME_TICKS);
    }

    /** 出ている文字をすべて消す。プラグインを止めるときに呼ぶ。 */
    void clearAll() {
        for (TextDisplay display : List.copyOf(alive)) {
            remove(display);
        }
    }

    private void remove(TextDisplay display) {
        alive.remove(display);
        if (display.isValid()) {
            display.remove();
        }
    }

    private static Transformation transformation(float dy) {
        return new Transformation(new Vector3f(0, dy, 0), new AxisAngle4f(),
                new Vector3f(SCALE, SCALE, SCALE), new AxisAngle4f());
    }
}
