package fts.onlinetimes;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class OnlineTimes implements Listener {
    private static final HashMap<UUID, IPlayerOnlineTimes> stats = new HashMap<>();
    private static BukkitRunnable task = null;

    public static void initialize(FunctionalToolSet plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            stats.put(player.getUniqueId(), load(player));
        }

        Bukkit.getPluginManager().registerEvents(new OnlineTimes(), plugin);

        if (task != null) {
            return;
        }

        task = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    IPlayerOnlineTimes stat = stats.get(player.getUniqueId());
                    stat.dayTime++;
                    stat.totalTime++;
                    if (counter % 60 == 0) {
                        save(player);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private static IPlayerOnlineTimes load(Player player) {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/onlinetimes/",
                player.getUniqueId().toString() + ".yml");
        if (!file.exists()) {
            return (new IPlayerOnlineTimes(0, 0, LocalDate.now().getDayOfMonth()));
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return new IPlayerOnlineTimes(yaml.getInt("dayTime"), yaml.getInt("totalTime"), yaml.getInt("lastLoginDate"));
    }

    public static void save(Player player) {
        if (!stats.containsKey(player.getUniqueId())) {
            return;
        }
        IPlayerOnlineTimes stat = stats.get(player.getUniqueId());
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/onlinetimes/",
                player.getUniqueId().toString() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set("dayTime", stat.dayTime);
        yaml.set("totalTime", stat.totalTime);
        yaml.set("lastLoginDate", stat.lastLoginDate);
        try {
            yaml.save(file);
        } catch (IOException e) {
            FunctionalToolSet.getInstance().getLogger().info(
                    ResourceUtils.getSpecialLang("error-while-save-onlinetime",
                            new ArrayList<String>() {
                                {
                                    add("{player}");
                                    add(player.getName());
                                }
                            })
            );
        }
    }

    public static int getTotalTime(Player player) {
        return stats.get(player.getUniqueId()).totalTime;
    }

    public static int getDayTime(Player player) {
        return stats.get(player.getUniqueId()).dayTime;
    }

    public static String turnToString(int time) {
        if (time <= 0) {
            return "";
        }
        int d = time / 3600 / 24;
        int h = time % (3600 * 24) / 3600;
        int m = time % 3600 / 60;
        int s = time % 60;
        if (d == 0) {
            if (h == 0) {
                if (m == 0) {
                    return s + "秒";
                } else {
                    return m + "分钟" + s + "秒";
                }
            } else {
                return h + "小时" + m + "分钟" + s + "秒";
            }
        } else {
            return d + "天" + h + "小时" + m + "分钟" + s + "秒";
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        stats.put(player.getUniqueId(), load(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        save(player);
        stats.remove(player.getUniqueId());
    }
}
