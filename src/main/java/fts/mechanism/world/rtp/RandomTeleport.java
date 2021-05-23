package fts.mechanism.world.rtp;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RandomTeleport {
    private static final List<String> enableWorlds = new ArrayList<>();
    private static final HashMap<UUID, Long> lastUseTimeStamp = new HashMap<>();
    private static int waitTime;
    private static int maxX;
    private static int maxZ;
    private static int minX;
    private static int minZ;
    private static int cooldown;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("rtp.yml");
        File file = new File(plugin.getDataFolder(), "rtp.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        waitTime = yaml.getInt("waitTime");
        maxX = yaml.getInt("maxRange.x");
        maxZ = yaml.getInt("maxRange.z");
        minX = yaml.getInt("minRange.x");
        minZ = yaml.getInt("minRange.z");
        cooldown = yaml.getInt("cooldown");

        List<String> disableWorlds = yaml.getStringList("disableWorlds");
        for (World world : Bukkit.getWorlds()) {
            if (!disableWorlds.contains(world.getName())) {
                enableWorlds.add(world.getName());
            }
        }
    }

    public static void initRTP(Player player) {
        if (!player.hasPermission("fts.mechanism.world.rtp.use")) {
            ResourceUtils.sendMessage(player, "no-permission-rtp");
            return;
        }
        if ((!enableWorlds.contains(player.getWorld().getName())) && (!player.hasPermission("fts.mechanism.world.rtp.ignoreworld"))) {
            ResourceUtils.sendMessage(player, "cannot-rtp-in-world");
            return;
        }
        if (lastUseTimeStamp.containsKey(player.getUniqueId()) && (!player.hasPermission("fts.mechanism.world.rtp.ignorecd"))) {
            if (System.currentTimeMillis() - lastUseTimeStamp.get(player.getUniqueId()) < cooldown * 1000) {
                ResourceUtils.sendSpecialMessage(player, "cooldowning-when-rtp",
                        new ArrayList<String>() {
                            {
                                add("{time}");
                                add(String.valueOf((cooldown - (System.currentTimeMillis() - lastUseTimeStamp.get(player.getUniqueId()) / 1000))));
                            }
                        });
                return;
            }
        }
        ResourceUtils.sendSpecialMessage(player, "wait-rtp",
                new ArrayList<String>() {
                    {
                        add("{time}");
                        add(String.valueOf(waitTime));
                    }
                });
        Location from = player.getLocation();
        Location destination = getRandomDestination(from.clone());
        if (destination == null) {
            ResourceUtils.sendMessage(player, "cannot-find-save-loc");
            return;
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                if (player.getLocation().distance(from) >= 0.3 && (!player.hasPermission("fts.mechanism.world.rtp.ignoremove"))) {
                    ResourceUtils.sendMessage(player, "cancel-rtp-after-move");
                    return;
                }
                ResourceUtils.sendSpecialMessage(player, "successfully-rtp",
                        new ArrayList<String>() {
                            {
                                add("{X}");
                                add(String.valueOf(destination.getBlockX()));
                                add("{Z}");
                                add(String.valueOf(destination.getBlockZ()));
                            }
                        });
                player.teleport(destination);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
                lastUseTimeStamp.remove(player.getUniqueId());
                lastUseTimeStamp.put(player.getUniqueId(), System.currentTimeMillis());
            }

        }.runTaskLater(FunctionalToolSet.getInstance(), waitTime * 20L);
    }

    private static Location getRandomDestination(Location loc) {
        Location current = loc.clone();
        int y = -1, counter = 0;
        do {
            counter++;
            if (counter == 100) {
                break;
            }

            loc = current.clone();
            loc.add(minX + Math.random() * (maxX - minX), 0, minZ + Math.random() * (maxZ - minZ));

            y = isSafe(loc);
            if (y != -1) {
                break;
            }
        } while (true);
        if (y == -1) {
            return null;
        }
        loc.setY(y);
        return loc;
    }

    private static int isSafe(Location loc) {
        for (int y = 128; y >= 32; y--) {
            loc.setY(y);
            Location upLoc = loc.clone().add(0, 1, 0);
            Location downLoc = loc.clone().add(0, -1, 0);
            if (loc.getBlock().getType() == Material.AIR && upLoc.getBlock().getType() == Material.AIR
                    && downLoc.getBlock().getType() != Material.AIR && !downLoc.getBlock().getType().isSolid()) {
                return y;
            }
        }
        return -1;
    }
}
