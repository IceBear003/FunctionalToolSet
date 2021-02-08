package utes.bancmd;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/* TODO
 * utes.cmdban.ignore
 */
public class CommandBanner implements Listener {
    private static final HashMap<String, List<String>> worlds = new HashMap<String, List<String>>();
    private static YamlConfiguration yaml;

    public CommandBanner() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "cmdban.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("cmdban.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable"))
                continue;
            worlds.put(path, yaml.getStringList(path));
        }

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("utes.cmdban.ignore")) return;
        World world = player.getWorld();
        String cmd = event.getMessage();
        while (cmd.startsWith(" ")) {
            cmd.replaceFirst(" ", "");
        }
        while (cmd.startsWith("/ ")) {
            cmd.replaceFirst(" ", "");
        }
        if (!worlds.containsKey(world.getName())) {
            return;
        }
        for (String label : worlds.get(world.getName())) {
            if (cmd.startsWith("/" + label)) {
                event.setCancelled(true);
                player.sendMessage("该世界无法使用此指令");
            }
        }
    }
}
