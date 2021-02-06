package utes.lift;

import java.io.File;

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

public class IronBlockLift implements Listener {
    private static YamlConfiguration yaml;
    private static int maxHeight;
    private static int minHeight;
    private static int consumeHunger;
    private static int consumeExp;

    public IronBlockLift() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "lift.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("lift.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        maxHeight = yaml.getInt("maxHeight");
        minHeight = yaml.getInt("minHeight");
        consumeHunger = yaml.getInt("consumeHunger");
        consumeExp = yaml.getInt("consumeExp");

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    @EventHandler
    public void onJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().add(0, -1, 0).getBlock().getType() != Material.IRON_BLOCK)
            return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            if (event.getTo().getBlockY() > event.getFrom().getBlockY()) {
                Location loc = event.getFrom();
                for (int y = minHeight; y <= maxHeight; y++) {
                    Location newLoc = loc.clone().add(0, y, 0);
                    if (newLoc.getBlock().getType() == Material.IRON_BLOCK) {
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
        if (player.getLocation().add(0, -1, 0).getBlock().getType() != Material.IRON_BLOCK)
            return;
        Location loc = player.getLocation();
        for (int y = minHeight; y <= maxHeight; y++) {
            Location newLoc = loc.clone().add(0, -y, 0);
            if (newLoc.getBlock() == null)
                return;
            if (newLoc.getBlock().getType() == Material.IRON_BLOCK) {
                player.teleport(newLoc.add(0, 1, 0));
                consume(player);
                player.sendMessage("您乘坐铁块电梯下楼。");
            }
        }
    }

    private void consume(Player player) {
        player.setFoodLevel(player.getFoodLevel() - consumeHunger >= 0 ? player.getFoodLevel() - consumeHunger : 0);
        player.setTotalExperience(
                player.getTotalExperience() - consumeExp >= 0 ? player.getTotalExperience() - consumeExp : 0);
    }
}
