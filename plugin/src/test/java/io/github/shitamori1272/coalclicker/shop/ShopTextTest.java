package io.github.shitamori1272.coalclicker.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.game.GameState;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.game.Upgrade;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShopTextTest {

    private static GameState state(Map<String, Long> tools, Set<String> upgrades, long clicks, double totalMined) {
        return GameState.of(tools, upgrades, clicks, BigNum.of(totalMined));
    }

    private static List<String> plain(List<ShopText.Line> lines) {
        return lines.stream().map(ShopText.Line::plain).toList();
    }

    @Test
    void 割合() {
        assertEquals("0%", ShopText.percent(BigNum.ZERO, BigNum.ZERO));
        assertEquals("0%", ShopText.percent(BigNum.ZERO, BigNum.of(10)));
        assertEquals("100%", ShopText.percent(BigNum.of(10), BigNum.of(10)));
        assertEquals("50%", ShopText.percent(BigNum.of(5), BigNum.of(10)));
        assertEquals("33.3%", ShopText.percent(BigNum.of(1), BigNum.of(3)));
        // 切り捨て
        assertEquals("66.6%", ShopText.percent(BigNum.of(2), BigNum.of(3)));
        assertEquals("12.3%", ShopText.percent(BigNum.of(123), BigNum.of(1000)));
        assertEquals("0.1% 未満", ShopText.percent(BigNum.of(1), BigNum.of(1e6)));
        assertEquals("99.9%", ShopText.percent(BigNum.of(9999), BigNum.of(10000)));
        // とても大きな数でも
        assertEquals("25%", ShopText.percent(BigNum.of(1, 500), BigNum.of(4, 500)));
    }

    @Test
    void 道具の説明文() {
        // 安全灯 10 個（毎秒 10）と炭車 1 個（毎秒 10）、安全灯の改良 I（安全灯 ×2）
        GameState s = state(Map.of("lamp", 10L, "minecart", 1L), Set.of("lamp_1"), 0, 0);
        List<ShopText.Line> lore = ShopText.toolLore(Tool.LAMP, s, BigNum.of(100));
        List<String> text = plain(lore);
        // 150 × 1.15^10 ≈ 606.8
        assertEquals("価格：606 個", text.get(0));
        assertEquals(ShopText.Tone.UNAFFORDABLE, lore.get(0).segments().get(1).tone());
        assertEquals("所持数：10 個", text.get(1));
        assertEquals("1 つ買うと毎秒 +2 個", text.get(2));
        // 安全灯 20・炭車 10 → 全体 30 のうち 66.6%
        assertEquals("この道具の合計：毎秒 20 個・全体の 66.6%", text.get(3));
        assertTrue(text.contains(Tool.LAMP.shortDescription()));
        assertTrue(text.contains("クリックで 1 つ購入"));
        assertTrue(text.getLast().startsWith("Shift+クリックで買えるだけ（最大 10 個）"));
    }

    @Test
    void 写真の行は写真のある道具だけに出る() {
        GameState s = state(Map.of(), Set.of(), 0, 0);
        for (Tool tool : Tool.values()) {
            boolean hasPhotoLine = ShopText.toolLore(tool, s, BigNum.ZERO).stream()
                    .flatMap(l -> l.segments().stream())
                    .anyMatch(seg -> seg.tone() == ShopText.Tone.PHOTO);
            assertEquals(ToolPhoto.of(tool).isPresent(), hasPhotoLine, tool.id());
        }
    }

    @Test
    void 写真のある道具は写真の下に場所を空ける() {
        GameState s = state(Map.of(), Set.of(), 0, 0);
        List<ShopText.Line> lore = ShopText.toolLore(Tool.DRUM_CUTTER, s, BigNum.ZERO);
        int photoRow = -1;
        for (int i = 0; i < lore.size(); i++) {
            if (!lore.get(i).segments().isEmpty() && lore.get(i).segments().getFirst().tone() == ShopText.Tone.PHOTO) {
                photoRow = i;
            }
        }
        assertTrue(photoRow >= 0);
        // 左上・詰め・右上・行頭へ・左下・詰め・右下
        assertEquals("", lore.get(photoRow).plain());
        // 高さ 80 → 写真の行を含めて 8 行ぶん＋余白 1 行が空で、その次が解説
        for (int i = 1; i <= ToolPhoto.rows(); i++) {
            assertEquals("", lore.get(photoRow + i).plain());
        }
        assertEquals(8, ToolPhoto.rows());
        assertTrue(lore.get(photoRow + ToolPhoto.rows() + 1).plain().startsWith("回転するドラム"));
    }

    @Test
    void 買える価格は黄色_まとめ買いの見込みも出る() {
        GameState s = state(Map.of(), Set.of(), 0, 0);
        List<ShopText.Line> lore = ShopText.toolLore(Tool.LAMP, s, BigNum.of(400));
        assertEquals(ShopText.Tone.AFFORDABLE, lore.get(0).segments().get(1).tone());
        assertEquals("価格：150 個", lore.get(0).plain());
        assertEquals("この道具の合計：毎秒 0 個・全体の 0%", lore.get(3).plain());
        // 150 + 172.5 = 322.5 で 2 個まで
        assertEquals("Shift+クリックで買えるだけ（最大 10 個）（今なら 2 個・322 個）", lore.getLast().plain());
    }

    @Test
    void 改良は安い順() {
        // 安全灯 1 個 → lamp_1（1,500）、手掘り 100 回 → pickaxe_1（1,000）
        GameState s = state(Map.of("lamp", 1L), Set.of(), 100, 0);
        assertEquals(List.of(Upgrade.byId("pickaxe_1").orElseThrow(), Upgrade.byId("lamp_1").orElseThrow()),
                ShopText.availableByPrice(s));
        // 買ったものは並ばない
        GameState bought = state(Map.of("lamp", 1L), Set.of("pickaxe_1"), 100, 0);
        assertEquals(List.of(Upgrade.byId("lamp_1").orElseThrow()), ShopText.availableByPrice(bought));
    }

    @Test
    void 改良の説明文() {
        Upgrade lamp1 = Upgrade.byId("lamp_1").orElseThrow();
        List<ShopText.Line> lore = ShopText.upgradeLore(lamp1, BigNum.of(2_000));
        List<String> text = plain(lore);
        assertEquals("効果：安全灯の効果 ×2", text.get(0));
        assertEquals("価格：1,500 個", text.get(1));
        assertEquals(ShopText.Tone.AFFORDABLE, lore.get(1).segments().get(1).tone());
        assertEquals("クリックで購入", text.getLast());
        assertEquals(lamp1.explanation(), String.join("", text.subList(3, text.size() - 2)));
        assertEquals(ShopText.Tone.UNAFFORDABLE,
                ShopText.upgradeLore(lamp1, BigNum.of(1_000)).get(1).segments().get(1).tone());
    }

    @Test
    void 改良の見出し() {
        assertEquals("まだ買える改良はありません", plain(ShopText.upgradeHeaderLore(0, 0)).getFirst());
        assertEquals(List.of("買える改良：3 個", "下に安い順に並んでいます（開き直すと並べ直します）"), plain(ShopText.upgradeHeaderLore(3, 0)));
        assertEquals("ページ：2 / 2", plain(ShopText.upgradeHeaderLore(20, 1)).get(1));
    }

    @Test
    void 次の改良のヒント() {
        // はじめは、いちばん安い pickaxe_1（1,000）
        assertEquals(Optional.of("手掘りをあと 100 回で次の改良"),
                ShopText.nextLockedHint(state(Map.of(), Set.of(), 0, 0)));
        // ツルハシ I が出たら、次に安いのは lamp_1（1,500）
        assertEquals(Optional.of("安全灯をあと 1 個で次の改良"),
                ShopText.nextLockedHint(state(Map.of(), Set.of(), 100, 0)));
        // 安全灯 3 個なら、次に安いのは lamp_2（7,500。minecart_1 は 1 万）
        assertEquals(Optional.of("安全灯をあと 2 個で次の改良"),
                ShopText.nextLockedHint(state(Map.of("lamp", 3L), Set.of(), 100, 0)));
        assertEquals("安全灯をあと 2 個で次の改良",
                ShopText.lockedHint(Upgrade.toolTier(Tool.LAMP, 2), state(Map.of("lamp", 3L), Set.of(), 0, 0)));
        assertEquals("累計採掘量があと 4万 個で次の改良",
                ShopText.lockedHint(Upgrade.byId("skill_1").orElseThrow(), state(Map.of(), Set.of(), 0, 10_000)));
    }

    @Test
    void すべて出ていればヒントはない() {
        Map<String, Long> tools = new java.util.HashMap<>();
        for (Tool tool : Tool.values()) {
            tools.put(tool.id(), 200L);
        }
        GameState s = state(tools, Set.of(), 10_000, 1e13);
        assertEquals(Optional.empty(), ShopText.nextLockedHint(s));
    }

    @Test
    void 折り返し() {
        assertEquals(List.of("短い"), ShopText.wrap("短い", 20));
        assertEquals(List.of("あいうえお", "かきくけこ", "さし"), ShopText.wrap("あいうえおかきくけこさし", 5));
        // 句読点があればその直後で折る
        assertEquals(List.of("あいう、", "えおかき"), ShopText.wrap("あいう、えおかき", 5));
        for (String row : ShopText.wrap(Tool.HEADFRAME.explanation(), ShopText.WRAP_WIDTH)) {
            assertTrue(row.length() <= ShopText.WRAP_WIDTH, row);
        }
        assertEquals(Tool.HEADFRAME.explanation(),
                String.join("", ShopText.wrap(Tool.HEADFRAME.explanation(), ShopText.WRAP_WIDTH)));
    }
}
