package utes.timeoperate;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

import java.io.File;

public class QuickNight {
    private static YamlConfiguration yaml;
    private static double percent;
    private static int speed;
    private static boolean title;

    public QuickNight() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "timeoperate.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("timeoperate.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("quickNight.enable"))
            return;

        percent = yaml.getDouble("quickNight.percent");
        speed = yaml.getInt("quickNight.speed");
        title = yaml.getBoolean("quickNight.title");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getTime() < 12600)
                        continue;
                    int amount = 0;
                    if (world.getPlayers().size() == 0)
                        continue;
                    for (Player player : world.getPlayers())
                        if (player.isSleeping()) {
                            amount++;
                        }
                    if (amount == world.getPlayers().size())
                        continue;
                    if (amount >= world.getPlayers().size() * percent) {
                        long newTime = world.getTime() + 2 * (speed - 1);
                        System.out.println(newTime);
                        if (newTime >= 24000)
                            world.setTime(newTime - 24000);
                        else
                            world.setTime(newTime);

                        if (title) {
                            for (Player player : world.getPlayers()) {
                                player.resetTitle();
                                player.sendTitle("§a" + getFormatTime(newTime), "§e世界时间");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(UntilTheEndServer.getInstance(), 0L, 2L);
    }

    private static String getFormatTime(long newTime) {
        long hour = newTime / 1000;
        long minute = (long) (newTime % 1000 * 0.06);
        String tmp = "";
        if (minute < 10) tmp += "0" + minute;
        else tmp += minute;
        if (hour + 6 >= 24) return "0" + (hour + 6 - 24) + ":" + tmp;
        else return (hour + 6) + ":" + tmp;
    }
}
