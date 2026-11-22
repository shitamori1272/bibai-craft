package io.github.shitamori1272.coalclicker.ranking;

import io.github.shitamori1272.coalclicker.data.DataStore;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scheduler.BukkitTask;

/**
 * サイドバーにサーバー全体の出炭量を出す（企画書 7 章）。1 秒ごとに更新する。
 *
 * <p>このプラグイン専用のスコアボードを作り、メインのスコアボードを見ているプレイヤーにだけ割り当てる。
 * ほかのプラグインが別のスコアボードを割り当てていれば上書きしない。
 * 専用ボードを見ている間は、メインのスコアボードのチームや表示（/team・/scoreboard）は見えない。
 * プラグイン停止時に、専用ボードを見ている人をメインのスコアボードへ戻す。
 */
public final class Sidebar implements Listener, Runnable {

    private static final long PERIOD_TICKS = 20L;
    /** 行の中身は customName で出すので、エントリ名はプレイヤーと被らない固定の文字列にする。 */
    private static final String ENTRY = "#coal-total";

    private final DataStore dataStore;
    private final Scoreboard board;
    private final Score line;
    private String lastText;
    private BukkitTask task;

    private Sidebar(DataStore dataStore) {
        this.dataStore = dataStore;
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("coalclicker", Criteria.DUMMY,
                Component.text(SidebarText.TITLE, NamedTextColor.GOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // 右側の点数（数字）を出さない
        objective.numberFormat(NumberFormat.blank());
        this.line = objective.getScore(ENTRY);
        line.setScore(0);
    }

    /** 表示を始める。onEnable から呼ぶ。止めるときは {@link #stop()}。 */
    public static Sidebar start(JavaPlugin plugin, DataStore dataStore) {
        Sidebar sidebar = new Sidebar(dataStore);
        sidebar.run();
        for (Player player : Bukkit.getOnlinePlayers()) {
            sidebar.show(player);
        }
        Bukkit.getPluginManager().registerEvents(sidebar, plugin);
        sidebar.task = Bukkit.getScheduler().runTaskTimer(plugin, sidebar, PERIOD_TICKS, PERIOD_TICKS);
        return sidebar;
    }

    /** 更新を止め、専用ボードを見ている人をメインのスコアボードへ戻す。onDisable から呼ぶ。 */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboard() == board) {
                player.setScoreboard(main);
            }
        }
    }

    @Override
    public void run() {
        String text = SidebarText.serverTotal(dataStore.server().serverTotalMined());
        if (!text.equals(lastText)) {
            line.customName(Component.text(text, NamedTextColor.WHITE));
            lastText = text;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        show(event.getPlayer());
    }

    private void show(Player player) {
        // ほかのプラグインが自分のスコアボードを割り当てていれば、そちらを優先する
        if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
            player.setScoreboard(board);
        }
    }
}
