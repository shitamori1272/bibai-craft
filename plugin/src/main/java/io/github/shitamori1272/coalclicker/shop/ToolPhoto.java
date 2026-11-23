package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.game.Tool;
import java.util.Optional;

/**
 * 道具の説明文に出す実物の写真（リソースパック {@code resourcepack/} のフォント {@code coalclicker:photo}）。
 *
 * <p>フォントの 1 文字は 256×256 の枠に入る大きさまでなので、写真は 2×2 の 4 文字に分けてある
 * （{@code resourcepack/make_photos.py} が作る）。左上・右上を並べ、マイナスの幅の文字で行の頭へ戻り、
 * 左下・右下を並べる。下の 2 枚はフォントの {@code ascent} で 1 段下に描かれる。
 *
 * <p>写真は説明文の 1 行目から下の行にはみ出して描かれるので、はみ出す分だけ空の行を入れて場所を空ける。
 * 説明文の 1 行は 10 ピクセルなので、高さ {@link #HEIGHT} の写真には {@code ceil(HEIGHT / 10)} 行ぶん要る。
 * 並び順・大きさを変えたら {@code make_photos.py} も合わせる。
 */
public enum ToolPhoto {
    DRUM_CUTTER(Tool.DRUM_CUTTER),
    HEADFRAME(Tool.HEADFRAME),
    LAMP(Tool.LAMP),
    MINECART(Tool.MINECART),
    COAL_PICK(Tool.COAL_PICK),
    SHIELD(Tool.SHIELD),
    CONVEYOR(Tool.CONVEYOR),
    BLASTING(Tool.BLASTING);

    /** フォントの名前空間とキー（{@code assets/coalclicker/font/photo.json}）。 */
    public static final String FONT_NAMESPACE = "coalclicker";
    public static final String FONT_KEY = "photo";

    /** 写真の高さ（説明文のピクセル。フォントの {@code height} と同じ）。 */
    static final int HEIGHT = 80;

    /** 説明文 1 行の高さ（ピクセル）。 */
    static final int LINE_HEIGHT = 10;

    /** 1 枚目の写真の左上の文字。写真ごとに 4 文字ずつ（左上・右上・左下・右下）。 */
    private static final char FIRST_TILE = '';

    /** 文字の後ろに入る 1 ピクセルの隙間を詰める（幅 -1）。 */
    private static final char CLOSE_GAP = '';

    /** 行の頭へ戻る（幅 -(写真の幅 + 1)）。 */
    private static final char BACK_TO_START = '';

    private final Tool tool;

    ToolPhoto(Tool tool) {
        this.tool = tool;
    }

    /** 写真を描く文字の並び。 */
    public String glyph() {
        char first = (char) (FIRST_TILE + ordinal() * 4);
        return new String(new char[] {
            first, CLOSE_GAP, (char) (first + 1), BACK_TO_START,
            (char) (first + 2), CLOSE_GAP, (char) (first + 3)
        });
    }

    /** 写真が占める行の数（写真の行を含む）。 */
    static int rows() {
        return (HEIGHT + LINE_HEIGHT - 1) / LINE_HEIGHT;
    }

    /** その道具の写真。まだ用意していなければ空。 */
    public static Optional<ToolPhoto> of(Tool tool) {
        for (ToolPhoto photo : values()) {
            if (photo.tool == tool) {
                return Optional.of(photo);
            }
        }
        return Optional.empty();
    }
}
