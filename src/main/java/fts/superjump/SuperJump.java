package fts.superjump;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SuperJump {
    public static void addEffect(Player player, int level) {
        if (player.hasPermission("fts.sj." + level)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999 * 20, level - 1));
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 0, 0), 100);
            player.sendMessage("§6" + level + "§r级超级跳已经开启！");
        } else {
            player.sendMessage("您没有权限使用§6" + level + "§r级超级跳！");
        }
    }

    public static void removeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP);
        player.sendMessage("超级跳已经关闭！");
    }
}
