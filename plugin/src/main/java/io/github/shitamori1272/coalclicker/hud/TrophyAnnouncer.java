package io.github.shitamori1272.coalclicker.hud;

import io.github.shitamori1272.coalclicker.CoalClicker;
import io.github.shitamori1272.coalclicker.data.DataStore;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.game.Trophy;
import java.time.Duration;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * 累計採掘量の節目（{@link Trophy}）に届いた人にトロフィーを渡し、タイトルと効果音で知らせる。
 *
 * <p>手掘り・毎秒の石炭・到達ボーナス・ログアウト中の分など、どこで累計が増えても拾えるよう、
 * {@link #PERIOD_TICKS} ごとにオンラインの全員を見る。一度に複数の節目を越えたら（ログアウト中の分など）、
 * まとめて渡していちばん大きい節目だけ知らせる。パーティクルは出さない。サーバーのメインスレッドから使うこと。
 */
public final class TrophyAnnouncer {

    /** 見回りの間隔（tick）。 */
    private static final long PERIOD_TICKS = 10;

    private final DataStore dataStore;
    private final BukkitTask task;

    private TrophyAnnouncer(CoalClicker plugin) {
        this.dataStore = plugin.dataStore();
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, PERIOD_TICKS, PERIOD_TICKS);
    }

    /** 見回りを始める。onEnable から呼ぶ。 */
    public static TrophyAnnouncer start(CoalClicker plugin) {
        return new TrophyAnnouncer(plugin);
    }

    public void stop() {
        task.cancel();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            check(player);
        }
    }

    private void check(Player player) {
        PlayerData data = dataStore.get(player);
        List<Integer> reached = Trophy.newlyReached(data.totalMined(), data.upgrades());
        if (reached.isEmpty()) {
            return;
        }
        for (int digits : reached) {
            data.addUpgrade(Trophy.id(digits));
        }
        announce(player, reached.getLast(), reached.size(), Trophy.count(data.upgrades()));
    }

    private static void announce(Player player, int digits, int gained, int total) {
        String name = Trophy.milestoneName(digits);
        long bonus = Math.round(Trophy.BONUS_PER_TROPHY * 100 * total);
        String extra = gained > 1 ? "（" + gained + "個まとめて）" : "";
        player.showTitle(Title.title(
                Component.text("🏆 " + name + " 突破！", NamedTextColor.GOLD),
                Component.text("トロフィー " + total + "個 ・ 生産 +" + bonus + "%", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2500), Duration.ofMillis(600))));
        player.sendMessage(Component.text("🏆 累計 " + name + " 突破のトロフィーを手に入れました" + extra
                + "。毎秒の石炭と手掘りが +" + bonus + "% になりました。", NamedTextColor.GOLD));
        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1f);
    }
}
