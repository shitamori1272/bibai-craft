package io.github.shitamori1272.coalclicker.dig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.data.DataCodec;
import io.github.shitamori1272.coalclicker.data.PlayerData;
import io.github.shitamori1272.coalclicker.data.ServerData;
import io.github.shitamori1272.coalclicker.game.Formulas;
import io.github.shitamori1272.coalclicker.game.Tool;
import io.github.shitamori1272.coalclicker.number.BigNum;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DigEngineTest {

    private static PlayerData player() {
        return new PlayerData(UUID.randomUUID());
    }

    private static void assertRelative(double expected, BigNum actual, double tolerance) {
        assertEquals(1.0, actual.toDouble() / expected, tolerance, () -> "期待 " + expected + " / 実際 " + actual);
    }

    /** 小さな刻みで「今の階層の毎秒の石炭 × 刻み」を足していく、素朴なシミュレーション。 */
    private static double bruteForce(double base, double totalMined, double seconds, double dt) {
        double total = totalMined;
        double gained = 0;
        for (double t = 0; t < seconds - 1e-12; t += dt) {
            double step = Math.min(dt, seconds - t);
            long level = Formulas.levelOf(BigNum.of(total));
            double coal = base * Math.pow(1.1, level) * step;
            total += coal;
            gained += coal;
        }
        return gained;
    }

    // ---- 毎秒の石炭 ----

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
        assertEquals(0, change.newLevel());
        assertEquals(0, change.levelsAdvanced());
    }

    @Test
    void 道具がなければ何も変わらず保存対象にもならない() {
        PlayerData data = player();
        data.setCoal(BigNum.of(5));
        // 保存対象の印を消すために往復させる
        PlayerData fresh = DataCodec.decodePlayer(data.uuid(), DataCodec.encodePlayer(data));
        DigEngine.Change change = DigEngine.autoTick(fresh, new ServerData(), 1);
        assertTrue(change.coal().isZero());
        assertFalse(fresh.isDirty());
    }

    @Test
    void 境目を越えたら途中から深さ倍率が上がる() {
        PlayerData data = player();
        data.setTotalMined(BigNum.of(6_390));
        data.setToolCount(Tool.LAMP.id(), 20); // 階層 0 で毎秒 20、階層 1 で 22
        DigEngine.Change change = DigEngine.autoTick(data, new ServerData(), 1);
        assertEquals(0, change.oldLevel());
        assertEquals(1, change.newLevel());
        assertEquals(1, change.levelsAdvanced());
        // 0.5 秒で 10 個（境目まで）、残り 0.5 秒は毎秒 22 個で 11 個
        assertEquals(21, change.coal().toDouble(), 1e-9);
    }

    @Test
    void 何階層ぶんもまとめて足すと小刻みのシミュレーションと一致する() {
        // 毎秒 10 個で 1 時間：複数の階層を進む
        double base = 10;
        BigNum closed = DigEngine.autoCoal(BigNum.of(base), BigNum.ZERO, 3600);
        assertRelative(bruteForce(base, 0, 3600, 0.01), closed, 1e-4);
        assertTrue(Formulas.levelOf(closed) >= 2);

        // 途中の進み具合から、8 時間ぶん
        assertRelative(bruteForce(3.7, 12_345, 8 * 3600, 0.05),
                DigEngine.autoCoal(BigNum.of(3.7), BigNum.of(12_345), 8 * 3600), 1e-4);
    }

    @Test
    void 一秒ずつ足しても一度に足しても同じ() {
        PlayerData stepwise = player();
        stepwise.setToolCount(Tool.MINECART.id(), 5);
        for (int i = 0; i < 2000; i++) {
            DigEngine.autoTick(stepwise, new ServerData(), 1);
        }
        PlayerData once = player();
        once.setToolCount(Tool.MINECART.id(), 5);
        DigEngine.Change change = DigEngine.autoTick(once, new ServerData(), 2000);
        assertRelative(stepwise.totalMined().toDouble(), once.totalMined(), 1e-9);
        assertEquals(Formulas.levelOf(stepwise.totalMined()), change.newLevel());
        assertTrue(change.levelsAdvanced() > 1);
    }

    @Test
    void 毎秒の石炭が途方もなく大きくても止まらず有限で返る() {
        BigNum huge = BigNum.of(1, 5000);
        BigNum coal = DigEngine.autoCoal(huge, BigNum.ZERO, 8 * 3600);
        assertTrue(coal.isGreaterThanOrEqual(huge.multiply(8 * 3600)));
        assertTrue(DigEngine.autoCoal(BigNum.ONE, BigNum.ZERO, 0).isZero());
        assertTrue(DigEngine.autoCoal(BigNum.ZERO, BigNum.ZERO, 100).isZero());
    }

    @Test
    void 良質な炭層の倍率を掛ける() {
        PlayerData data = player();
        data.setToolCount(Tool.MINECART.id(), 3);
        assertEquals(210, DigEngine.autoTick(data, new ServerData(), 1, 7).coal().toDouble(), 1e-9);
    }

    // ---- 手掘り ----

    @Test
    void 手掘りは1回ごとに石炭がもらえて回数が増える() {
        PlayerData data = player();
        ServerData server = new ServerData();
        DigEngine.Change change = DigEngine.click(data, server);
        assertEquals(10, change.coal().toDouble(), 1e-9);
        assertEquals(1, data.clicks());
        assertEquals(10, data.coal().toDouble(), 1e-9);
        assertEquals(10, data.totalMined().toDouble(), 1e-9);
        assertEquals(10, server.serverTotalMined().toDouble(), 1e-9);

        data.addUpgrade("pickaxe_1");
        data.addUpgrade("skill_1");
        data.setToolCount(Tool.MINECART.id(), 100); // 毎秒 1,000
        // 20 ＋ 1,000 × 0.01 ＝ 30
        assertEquals(30, DigEngine.click(data, server).coal().toDouble(), 1e-9);
        assertEquals(2, data.clicks());
    }

    @Test
    void 手掘りの上乗せは今の階層の深さ倍率込みで倍率も掛かる() {
        PlayerData data = player();
        data.setTotalMined(Formulas.cumulativeToFinish(1)); // 階層 2
        data.addUpgrade("skill_1");
        data.setToolCount(Tool.MINECART.id(), 100); // 毎秒 1,000 × 1.21
        assertEquals(10 + 12.1, DigEngine.click(data, new ServerData()).coal().toDouble(), 1e-9);
        // 毎秒の石炭 ×7 なら上乗せも ×7、さらに手掘り ×2
        assertEquals((10 + 12.1 * 7) * 2, DigEngine.click(data, new ServerData(), 7, 2).coal().toDouble(), 1e-9);
    }

    @Test
    void 手掘りで境目を越えると階層が進む() {
        PlayerData data = player();
        data.setTotalMined(BigNum.of(6_395));
        DigEngine.Change change = DigEngine.click(data, new ServerData());
        assertEquals(1, change.levelsAdvanced());
    }

    // ---- 切羽 ----

    @Test
    void 見た目と数値の階層が同じで掘り切っていなければ手掘りできる() {
        assertEquals(DigEngine.Face.OPEN, DigEngine.face(2, 2, 0));
        assertEquals(DigEngine.Face.OPEN, DigEngine.face(2, 2, 95.9));
        // 数値上の階層が進んでいる（掘り切ってケージ待ち）
        assertEquals(DigEngine.Face.AWAITING_CAGE, DigEngine.face(3, 2, 96));
        assertEquals(DigEngine.Face.AWAITING_CAGE, DigEngine.face(3, 2, 10));
        assertEquals(DigEngine.Face.AWAITING_CAGE, DigEngine.face(2, 2, 96));
    }
}
