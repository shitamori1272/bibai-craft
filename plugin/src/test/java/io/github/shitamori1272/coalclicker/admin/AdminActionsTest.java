package io.github.shitamori1272.coalclicker.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminActionsTest {

    @Test
    void 量を読む() {
        assertEquals(BigNum.of(1, 100), AdminActions.parseAmount("1e100").orElseThrow());
        assertEquals(BigNum.of(1000), AdminActions.parseAmount("1000").orElseThrow());
        assertEquals(BigNum.of(2.5, 3000), AdminActions.parseAmount("2.5E3000").orElseThrow());
    }

    @Test
    void ゼロ以下や読めない量は受け付けない() {
        assertTrue(AdminActions.parseAmount("0").isEmpty());
        assertTrue(AdminActions.parseAmount("-5").isEmpty());
        assertTrue(AdminActions.parseAmount("abc").isEmpty());
        assertTrue(AdminActions.parseAmount("").isEmpty());
        assertTrue(AdminActions.parseAmount("NaN").isEmpty());
        assertTrue(AdminActions.parseAmount("Infinity").isEmpty());
    }

    @Test
    void 付与は今の石炭だけ増やす() {
        PlayerData d = new PlayerData(UUID.randomUUID());
        d.setCoal(BigNum.of(10));
        assertEquals(BigNum.of(110), AdminActions.give(d, BigNum.of(100)).orElseThrow());
        assertEquals(BigNum.of(110), d.coal());
        assertTrue(d.totalMined().isZero());
    }

    @Test
    void 大きすぎる付与は断り_データを変えない() {
        PlayerData d = new PlayerData(UUID.randomUUID());
        BigNum huge = AdminActions.parseAmount("9e" + Long.MAX_VALUE).orElseThrow();
        assertEquals(huge, AdminActions.give(d, huge).orElseThrow());
        assertTrue(AdminActions.give(d, huge).isEmpty());
        assertEquals(huge, d.coal());
    }

    @Test
    void 初期化しても区画は残す() {
        PlayerData d = new PlayerData(UUID.randomUUID());
        d.setCoal(BigNum.of(1, 50));
        d.setTotalMined(BigNum.of(1, 60));
        d.setClicks(320);
        d.setToolCount("lamp", 10);
        d.addUpgrade("lamp_1");
        d.markExplained("lamp");
        d.mine().setSection(7);
        d.mine().setMaxReachedLevel(12);

        AdminActions.reset(d);

        assertTrue(d.coal().isZero());
        assertTrue(d.totalMined().isZero());
        assertTrue(d.tools().isEmpty());
        assertTrue(d.explained().isEmpty());
        assertEquals(7, d.mine().section());
        assertEquals(0, d.clicks());
        assertTrue(d.upgrades().isEmpty());
        assertEquals(0, d.mine().maxReachedLevel());
        assertTrue(d.isDirty());
    }
}
