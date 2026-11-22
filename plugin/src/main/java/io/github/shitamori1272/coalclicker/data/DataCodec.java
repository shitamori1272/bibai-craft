package io.github.shitamori1272.coalclicker.data;

import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 保存データと YAML の形をした {@code Map} との変換。Bukkit に依存しないので単体テストできる。
 *
 * <p>形は企画書 8 章のとおり。BigNum は {@link BigNum#toString()} の文字列で持つ。
 * 読み込みでは、ない項目は初期値にする。数値は数値・文字列のどちらでも読めるが、
 * 読めない値・範囲外の値・整数でない値は丸めずに {@link IllegalArgumentException} を投げる（壊れたファイルとして扱う）。
 *
 * <p>古い形式（ルール改定 #57・#58 より前）のファイルも読める。{@code mine.level}・{@code mine.progress} は
 * 累計採掘量から計算するようになったので読み飛ばし、今はない道具（{@code pickaxe}・{@code drill} など）や
 * 知らない改良の ID は捨てる。書き出すときは新しい形にする。
 */
public final class DataCodec {

    private DataCodec() {
    }

    /** {@code players/<UUID>.yml} の中身にする。 */
    public static Map<String, Object> encodePlayer(PlayerData data) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("coal", data.coal().toString());
        root.put("totalMined", data.totalMined().toString());
        root.put("clicks", data.clicks());
        root.put("tools", new LinkedHashMap<>(data.tools()));
        root.put("upgrades", new ArrayList<>(data.upgrades()));
        root.put("explained", new ArrayList<>(data.explained()));
        MineState mine = data.mine();
        Map<String, Object> mineMap = new LinkedHashMap<>();
        mineMap.put("section", mine.section());
        mineMap.put("maxReachedLevel", mine.maxReachedLevel());
        root.put("mine", mineMap);
        root.put("lastSeen", data.lastSeen());
        return root;
    }

    /**
     * {@code players/<UUID>.yml} の中身から読み込む。返す値は保存不要（dirty でない）状態。
     *
     * @throws IllegalArgumentException 値が不正なとき
     */
    public static PlayerData decodePlayer(UUID uuid, Map<String, ?> root) {
        PlayerData data = new PlayerData(uuid);
        data.setCoal(bigNum(root, "coal"));
        data.setTotalMined(bigNum(root, "totalMined"));
        data.setClicks(nonNegativeLong(root, "clicks", "clicks"));
        Map<?, ?> tools = map(root, "tools");
        if (tools != null) {
            tools.forEach((id, count) -> {
                long n = exactLong(count, "tools." + id);
                if (n < 0) {
                    throw new IllegalArgumentException("tools." + id + " が負の数です: " + n);
                }
                // 今はない道具（古い形式の pickaxe・drill など）は捨てる
                if (Tool.byId(String.valueOf(id)).isPresent()) {
                    data.setToolCount(String.valueOf(id), n);
                }
            });
        }
        for (Object id : list(root, "upgrades")) {
            // 知らない改良は捨てる
            if (Upgrade.byId(String.valueOf(id)).isPresent()) {
                data.addUpgrade(String.valueOf(id));
            }
        }
        for (Object id : list(root, "explained")) {
            data.markExplained(String.valueOf(id));
        }
        Map<?, ?> mineMap = map(root, "mine");
        if (mineMap != null) {
            MineState mine = data.mine();
            Object section = mineMap.get("section");
            if (section != null) {
                int s = exactInt(section, "mine.section");
                if (s < MineState.NO_SECTION) {
                    throw new IllegalArgumentException("mine.section が不正です: " + s);
                }
                mine.setSection(s);
            }
            // 古い形式の mine.level・mine.progress は累計採掘量から計算するようになったので読み飛ばす
            mine.setMaxReachedLevel(nonNegativeLong(mineMap, "maxReachedLevel", "mine.maxReachedLevel"));
        }
        Object lastSeen = root.get("lastSeen");
        if (lastSeen != null) {
            data.setLastSeen(exactLong(lastSeen, "lastSeen"));
        }
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

    /**
     * {@code mine.yml} の中身から読み込む。返す値は保存不要（dirty でない）状態。
     *
     * @throws IllegalArgumentException 値が不正なとき
     */
    public static ServerData decodeServer(Map<String, ?> root) {
        ServerData data = new ServerData();
        Object next = root.get("nextSection");
        if (next != null) {
            data.setNextSection(exactInt(next, "nextSection"));
        }
        data.setServerTotalMined(bigNum(root, "serverTotalMined"));
        data.clearDirty();
        return data;
    }

    private static Map<?, ?> map(Map<String, ?> root, String key) {
        Object value = root.get(key);
        if (value == null || value instanceof Map<?, ?>) {
            return (Map<?, ?>) value;
        }
        throw new IllegalArgumentException(key + " がマップではありません: " + value);
    }

    private static List<?> list(Map<String, ?> root, String key) {
        Object value = root.get(key);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException(key + " がリストではありません: " + value);
    }

    private static BigNum bigNum(Map<String, ?> root, String key) {
        Object value = root.get(key);
        try {
            return switch (value) {
                case null -> BigNum.ZERO;
                case Number n -> BigNum.of(finiteDouble(n, key));
                default -> BigNum.parse(value.toString());
            };
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(key + " が不正です: " + value, e);
        }
    }

    private static long nonNegativeLong(Map<?, ?> map, String key, String label) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        long n = exactLong(value, label);
        if (n < 0) {
            throw new IllegalArgumentException(label + " が負の数です: " + n);
        }
        return n;
    }

    private static int exactInt(Object value, String label) {
        long n = exactLong(value, label);
        if (n < Integer.MIN_VALUE || n > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(label + " が範囲外です: " + value);
        }
        return (int) n;
    }

    /** 整数として正確に表せる値だけを long にする。小数部がある・範囲外・数でない値は例外。 */
    static long exactLong(Object value, String label) {
        try {
            return switch (value) {
                case Long n -> n;
                case Integer n -> n;
                case Short n -> n;
                case Byte n -> n;
                case BigInteger n -> n.longValueExact();
                case Number n -> new BigDecimal(n.toString()).longValueExact();
                default -> new BigDecimal(value.toString().trim()).longValueExact();
            };
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(label + " が整数ではないか範囲外です: " + value, e);
        }
    }

    private static double finiteDouble(Object value, String label) {
        double d;
        try {
            d = value instanceof Number n ? n.doubleValue() : Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " が数ではありません: " + value, e);
        }
        if (!Double.isFinite(d)) {
            throw new IllegalArgumentException(label + " が有限の数ではありません: " + value);
        }
        return d;
    }
}
