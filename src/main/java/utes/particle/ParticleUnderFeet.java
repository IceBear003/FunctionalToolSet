package utes.particle;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import utes.UntilTheEndServer;

import java.util.HashMap;
import java.util.UUID;

public class ParticleUnderFeet implements Listener {
    private static final HashMap<UUID, Effect> users = new HashMap<>();

    public static void initialize(UntilTheEndServer plugin) {
        Bukkit.getPluginManager().registerEvents(new ParticleUnderFeet(), plugin);
    }

    public static void drawParticle(Player player, String particleName) {
        Effect particle;
        try {
            particle = Effect.valueOf(particleName);
        } catch (Exception exception) {
            player.sendMessage("请输入正确的粒子效果名称");
            return;
        }
        if (player.hasPermission("utes.particle.over." + particle.toString())) {
            users.remove(player.getUniqueId());
            users.put(player.getUniqueId(), particle);
        } else {
            player.sendMessage("您没有权限使用该粒子效果！");
        }
    }

    public static void stop(Player player) {
        users.remove(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (users.containsKey(player.getUniqueId())) {
            Location to = event.getTo();
            Location from = event.getFrom();
            Location loc = player.getLocation();
            if ((to.getX() != from.getBlockX()) || (to.getY() != from.getY()) || (to.getZ() != from.getZ())) {
                loc.setY(loc.getY() - 1.0D);
                player.getWorld().playEffect(loc, users.get(player.getUniqueId()), 1, 25);
            } else {
                event.setCancelled(false);
            }
        } else {
            event.setCancelled(false);
        }
    }
}
