package utes.information;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import utes.ResourceUtils;
import utes.UntilTheEndServer;

import java.io.File;

public class NoLoginQuitMessage implements Listener {
    public static void initialize(UntilTheEndServer plugin) {
        ResourceUtils.autoUpdateConfigs("information.yml");
        File file = new File(plugin.getDataFolder(), "information.yml");
        YamlConfiguration yaml;
        yaml = YamlConfiguration.loadConfiguration(file);

        if (yaml.getBoolean("enableJoinMessage")) {
            Bukkit.getPluginManager().registerEvents(new NoLoginQuitMessage(), plugin);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

}
