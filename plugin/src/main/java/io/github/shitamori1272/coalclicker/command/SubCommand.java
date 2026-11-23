package io.github.shitamori1272.coalclicker.command;

import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * {@code /coal} のサブコマンド。{@link CoalCommand#register(SubCommand)} で登録する。
 *
 * <p>{@link #path()} は空白区切りの名前。{@code ""} は引数なしの {@code /coal}、{@code "shop"} は {@code /coal shop}、
 * {@code "admin give"} は {@code /coal admin give} に対応する。{@link #execute} には名前の後ろの引数だけが渡る。
 */
public interface SubCommand {

    /** サブコマンドの名前（例: {@code "shop"}、{@code "admin give"}）。 */
    String path();

    /** ヘルプに出す短い説明。 */
    String description();

    /** 引数の書き方（例: {@code "<player> <量>"}）。なければ空文字。 */
    default String usage() {
        return "";
    }

    /** OP（または {@code coalclicker.admin} 権限）だけが使えるか。 */
    default boolean adminOnly() {
        return false;
    }

    /** プレイヤーだけが使えるか（コンソールからは使えない）。 */
    default boolean playerOnly() {
        return true;
    }

    void execute(CommandSender sender, String[] args);

    /** タブ補完の候補。args は名前の後ろの引数（入力途中の最後の要素を含む）。 */
    default List<String> suggest(CommandSender sender, String[] args) {
        return List.of();
    }
}
