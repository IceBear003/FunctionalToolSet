package fts.xpfly;

import fts.FunctionalToolSet;
import fts.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class XPFly {
    private static final ArrayList<UUID> flyingPlayers = new ArrayList<>();
    private static double exhaustSpeed;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("xpfly.yml");
        File file = new File(plugin.getDataFolder(), "xpfly.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        exhaustSpeed = yaml.getDouble("exhaustSpeed");

        HashSet<UUID> tmp = new HashSet<>();

        new BukkitRunnable() {
            long counter = 0;

            @Override
            public void run() {
                counter++;
                for (UUID uuid : (ArrayList<UUID>) flyingPlayers.clone()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player.getExp() < exhaustSpeed / 10 && player.getLevel() == 0) {
                        player.sendMessage("您没有足够的经验，自动停止飞行");
                        cancelFly(player);
                        return;
                    }

                    float currentExp = player.getExp();
                    float newExp = currentExp - (float) exhaustSpeed / 10;

                    if (newExp < 0.0f) {
                        if (tmp.contains(player.getUniqueId())) {
                            newExp = 1.0f;
                        } else {
                            tmp.add(player.getUniqueId());
                            player.setLevel(Math.max(player.getLevel() - 1, 0));
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    tmp.remove(player.getUniqueId());
                                }
                            }.runTaskLater(FunctionalToolSet.getInstance(), 2L);
                            newExp = 1.0f;
                        }
                    }

                    if (player.hasPermission("fts.xpfly.slowexhaust")) {
                        if (counter % 2 == 0) {
                            player.setExp(newExp);
                        }
                    } else {
                        player.setExp(newExp);
                    }

                    if (player.isOnGround() && player.isSneaking()) {
                        player.sendMessage("您已经落地，自动停止飞行");
                        cancelFly(player);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    private static int getExpToLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    public static void initXPFly(Player player) {
        if (!player.hasPermission("fts.xpfly.use")) {
            player.sendMessage("您没有权限使用经验飞行！");
            return;
        }
        if (flyingPlayers.contains(player.getUniqueId())) {
            cancelFly(player);
            player.sendMessage("经验飞行已经关闭");
        } else {
            goFly(player);
        }
    }

    private static void goFly(Player player) {
        if (player.getTotalExperience() < exhaustSpeed) {
            player.sendMessage("您没有足够的经验飞行！");
            return;
        }
        flyingPlayers.add(player.getUniqueId());
        player.setAllowFlight(true);
        player.setFlying(true);
        player.sendMessage("经验飞行已经开启");
    }

    private static void cancelFly(Player player) {
        flyingPlayers.remove(player.getUniqueId());
        player.setAllowFlight(false);
        player.setFlying(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4));
    }
}
