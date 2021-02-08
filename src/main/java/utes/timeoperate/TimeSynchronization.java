package utes.timeoperate;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

//TODO
public class TimeSynchronization {
    private static YamlConfiguration yaml;
    private static ArrayList<String> worlds = new ArrayList<String>();

    public TimeSynchronization() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "timeoperate.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("timeoperate.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);

        for (String name : yaml.getStringList("synchronization.enable")) {
            if (Bukkit.getWorld(name) == null)
                continue;
            else worlds.add(name);
        }
        if (worlds.size() != 0) {
            for (String name : worlds) {
                World world = Bukkit.getWorld(name);
                world.setGameRuleValue("doDaylightCycle", "false");
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (String name : worlds) {
                        World world = Bukkit.getWorld(name);
                        world.setTime((long) (getTimePercent() * 24000));
                    }
                }
            }.runTaskTimer(UntilTheEndServer.getInstance(), 0L, 5L);
        }
    }

    private static double getTimePercent() {
        double seconds = 86400 - getRemainSecondsOneDay();
        if (seconds >= 21600) return (seconds - 21600) / 86400.00;
        else return 1.0 - seconds / 86400.00;
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
