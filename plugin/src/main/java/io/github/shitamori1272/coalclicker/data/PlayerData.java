package io.github.shitamori1272.coalclicker.data;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * プレイヤー 1 人分の保存データ。道具はゲームのルールに依存しないよう、文字列の ID（例: {@code "pickaxe"}）で持つ。
 *
 * <p>値を変えると {@link #isDirty()} が true になり、次の自動保存で書き出される。
 * サーバーのメインスレッドからだけ触ること。
 */
public final class PlayerData {

    private final UUID uuid;
    private BigNum coal = BigNum.ZERO;
    private BigNum totalMined = BigNum.ZERO;
    private final Map<String, Long> tools = new LinkedHashMap<>();
    private final Set<String> explained = new LinkedHashSet<>();
    private final MineState mine = new MineState(this::markDirty);
    private long lastSeen;
    private boolean dirty;

    /** 初期値（石炭 0・道具なし・区画未割り当て）のデータを作る。 */
    public PlayerData(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
    }

    public UUID uuid() {
        return uuid;
    }

    /** 今持っている石炭。 */
    public BigNum coal() {
        return coal;
    }

    public void setCoal(BigNum coal) {
        this.coal = Objects.requireNonNull(coal);
        markDirty();
    }

    /** 累計採掘量。 */
    public BigNum totalMined() {
        return totalMined;
    }

    public void setTotalMined(BigNum totalMined) {
        this.totalMined = Objects.requireNonNull(totalMined);
        markDirty();
    }

    /** 道具の所持数。持っていなければ 0。 */
    public long getToolCount(String id) {
        return tools.getOrDefault(id, 0L);
    }

    /** 道具の所持数を設定する。0 以下にすると記録から消す。 */
    public void setToolCount(String id, long count) {
        Objects.requireNonNull(id);
        if (count <= 0) {
            tools.remove(id);
        } else {
            tools.put(id, count);
        }
        markDirty();
    }

    /** 道具を amount 個増やす。 */
    public void addTool(String id, long amount) {
        setToolCount(id, Math.addExact(getToolCount(id), amount));
    }

    /** 道具の所持数の一覧（読み取り専用）。 */
    public Map<String, Long> tools() {
        return Collections.unmodifiableMap(tools);
    }

    /** 解説を表示済みか。 */
    public boolean isExplained(String toolId) {
        return explained.contains(toolId);
    }

    /** 解説を表示済みにする。はじめて記録したときだけ true を返す。 */
    public boolean markExplained(String toolId) {
        boolean added = explained.add(Objects.requireNonNull(toolId));
        if (added) {
            markDirty();
        }
        return added;
    }

    /** 解説を表示済みの道具 ID の一覧（読み取り専用）。 */
    public Set<String> explained() {
        return Collections.unmodifiableSet(explained);
    }

    /** 坑道の進み具合。 */
    public MineState mine() {
        return mine;
    }

    /** 最後にいた時刻（エポック秒）。0 なら記録なし。保存のたびに更新される。 */
    public long lastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        markDirty();
    }

    /** 前回の保存から値が変わったか。 */
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
