package fts.timeoperate;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

public class TimeSynchronization {
    private static BukkitRunnable task = null;
    private static ArrayList<String> worlds = new ArrayList<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("timeoperate.yml");
        File file = new File(plugin.getDataFolder(), "timeoperate.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for (String name : yaml.getStringList("synchronization.enable")) {
            if (Bukkit.getWorld(name) != null) {
                worlds.add(name);
            }
        }
        if (task != null) {
            return;
        }
        if (worlds.size() != 0) {
            for (String name : worlds) {
                World world = Bukkit.getWorld(name);
                world.setGameRuleValue("doDaylightCycle", "false");
            }
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (String name : worlds) {
                        World world = Bukkit.getWorld(name);
                        world.setTime((long) (getTimePercent() * 24000));
                    }
                }
            };
            task.runTaskTimer(plugin, 0L, 5L);
        }
    }

    private static double getTimePercent() {
        double seconds = 86400 - getRemainSecondsOneDay();
        if (seconds >= 21600) {
            return (seconds - 21600) / 86400.00;
        } else {
            return 1.0 - seconds / 86400.00;
        }
    }

    private static Integer getRemainSecondsOneDay() {
        Date currentDate = new Date();
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }
}
