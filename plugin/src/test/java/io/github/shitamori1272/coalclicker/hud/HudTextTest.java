package io.github.shitamori1272.coalclicker.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HudTextTest {

    private static PlayerData data() {
        return new PlayerData(UUID.randomUUID());
    }

    @Test
    void 企画書の例の形で出す() {
        PlayerData d = data();
        d.setCoal(BigNum.of(1234));
        // 階層 1 は累計 6,400〜19,200。12,416 は進み具合 0.47 → 80m × 0.47 = 37.6m → 37m
        d.setTotalMined(BigNum.of(12_416));
        assertEquals("石炭 1,234個 ｜ 毎秒 0個 ｜ 第2坑道 深さ200m 37/80m", HudText.actionBar(d));
    }

    @Test
    void 毎秒は道具の合計に深さ倍率を掛けたもの() {
        PlayerData d = data();
        d.setToolCount("lamp", 3);
        d.setCoal(BigNum.of(5));
        assertEquals("石炭 5個 ｜ 毎秒 3個 ｜ 第1坑道 深さ100m 0/64m", HudText.actionBar(d));
        d.setTotalMined(BigNum.of(6_400)); // 階層 1 → ×1.1
        assertEquals("石炭 5個 ｜ 毎秒 3.3個 ｜ 第2坑道 深さ200m 0/80m", HudText.actionBar(d));
    }

    @Test
    void ケージ待ちのときは数値上の階層のままヒントを付ける() {
        PlayerData d = data();
        d.setTotalMined(BigNum.of(44_800)); // 階層 3
        assertEquals("石炭 0個 ｜ 毎秒 0個 ｜ 第4坑道 深さ400m 0/112m ｜ ケージで降りよう", HudText.actionBar(d, true));
        assertEquals("石炭 0個 ｜ 毎秒 0個 ｜ 第4坑道 深さ400m 0/112m", HudText.actionBar(d, false));
    }

    @Test
    void 第1坑道の初期状態() {
        assertEquals("石炭 0個 ｜ 毎秒 0個 ｜ 第1坑道 深さ100m 0/64m", HudText.actionBar(data()));
    }

    @Test
    void 大きな値でもあふれない() {
        assertEquals("第9,223,372,036,854,775,808坑道", HudText.tunnelName(Long.MAX_VALUE));
        assertEquals("922,337,203,685,477,580,800", HudText.depth(Long.MAX_VALUE));
        String progress = HudText.progressText(Long.MAX_VALUE, 0.5);
        assertTrue(progress.endsWith("垓m"), progress);

        PlayerData d = data();
        d.setCoal(BigNum.of(1, 100));
        d.setToolCount("headframe", Long.MAX_VALUE);
        d.setTotalMined(BigNum.of(1, 5000)); // 階層 16,600 ほど
        String text = HudText.actionBar(d);
        assertTrue(text.startsWith("石炭 1×10^100個"), text);
        assertTrue(text.contains("坑道 深さ"), text);
    }

    @Test
    void 記録の一覧() {
        PlayerData d = data();
        d.setCoal(BigNum.of(500));
        d.setTotalMined(BigNum.of(32_000)); // 階層 2（19,200〜44,800）の半分
        d.setToolCount("blasting", 4);
        d.setToolCount("lamp", 2);
        d.addUpgrade("lamp_1"); // 安全灯 ×2
        d.addUpgrade("unknown_upgrade"); // 知らない ID は数えない
        d.setClicks(1_234);
        d.mine().setMaxReachedLevel(3);
        List<HudText.Entry> lines = HudText.record(d);
        assertEquals(new HudText.Entry("現在の石炭", "500個"), lines.get(0));
        assertEquals(new HudText.Entry("累計採掘量", "3.2万個"), lines.get(1));
        // (4 × 80 + 2 × 1 × 2) × 1.1^2 = 392.04
        assertEquals(new HudText.Entry("毎秒の石炭", "392個"), lines.get(2));
        assertEquals(new HudText.Entry("手掘り 1 回", "10個"), lines.get(3));
        assertEquals(new HudText.Entry("手掘りの回数", "1,234回"), lines.get(4));
        assertEquals(new HudText.Entry("今の坑道", "第3坑道・深さ300m・48/96m・次の坑道まであと1.28万個"), lines.get(5));
        assertEquals(new HudText.Entry("最深の坑道", "第4坑道（深さ400m）"), lines.get(6));
        // 2 × 1 × 2 × 1.21 = 4.84 → 4.8、4 × 80 × 1.21 = 387.2 → 387（有効数字で切り捨て）
        assertEquals(new HudText.Entry("安全灯", "2個（毎秒 4.8個）"), lines.get(7));
        assertEquals(new HudText.Entry("発破", "4個（毎秒 387個）"), lines.get(9));
        assertEquals(new HudText.Entry("立坑櫓", "0個（毎秒 0個）"), lines.get(14));
        assertEquals(new HudText.Entry("改良", "1/64"), lines.get(15));
        assertEquals(7 + 8 + 1, lines.size());
    }

    @Test
    void 最深の坑道は今の階層より浅くならない() {
        PlayerData d = data();
        d.setTotalMined(BigNum.of(44_800)); // 階層 3 になったばかり
        List<HudText.Entry> lines = HudText.record(d);
        assertEquals(new HudText.Entry("最深の坑道", "第4坑道（深さ400m）"), lines.get(6));
        assertEquals(new HudText.Entry("今の坑道", "第4坑道・深さ400m・0/112m・次の坑道まであと5.119万個"), lines.get(5));
    }

    @Test
    void 手掘り1回はツルハシと熟練の改良を反映する() {
        PlayerData d = data();
        d.setToolCount("conveyor", 1); // 毎秒 2,600
        d.addUpgrade("pickaxe_1"); // 10 × 2 = 20
        d.addUpgrade("skill_1"); // ＋ 2,600 × 0.01 = 26
        List<HudText.Entry> lines = HudText.record(d);
        assertEquals(new HudText.Entry("毎秒の石炭", "2,600個"), lines.get(2));
        assertEquals(new HudText.Entry("手掘り 1 回", "46個"), lines.get(3));
        assertEquals(new HudText.Entry("改良", "2/64"), lines.get(15));
    }

    @Test
    void 初期状態の記録() {
        List<HudText.Entry> lines = HudText.record(data());
        assertEquals(new HudText.Entry("手掘りの回数", "0回"), lines.get(4));
        assertEquals(new HudText.Entry("今の坑道", "第1坑道・深さ100m・0/64m・次の坑道まであと6,400個"), lines.get(5));
        assertEquals(new HudText.Entry("最深の坑道", "第1坑道（深さ100m）"), lines.get(6));
        assertEquals(new HudText.Entry("改良", "0/64"), lines.get(15));
    }

    @Test
    void 次の坑道までの残りは0未満にしない() {
        assertEquals(BigNum.ZERO, HudText.untilNextLevel(BigNum.of(1, 1000), 0));
        assertEquals(BigNum.of(500), HudText.untilNextLevel(BigNum.of(5_900), 0));
    }
}
