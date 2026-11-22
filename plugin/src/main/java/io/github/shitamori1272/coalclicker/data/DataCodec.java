package io.github.shitamori1272.coalclicker.data;

import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 保存データと YAML の形をした {@code Map} との変換。Bukkit に依存しないので単体テストできる。
 *
 * <p>形は企画書 8 章のとおり。BigNum は {@link BigNum#toString()} の文字列で持つ。
 * 読み込みは手で編集したファイルも受け付けるよう、数値・文字列のどちらでも読める。
 */
public final class DataCodec {

    private DataCodec() {
    }

    /** {@code players/<UUID>.yml} の中身にする。 */
    public static Map<String, Object> encodePlayer(PlayerData data) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("coal", data.coal().toString());
        root.put("totalMined", data.totalMined().toString());
        root.put("tools", new LinkedHashMap<>(data.tools()));
        root.put("explained", new ArrayList<>(data.explained()));
        MineState mine = data.mine();
        Map<String, Object> mineMap = new LinkedHashMap<>();
        mineMap.put("section", mine.section());
        mineMap.put("level", mine.level());
        mineMap.put("progress", mine.progress());
        mineMap.put("maxReachedLevel", mine.maxReachedLevel());
        root.put("mine", mineMap);
        root.put("lastSeen", data.lastSeen());
        return root;
    }

    /** {@code players/<UUID>.yml} の中身から読み込む。ない項目は初期値になる。返す値は保存不要（dirty でない）状態。 */
    public static PlayerData decodePlayer(UUID uuid, Map<String, ?> root) {
        PlayerData data = new PlayerData(uuid);
        data.setCoal(bigNum(root.get("coal")));
        data.setTotalMined(bigNum(root.get("totalMined")));
        if (root.get("tools") instanceof Map<?, ?> tools) {
            tools.forEach((id, count) -> data.setToolCount(String.valueOf(id), longValue(count, 0)));
        }
        if (root.get("explained") instanceof List<?> explained) {
            explained.forEach(id -> data.markExplained(String.valueOf(id)));
        }
        if (root.get("mine") instanceof Map<?, ?> mineMap) {
            MineState mine = data.mine();
            mine.setSection((int) longValue(mineMap.get("section"), MineState.NO_SECTION));
            mine.setLevel(Math.max(0, longValue(mineMap.get("level"), 0)));
            mine.setProgress(doubleValue(mineMap.get("progress")));
            mine.setMaxReachedLevel(Math.max(0, longValue(mineMap.get("maxReachedLevel"), 0)));
        }
        data.setLastSeen(longValue(root.get("lastSeen"), 0));
        data.clearDirty();
        return data;
    }

    /** {@code mine.yml} の中身にする。 */
    public static Map<String, Object> encodeServer(ServerData data) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("nextSection", data.nextSection());
        root.put("serverTotalMined", data.serverTotalMined().toString());
        return root;
    }

    /** {@code mine.yml} の中身から読み込む。ない項目は初期値になる。返す値は保存不要（dirty でない）状態。 */
    public static ServerData decodeServer(Map<String, ?> root) {
        ServerData data = new ServerData();
        data.setNextSection((int) Math.max(0, longValue(root.get("nextSection"), 0)));
        data.setServerTotalMined(bigNum(root.get("serverTotalMined")));
        data.clearDirty();
        return data;
    }

    private static BigNum bigNum(Object value) {
        return switch (value) {
            case null -> BigNum.ZERO;
            case Number n -> BigNum.of(n.doubleValue());
            default -> BigNum.parse(value.toString());
        };
    }

    private static long longValue(Object value, long fallback) {
        return switch (value) {
            case null -> fallback;
            case Number n -> n.longValue();
            default -> Long.parseLong(value.toString().trim());
        };
    }

    private static double doubleValue(Object value) {
        return switch (value) {
            case null -> 0;
            case Number n -> n.doubleValue();
            default -> Double.parseDouble(value.toString().trim());
        };
    }
}
