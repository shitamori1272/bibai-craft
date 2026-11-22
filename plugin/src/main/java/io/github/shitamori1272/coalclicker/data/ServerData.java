package io.github.shitamori1272.coalclicker.data;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** サーバー全体の保存データ（{@code mine.yml}）。サーバーのメインスレッドからだけ触ること。 */
public final class ServerData {

    private int nextSection;
    private BigNum serverTotalMined = BigNum.ZERO;
    private boolean dirty;
    /** プレイヤーのデータから分かっている、使用中の区画（保存しない）。 */
    private final Set<Integer> usedSections = new HashSet<>();

    /** 次に割り当てる区画番号。 */
    public int nextSection() {
        return nextSection;
    }

    public void setNextSection(int nextSection) {
        if (nextSection < 0) {
            throw new IllegalArgumentException("区画番号は 0 以上にしてください: " + nextSection);
        }
        this.nextSection = nextSection;
        dirty = true;
    }

    /** 新しい区画番号を払い出し、次の番号へ進める。使用中と分かっている区画は飛ばす。 */
    public int allocateSection() {
        int section = nextSection;
        while (usedSections.contains(section)) {
            section = Math.addExact(section, 1);
        }
        nextSection = Math.addExact(section, 1);
        usedSections.add(section);
        dirty = true;
        return section;
    }

    /**
     * 区画が使用中であることを記録する（保存はしない）。次に割り当てる番号がそれ以下なら、その次まで進める。
     * {@code mine.yml} がなくなったり壊れたりしても、区画を二重に割り当てないために使う。
     */
    public void reserveSection(int section) {
        if (section < 0) {
            return;
        }
        usedSections.add(section);
        if (nextSection <= section) {
            nextSection = Math.addExact(section, 1);
            dirty = true;
        }
    }

    /** サーバー全体の出炭量。 */
    public BigNum serverTotalMined() {
        return serverTotalMined;
    }

    public void setServerTotalMined(BigNum serverTotalMined) {
        this.serverTotalMined = Objects.requireNonNull(serverTotalMined);
        dirty = true;
    }

    /** サーバー全体の出炭量に amount を足す。 */
    public void addServerTotalMined(BigNum amount) {
        setServerTotalMined(serverTotalMined.add(amount));
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        dirty = true;
    }

    void clearDirty() {
        dirty = false;
    }
}
