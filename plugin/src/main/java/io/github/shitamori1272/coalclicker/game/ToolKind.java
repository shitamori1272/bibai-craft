package io.github.shitamori1272.coalclicker.game;

/** 道具の種類。どのステータスに効くかを表す（企画書 6 章）。 */
public enum ToolKind {
    /** 手掘り。切羽を崩したときの石炭が増える（ツルハシ）。 */
    MANUAL("手掘り"),
    /** 採炭。採炭力 E が上がる。 */
    COAL("採炭"),
    /** 掘進。掘進力 D が上がる。 */
    DIG("掘進");

    private final String displayName;

    ToolKind(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
