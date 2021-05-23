package fts.info.scoreboard;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import fts.stat.onlinetimes.OnlineTimes;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoreBoard implements Listener {
    private static final ArrayList<UUID> disablers = new ArrayList<>();
    private static BukkitRunnable task = null;
    private static String title;
    private static List<String> lines;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("scoreboard.yml");
        File file = new File(plugin.getDataFolder(), "scoreboard.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        title = yaml.getString("title");
        lines = yaml.getStringList("lines");

        Bukkit.getPluginManager().registerEvents(new ScoreBoard(), plugin);

        if (task != null) {
            return;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        if (disablers.contains(player.getUniqueId())) {
                            continue;
                        }
                        Scoreboard board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
                        Objective object = board.registerNewObjective("Scoreboard", "scoreboard");
                        object.setDisplayName(ResourceUtils.getPapi(player, title));
                        object.setDisplaySlot(DisplaySlot.SIDEBAR);
                        int size = lines.size();
                        for (String line : lines) {
                            line = ResourceUtils.getPapi(player, line);
                            line = line.replace("%dayOnlineTime%",
                                    OnlineTimes.turnToString(OnlineTimes.getDayTime(player)));
                            line = line.replace("%totalOnlineTime%",
                                    OnlineTimes.turnToString(OnlineTimes.getTotalTime(player)));
                            object.getScore(line).setScore(size--);
                        }
                        player.setScoreboard(board);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 1L, 20L);
    }

    public static void changeState(Player player) {
        if (!player.hasPermission("fts.sb.toggle")) {
            ResourceUtils.sendMessage(player, "no-permission-toggle-scoreboard");
            return;
        }
        if (!disablers.contains(player.getUniqueId())) {
            disablers.add(player.getUniqueId());
            player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
            ResourceUtils.sendMessage(player, "close-scoreboard");
        } else {
            disablers.remove(player.getUniqueId());
            ResourceUtils.sendMessage(player, "open-scoreboard");
        }
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent event) {
        disablers.remove(event.getPlayer().getUniqueId());
    }
}
