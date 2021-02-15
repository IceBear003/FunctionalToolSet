package fts.joincmd;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.util.List;

public class JoinCommand implements Listener {
    private static String usualMessage;
    private static String firstMessage;
    private static String usualTitle;
    private static String firstTitle;
    private static String usualSubtitle;
    private static String firstSubtitle;
    private static String usualBroadcast;
    private static String firstBroadcast;
    private static boolean usualIsOp;
    private static boolean firstIsOp;
    private static List<String> usualCommands;
    private static List<String> firstCommands;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("joincmd.yml");
        File file = new File(plugin.getDataFolder(), "joincmd.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        usualMessage = yaml.getString("usualJoin.message");
        usualTitle = yaml.getString("usualJoin.title");
        usualSubtitle = yaml.getString("usualJoin.subtitle");
        usualBroadcast = yaml.getString("usualJoin.broadcast");
        usualIsOp = yaml.getBoolean("usualJoin.isOp");
        usualCommands = yaml.getStringList("usualJoin.cmds");

        firstMessage = yaml.getString("firstJoin.message");
        firstTitle = yaml.getString("firstJoin.title");
        firstSubtitle = yaml.getString("firstJoin.subtitle");
        firstBroadcast = yaml.getString("firstJoin.broadcast");
        firstIsOp = yaml.getBoolean("firstJoin.isOp");
        firstCommands = yaml.getStringList("firstJoin.cmds");

        Bukkit.getPluginManager().registerEvents(new JoinCommand(), plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            event.setJoinMessage(usualMessage.equals("") ? null : usualMessage.replace("{player}", player.getName()));
            player.sendTitle(usualTitle.replace("{player}", player.getName()), usualSubtitle.replace("{player}", player.getName()), 10, 70, 20);
            if (!usualBroadcast.equalsIgnoreCase("")) {
                Bukkit.broadcastMessage(usualBroadcast.replace("{player}", player.getName()));
            }
            if (usualIsOp) {
                boolean isOp = player.isOp();
                player.setOp(usualIsOp);
                for (String cmd : usualCommands) {
                    player.performCommand(cmd.replace("{player}", player.getName()));
                }
                player.setOp(isOp);
            } else {
                for (String cmd : usualCommands) {
                    player.performCommand(cmd.replace("{player}", player.getName()));
                }
            }
        } else {
            event.setJoinMessage(firstMessage.equals("") ? null : firstMessage.replace("{player}", player.getName()));
            player.sendTitle(firstTitle.replace("{player}", player.getName()), firstSubtitle.replace("{player}", player.getName()), 10, 70, 20);
            if (!firstBroadcast.equalsIgnoreCase("")) {
                Bukkit.broadcastMessage(firstBroadcast.replace("{player}", player.getName()));
            }
            if (firstIsOp) {
                boolean isOp = player.isOp();
                player.setOp(firstIsOp);
                for (String cmd : firstCommands) {
                    player.performCommand(cmd.replace("{player}", player.getName()));
                }
                player.setOp(isOp);
            } else {
                for (String cmd : firstCommands) {
                    player.performCommand(cmd.replace("{player}", player.getName()));
                }
            }
        }

    }
}
