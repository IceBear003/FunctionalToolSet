package utes.timeoperate;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

import java.io.File;

public class QuickNight {
    private static double percent;
    private static int speed;
    private static boolean title;

    public static void initialize(UntilTheEndServer plugin) {
        File file = new File(plugin.getDataFolder(), "timeoperate.yml");
        if (!file.exists()) {
            plugin.saveResource("timeoperate.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("quickNight.enable")) {
            return;
        }

        percent = yaml.getDouble("quickNight.percent");
        speed = yaml.getInt("quickNight.speed");
        title = yaml.getBoolean("quickNight.title");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    //白天不进行计算
                    if (world.getTime() < 12600) {
                        continue;
                    }
                    //计算某世界总睡觉玩家人数
                    int amount = 0;
                    if (world.getPlayers().size() == 0) {
                        continue;
                    }
                    for (Player player : world.getPlayers()) {
                        if (player.isSleeping()) {
                            amount++;
                        }
                    }
                    //如果全部都在入睡，就按原版机制跳到第二天
                    if (amount == world.getPlayers().size()) {
                        continue;
                    }
                    //如果入睡人数超过占比，则加速时间流动
                    if (amount >= world.getPlayers().size() * percent) {
                        long newTime = world.getTime() + 2 * (speed - 1);
                        if (newTime >= 24000) {
                            world.setTime(newTime - 24000);
                        } else {
                            world.setTime(newTime);
                        }
                        //告诉玩家几点了
                        if (title) {
                            for (Player player : world.getPlayers()) {
                                player.resetTitle();
                                player.sendTitle("§a" + getFormatTime(newTime), "§e世界时间", 10, 70, 20);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    //把世界时间刻变为刻度的hh:mm
    private static String getFormatTime(long newTime) {
        long hour = newTime / 1000;
        long minute = (long) (newTime % 1000 * 0.06);
        String tmp = "";
        if (minute < 10) {
            tmp += "0" + minute;
        } else {
            tmp += minute;
        }
        if (hour + 6 >= 24) {
            return "0" + (hour + 6 - 24) + ":" + tmp;
        } else {
            return (hour + 6) + ":" + tmp;
        }
    }
}
