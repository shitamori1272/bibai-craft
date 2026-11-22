package io.github.shitamori1272.coalclicker.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.List;
import java.util.Map;
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
        d.mine().setLevel(1);
        d.mine().setProgress(0.47); // 80m × 0.47 = 37.6m → 37m
        assertEquals("石炭 1,234個 ｜ 毎秒 0個 ｜ 第2坑道 深さ200m 37/80m", HudText.actionBar(d));
    }

    @Test
    void 毎秒は掘進速度と1mあたりの石炭の積() {
        // D = 1 → 速度 0.1m/秒、E = 100、階層 0 → 毎秒 10 個
        assertEquals(BigNum.of(10).toDouble(), HudText.coalPerSecond(Map.of("blasting", 1L), 0).toDouble(), 1e-9);
        // 階層 1 では ×1.1
        assertEquals(11, HudText.coalPerSecond(Map.of("blasting", 1L), 1).toDouble(), 1e-9);
        assertTrue(HudText.coalPerSecond(Map.of(), 5).isZero());
    }

    @Test
    void ケージ待ちのときは数値上の階層のままヒントを付ける() {
        PlayerData d = data();
        d.mine().setLevel(3);
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
        d.setToolCount("drill", Long.MAX_VALUE);
        d.mine().setLevel(1_000_000_000L);
        String text = HudText.actionBar(d);
        assertTrue(text.startsWith("石炭 1×10^100個"), text);
        assertTrue(text.contains("第1,000,000,001坑道 深さ100,000,000,100m"), text);
    }

    @Test
    void 記録の一覧() {
        PlayerData d = data();
        d.setCoal(BigNum.of(500));
        d.setTotalMined(BigNum.of(12345));
        d.setToolCount("blasting", 4);
        d.setToolCount("lamp", 2);
        d.mine().setLevel(2);
        d.mine().setProgress(0.5);
        d.mine().setMaxReachedLevel(3);
        List<HudText.Entry> lines = HudText.record(d);
        assertEquals(new HudText.Entry("現在の石炭", "500個"), lines.get(0));
        assertEquals(new HudText.Entry("累計採掘量", "1.234万個"), lines.get(1));
        assertEquals(new HudText.Entry("掘進力 D", "4"), lines.get(2));
        assertEquals(new HudText.Entry("採炭力 E", "120"), lines.get(3));
        assertEquals(new HudText.Entry("掘進速度", "0.2m/秒"), lines.get(4));
        assertEquals(new HudText.Entry("今の坑道", "第3坑道・深さ300m・48/96m"), lines.get(6));
        assertEquals(new HudText.Entry("最深の坑道", "第4坑道"), lines.get(7));
        assertTrue(lines.contains(new HudText.Entry("安全灯", "2個")));
        assertTrue(lines.contains(new HudText.Entry("ツルハシ", "0個")));
        assertEquals(8 + 6, lines.size());
    }
}
