package fts.info.motd;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.File;
import java.util.List;

public class MotdManager implements Listener {
    private static int maxPlayer;
    private static String motd = "";

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("motd.yml");
        File file = new File(plugin.getDataFolder(), "motd.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        maxPlayer = yaml.getInt("maxPlayer");
        if (maxPlayer == 0) {
            maxPlayer = Bukkit.getMaxPlayers();
        }

        List<String> lines = yaml.getStringList("motd");
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (index == lines.size() - 1) {
                motd += line;
            } else {
                motd += line + "\n";
            }
        }

        String iconFile = yaml.getString("icon");
        if (!iconFile.equalsIgnoreCase("")) {
            try {
                Bukkit.loadServerIcon(new File(iconFile));
            } catch (Exception e) {
                plugin.getLogger().info(
                        ResourceUtils.getLang("error-while-load-motd-png")
                );
            }
        }

        Bukkit.getPluginManager().registerEvents(new MotdManager(), plugin);
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        event.setMotd(motd);
        if (maxPlayer == -1) {
            event.setMaxPlayers(Bukkit.getOnlinePlayers().size() + 1);
        } else {
            event.setMaxPlayers(maxPlayer);
        }
        event.setServerIcon(Bukkit.getServerIcon());
    }
}
