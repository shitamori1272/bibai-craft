package io.github.shitamori1272.coalclicker.dig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.DataCodec;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DigEngineTest {

    private static PlayerData player() {
        return new PlayerData(UUID.randomUUID());
    }

    @Test
    void 自動は毎秒の石炭を秒数ぶん足す() {
        PlayerData data = player();
        data.setToolCount(Tool.MINECART.id(), 3);
        ServerData server = new ServerData();
        DigEngine.Change change = DigEngine.autoTick(data, server, 1);
        assertEquals(30, change.coal().toDouble(), 1e-9);
        assertEquals(30, data.coal().toDouble(), 1e-9);
        assertEquals(30, data.totalMined().toDouble(), 1e-9);
        assertEquals(30, server.serverTotalMined().toDouble(), 1e-9);
        // 累計 30 ÷ 1,000 → 64m × 0.03 ＝ 1.92m
        assertEquals(0, change.newLevel());
        assertEquals(1.92, change.newMeters(), 1e-9);
        assertTrue(change.moved());
        assertFalse(change.completedOldLevel());
    }

    @Test
    void 道具がなければ何も変わらず保存対象にもならない() {
        PlayerData data = player();
        data.setCoal(BigNum.of(5));
        // 保存対象の印を消すために往復させる
        PlayerData fresh = DataCodec.decodePlayer(data.uuid(),
                DataCodec.encodePlayer(data));
        DigEngine.Change change = DigEngine.autoTick(fresh, new ServerData(), 1);
        assertTrue(change.coal().isZero());
        assertFalse(change.moved());
        assertFalse(fresh.isDirty());
    }

    @Test
    void 境目を越えたら階層が進む() {
        PlayerData data = player();
        data.setTotalMined(BigNum.of(990));
        data.setToolCount(Tool.LAMP.id(), 20);
        DigEngine.Change change = DigEngine.autoTick(data, new ServerData(), 1);
        assertEquals(0, change.oldLevel());
        assertEquals(1, change.newLevel());
        assertTrue(change.completedOldLevel());
        assertEquals(1, change.levelsCompleted());
        assertEquals(64, change.oldLevelLength());
        // 累計 1,010 → 階層 1 の 10 ÷ 2,000 → 80m × 0.005 ＝ 0.4m
        assertEquals(0.4, change.newMeters(), 1e-9);
    }

    @Test
    void 手掘りは1回ごとに石炭がもらえて回数が増える() {
        PlayerData data = player();
        ServerData server = new ServerData();
        DigEngine.Change change = DigEngine.click(data, server);
        assertEquals(10, change.coal().toDouble(), 1e-9);
        assertEquals(1, data.clicks());
        assertEquals(10, data.totalMined().toDouble(), 1e-9);
        assertEquals(10, server.serverTotalMined().toDouble(), 1e-9);

        data.addUpgrade("pickaxe_1");
        data.addUpgrade("skill_1");
        data.setToolCount(Tool.MINECART.id(), 100); // 毎秒 1,000
        // 20 ＋ 1,000 × 0.01 ＝ 30
        assertEquals(30, DigEngine.click(data, server).coal().toDouble(), 1e-9);
        assertEquals(2, data.clicks());
    }
}
