package fts.chair;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Chair implements Listener {
    private static HashMap<UUID, UUID> chairs = new HashMap<>();
    private static List<String> blockTypes;
    private static double blood;

    public static void initialize(FunctionalToolSet plugin) {
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

    private static HashSet<UUID> clickers = new HashSet<>();

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("fts.sit")) {
            return;
        }
        if (clickers.contains(player.getUniqueId())) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (player.isSneaking()) {
            return;
        }
        clickers.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                clickers.remove(player.getUniqueId());
            }
        }.runTaskLaterAsynchronously(FunctionalToolSet.getInstance(), 2L);

        Block block = event.getClickedBlock();
        Material type = block.getType();
        boolean flag = false;
        for (String str : blockTypes) {
            if (block.getType().toString().contains(str)) {
                flag = true;
            }
        }
        if (sitters.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("你已经坐下来了！");
            return;
        }
        if (flag == true && block.getLocation().add(0, -1, 0).getBlock().getType() == Material.AIR) {
            event.setCancelled(true);
            player.sendMessage("你不能坐在悬空的椅子上！");
            return;
        }
        if (flag == true && block.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) {
            event.setCancelled(true);
            player.sendMessage("你不能坐在上方无空间的椅子上！");
            return;
        }
        if (flag) {
            Location loc = block.getLocation().add(0.5, -0.1, 0.5);
            World world = block.getWorld();
            Arrow arrow = (Arrow) world.spawnEntity(loc, EntityType.ARROW);
            arrow.teleport(loc);
            arrow.setPassenger(player);
            arrow.setGravity(false);
            arrow.setBounce(false);
            player.sendMessage("你坐到了椅子上");
            sitters.add(player.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (sitters.contains(player.getUniqueId()) && block.getType() == type) {
                        player.setHealth(Math.min(player.getHealth() + blood, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                    } else {
                        arrow.remove();
                        cancel();
                        player.sendMessage("你站了起来");
                        return;
                    }
                }
            }.runTaskTimer(FunctionalToolSet.getInstance(), 0L, 20L);
        }
    }

    private static HashSet<UUID> sitters = new HashSet<>();

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (sitters.contains(player.getUniqueId())) {
            sitters.remove(player.getUniqueId());
            player.teleport(player.getLocation().add(0, 1, 0));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (sitters.contains(player.getUniqueId())) {
            sitters.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (sitters.contains(player.getUniqueId())) {
            sitters.remove(player.getUniqueId());
        }
    }
}
