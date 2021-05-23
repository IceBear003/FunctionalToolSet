package fts.mechanism.player.superjump;

import fts.spi.ResourceUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class SuperJump {
    public static void addEffect(Player player, int level) {
        if (player.hasPermission("fts.sj." + level)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999 * 20, level - 1));
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 0, 0), 100);
            ResourceUtils.sendSpecialMessage(player, "use-superjump",
                    new ArrayList<String>() {
                        {
                            add("{level}");
                            add(String.valueOf(level));
                        }
                    });
        } else {
            ResourceUtils.sendSpecialMessage(player, "no-permission-use-superjump",
                    new ArrayList<String>() {
                        {
                            add("{level}");
                            add(String.valueOf(level));
                        }
                    });
        }
    }

    public static void removeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP);
        ResourceUtils.sendMessage(player, "close-superjump");
    }
}
