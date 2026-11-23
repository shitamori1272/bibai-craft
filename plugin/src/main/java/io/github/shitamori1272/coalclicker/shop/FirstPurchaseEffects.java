package io.github.shitamori1272.coalclicker.shop;

import io.github.shitamori1272.coalclicker.effect.SoundFade;
import io.github.shitamori1272.coalclicker.game.Tool;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * 道具をはじめて買ったときの演出（1 回だけ）。道具ごとにその道具らしい音とパーティクルを、買った本人にだけ出す。
 *
 * <ul>
 *   <li>安全灯：ランタンを置く音と、2 秒だけ明るくなる（暗視）。光の粒を少し
 *   <li>炭車：トロッコの走る音と、目の前を横切る石炭のかけら
 *   <li>発破：爆発音と爆発・煙。画面を揺らす（ダメージ・ブロックの破壊はない）
 *   <li>コールピック：石を叩く音を素早く 6 回と火花
 *   <li>ベルトコンベア：ピストンの音を一定の間隔で 4 回と、床を流れる石炭
 *   <li>ドラムカッター：砥石の回る音を 3 回と粉じん
 *   <li>自走枠：鉄の扉が重く閉まる音と、鎖のきしむ音
 *   <li>立坑櫓：巻揚機の音（トロッコの走行音を低く・鎖の音）のあと、合図の鐘を 2 回「チン、チン」
 * </ul>
 *
 * <p>演出の場所は目の前 2.5 ブロック。ショップの画面は閉じない（演出は画面の後ろで流れる）。
 * パーティクルは 1 回に数十個までにする。途中でログアウトしたら残りは出さない。サーバーのメインスレッドから使うこと。
 */
public final class FirstPurchaseEffects {

    /** 演出を出す、目からの距離（ブロック）。 */
    private static final double DISTANCE = 2.5;

    private static final BlockData COAL = Material.COAL_BLOCK.createBlockData();
    private static final BlockData COAL_ORE = Material.COAL_ORE.createBlockData();

    private final Plugin plugin;

    public FirstPurchaseEffects(Plugin plugin) {
        this.plugin = plugin;
    }

    /** tool をはじめて買った player に演出を出す。 */
    public void play(Player player, Tool tool) {
        switch (tool) {
            case LAMP -> lamp(player);
            case MINECART -> minecart(player);
            case BLASTING -> blasting(player);
            case COAL_PICK -> coalPick(player);
            case CONVEYOR -> conveyor(player);
            case DRUM_CUTTER -> drumCutter(player);
            case SHIELD -> shield(player);
            case HEADFRAME -> headframe(player);
        }
    }

    // ---- 道具ごとの演出 ----

    private void lamp(Player player) {
        Location at = front(player);
        player.playSound(at, Sound.BLOCK_LANTERN_PLACE, 1f, 1f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 40, 0, false, false, false));
        player.spawnParticle(Particle.END_ROD, at, 12, 0.6, 0.6, 0.6, 0.02);
    }

    private void minecart(Player player) {
        SoundFade.play(player, Sound.ENTITY_MINECART_RIDING, 0.8f, 1f);
        // 目の前を左から右へ、石炭のかけらが横切る
        Vector side = sideways(player);
        for (int step = 0; step < 6; step++) {
            int i = step;
            later(player, step * 3L, p -> {
                Location at = front(p).add(0, -0.8, 0).add(side.clone().multiply(-1.5 + i * 0.6));
                p.spawnParticle(Particle.BLOCK, at, 6, 0.1, 0.1, 0.1, 0, COAL);
            });
        }
        // 走り去っていくように、ぶつっと切らずに小さくする
        later(player, 20, p -> SoundFade.fadeOut(plugin, p, Sound.ENTITY_MINECART_RIDING, 0.8f, 1f, 5, 3));
    }

    private void blasting(Player player) {
        Location at = front(player);
        player.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        player.spawnParticle(Particle.EXPLOSION, at, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.SMOKE, at, 20, 0.6, 0.6, 0.6, 0.03);
        // 画面を揺らす（ダメージは受けない）
        player.sendHurtAnimation(0);
    }

    private void coalPick(Player player) {
        for (int i = 0; i < 6; i++) {
            later(player, i * 2L, p -> {
                Location at = front(p);
                p.playSound(at, Sound.BLOCK_STONE_HIT, 1f, 1.4f);
                p.spawnParticle(Particle.CRIT, at, 3, 0.2, 0.2, 0.2, 0.2);
            });
        }
    }

    private void conveyor(Player player) {
        Vector side = sideways(player);
        for (int i = 0; i < 4; i++) {
            int beat = i;
            later(player, i * 5L, p -> {
                Location at = front(p);
                p.playSound(at, Sound.BLOCK_PISTON_EXTEND, 0.7f, 1f);
                // 床の高さを、石炭が少しずつ流れていく
                Location floor = at.add(0, -1.2, 0).add(side.clone().multiply(-1 + beat * 0.6));
                p.spawnParticle(Particle.BLOCK, floor, 8, 0.3, 0.02, 0.1, 0, COAL);
            });
        }
    }

    private void drumCutter(Player player) {
        for (int i = 0; i < 3; i++) {
            later(player, i * 8L, p -> {
                Location at = front(p);
                p.playSound(at, Sound.BLOCK_GRINDSTONE_USE, 1f, 0.8f);
                p.spawnParticle(Particle.BLOCK, at, 10, 0.8, 0.8, 0.8, 0, COAL_ORE);
            });
        }
    }

    private void shield(Player player) {
        Location at = front(player);
        player.playSound(at, Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 0.5f);
        later(player, 6, p -> p.playSound(front(p), Sound.BLOCK_CHAIN_PLACE, 1f, 0.6f));
    }

    private void headframe(Player player) {
        // 巻揚機（ケージの降りる演出と同じ音の組み合わせ）
        SoundFade.play(player, Sound.ENTITY_MINECART_RIDING, 0.6f, 0.5f);
        for (int t = 0; t <= 24; t += 6) {
            later(player, t, p -> p.playSound(p, Sound.BLOCK_CHAIN_PLACE, 0.8f, 0.5f));
        }
        later(player, 20, p -> SoundFade.fadeOut(plugin, p, Sound.ENTITY_MINECART_RIDING, 0.6f, 0.5f, 5, 3));
        // 合図の鐘「チン、チン」
        later(player, 34, p -> p.playSound(p, Sound.BLOCK_BELL_USE, 1f, 1.2f));
        later(player, 44, p -> p.playSound(p, Sound.BLOCK_BELL_USE, 1f, 1.2f));
    }

    // ---- 共通 ----

    /** 目の前 {@link #DISTANCE} ブロックの場所。 */
    private static Location front(Player player) {
        Location eye = player.getEyeLocation();
        return eye.add(eye.getDirection().multiply(DISTANCE));
    }

    /** 視線に対して右向きの水平な単位ベクトル。 */
    private static Vector sideways(Player player) {
        Vector dir = player.getEyeLocation().getDirection().setY(0);
        if (dir.lengthSquared() < 1e-6) {
            return new Vector(1, 0, 0);
        }
        return new Vector(-dir.getZ(), 0, dir.getX()).normalize();
    }

    /** delay tick 後に、まだオンラインなら action を実行する（0 ならすぐ）。 */
    private void later(Player player, long delay, Consumer<Player> action) {
        if (delay <= 0) {
            action.accept(player);
            return;
        }
        UUID id = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player online = Bukkit.getPlayer(id);
            if (online != null && online.isOnline()) {
                action.accept(online);
            }
        }, delay);
    }
}
