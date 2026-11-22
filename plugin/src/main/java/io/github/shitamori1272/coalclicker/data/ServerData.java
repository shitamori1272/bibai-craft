package io.github.shitamori1272.coalclicker.data;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Objects;

/** サーバー全体の保存データ（{@code mine.yml}）。サーバーのメインスレッドからだけ触ること。 */
public final class ServerData {

    private int nextSection;
    private BigNum serverTotalMined = BigNum.ZERO;
    private boolean dirty;

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

    /** 新しい区画番号を払い出し、次の番号へ進める。 */
    public int allocateSection() {
        int section = nextSection;
        nextSection = Math.addExact(nextSection, 1);
        dirty = true;
        return section;
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
