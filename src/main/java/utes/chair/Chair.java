package utes.chair;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import utes.ResourceUtils;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/*
 * TODO
 * utes.sit
 */
public class Chair implements Listener {
    private static HashMap<UUID, UUID> chairs = new HashMap<>();
    private static List<String> blockTypes;
    private static double blood;

    public static void initialize(UntilTheEndServer plugin) {
        ResourceUtils.autoUpdateConfigs("chair.yml");
        File file = new File(plugin.getDataFolder(), "chair.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        blockTypes = yaml.getStringList("blocks");
        blood = yaml.getDouble("blood");

        Bukkit.getPluginManager().registerEvents(new Chair(), plugin);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("utes.sit")) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (player.isSneaking()) {
            return;
        }
        Block block = event.getClickedBlock();
        boolean flag = false;
        for (String str : blockTypes) {
            if (block.getType().toString().contains(str)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            Location loc = block.getLocation().add(0.5, -0.1, 0.5);
            World world = block.getWorld();
            Arrow arrow = (Arrow) world.spawnEntity(loc, EntityType.ARROW);
            arrow.setGravity(false);
            arrow.teleport(loc);
            arrow.setPassenger(player);
            player.sendMessage("你坐到了椅子上");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (arrow.getPassenger() != null) {
                        player.setHealth(Math.min(player.getHealth() + blood, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                    } else {
                        cancel();
                        arrow.remove();
                        player.sendMessage("你站了起来");
                        return;
                    }
                }
            }.runTaskTimer(UntilTheEndServer.getInstance(), 0L, 20L);
        }
    }
}
