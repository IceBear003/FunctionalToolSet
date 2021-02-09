package utes.lift;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import utes.UntilTheEndServer;

import java.io.File;

public class IronBlockLift implements Listener {
    private static int maxHeight;
    private static int minHeight;
    private static int consumeHunger;
    private static int consumeExp;
    private static Material blockType;

    public static void initialize(UntilTheEndServer plugin) {
        File file = new File(plugin.getDataFolder(), "lift.yml");
        if (!file.exists()) {
            plugin.saveResource("lift.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        maxHeight = yaml.getInt("maxHeight");
        minHeight = yaml.getInt("minHeight");
        consumeHunger = yaml.getInt("consumeHunger");
        consumeExp = yaml.getInt("consumeExp");
        blockType = Material.valueOf(yaml.getString("block"));

        Bukkit.getPluginManager().registerEvents(new IronBlockLift(), plugin);
    }

    @EventHandler
    public void onJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().add(0, -1, 0).getBlock().getType() != blockType) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            if (event.getTo().getBlockY() > event.getFrom().getBlockY()) {
                Location loc = event.getFrom();
                for (int y = minHeight; y <= maxHeight; y++) {
                    Location newLoc = loc.clone().add(0, y, 0);
                    if (newLoc.getBlock().getType() == blockType) {
                        player.teleport(newLoc.add(0, 1, 0));
                        consume(player);
                        player.sendMessage("您乘坐铁块电梯上楼。");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().add(0, -1, 0).getBlock().getType() != blockType) {
            return;
        }
        Location loc = player.getLocation();
        for (int y = minHeight; y <= maxHeight; y++) {
            Location newLoc = loc.clone().add(0, -y, 0);
            if (newLoc.getBlock() == null) {
                return;
            }
            if (newLoc.getBlock().getType() == blockType) {
                player.teleport(newLoc.add(0, 1, 0));
                consume(player);
                player.sendMessage("您乘坐铁块电梯下楼。");
            }
        }
    }

    private void consume(Player player) {
        player.setFoodLevel(Math.max(player.getFoodLevel() - consumeHunger, 0));
        player.setTotalExperience(Math.max(player.getTotalExperience() - consumeExp, 0));
    }
}
