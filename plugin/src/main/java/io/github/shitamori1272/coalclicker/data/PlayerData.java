package io.github.shitamori1272.coalclicker.data;

import io.github.shitamori1272.coalclicker.game.GameState;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * プレイヤー 1 人分の保存データ（企画書 8 章）。道具・改良は文字列の ID（例: {@code "lamp"}・{@code "lamp_1"}）で持つ。
 * 計算式からは {@link GameState} として読む。数値上の階層と進み具合は累計採掘量から計算するので持たない。
 *
 * <p>値を変えると {@link #isDirty()} が true になり、次の自動保存で書き出される。
 * サーバーのメインスレッドからだけ触ること。
 */
public final class PlayerData implements GameState {

    private final UUID uuid;
    private BigNum coal = BigNum.ZERO;
    private BigNum totalMined = BigNum.ZERO;
    private long clicks;
    private final Map<String, Long> tools = new LinkedHashMap<>();
    private final Set<String> upgrades = new LinkedHashSet<>();
    private final Set<String> explained = new LinkedHashSet<>();
    private final MineState mine = new MineState(this::markDirty);
    private long lastSeen;
    private boolean dirty;

    /** 初期値（石炭 0・道具と改良なし・区画未割り当て）のデータを作る。 */
    public PlayerData(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
    }

    /** 道具・改良・解説済み・坑道まで含めた独立した写しを作る。写しは保存不要（dirty でない）状態。 */
    public PlayerData copy() {
        PlayerData copy = new PlayerData(uuid);
        copy.coal = coal;
        copy.totalMined = totalMined;
        copy.clicks = clicks;
        copy.tools.putAll(tools);
        copy.upgrades.addAll(upgrades);
        copy.explained.addAll(explained);
        copy.mine.copyFrom(mine);
        copy.lastSeen = lastSeen;
        copy.dirty = false;
        return copy;
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
    @Override
    public BigNum totalMined() {
        return totalMined;
    }

    public void setTotalMined(BigNum totalMined) {
        this.totalMined = Objects.requireNonNull(totalMined);
        markDirty();
    }

    /** 手掘りの回数（ツルハシの改良の出現条件）。 */
    @Override
    public long clicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        if (clicks < 0) {
            throw new IllegalArgumentException("手掘りの回数は 0 以上にしてください: " + clicks);
        }
        this.clicks = clicks;
        markDirty();
    }

    /** 手掘りの回数を 1 増やす（long の上限で止める）。 */
    public void incrementClicks() {
        if (clicks < Long.MAX_VALUE) {
            setClicks(clicks + 1);
        }
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
    @Override
    public Map<String, Long> tools() {
        return Collections.unmodifiableMap(tools);
    }

    /** 改良を買ってあるか。 */
    public boolean hasUpgrade(String id) {
        return upgrades.contains(id);
    }

    /** 改良を買ったことにする。はじめて記録したときだけ true を返す。 */
    public boolean addUpgrade(String id) {
        boolean added = upgrades.add(Objects.requireNonNull(id));
        if (added) {
            markDirty();
        }
        return added;
    }

    /** 買った改良をすべて消す（管理コマンドの初期化用）。 */
    public void clearUpgrades() {
        if (!upgrades.isEmpty()) {
            upgrades.clear();
            markDirty();
        }
    }

    /** 買った改良の ID の一覧（読み取り専用。買った順）。 */
    @Override
    public Set<String> upgrades() {
        return Collections.unmodifiableSet(upgrades);
    }

    /** 解説を表示済みか（道具か改良の ID）。 */
    public boolean isExplained(String id) {
        return explained.contains(id);
    }

    /** 解説を表示済みにする（道具か改良の ID）。はじめて記録したときだけ true を返す。 */
    public boolean markExplained(String id) {
        boolean added = explained.add(Objects.requireNonNull(id));
        if (added) {
            markDirty();
        }
        return added;
    }

    /** 解説の表示済みをすべて消す（管理コマンドの初期化用）。 */
    public void clearExplained() {
        if (!explained.isEmpty()) {
            explained.clear();
            markDirty();
        }
    }

    /** 解説を表示済みの道具・改良の ID の一覧（読み取り専用）。 */
    public Set<String> explained() {
        return Collections.unmodifiableSet(explained);
    }

    /** 坑道（区画・到達ボーナスを受け取った階層）。 */
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
