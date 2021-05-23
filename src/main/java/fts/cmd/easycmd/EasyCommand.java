package fts.cmd.easycmd;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class EasyCommand implements Listener {
    private static final HashMap<String, List<String>> cmds = new HashMap<>();
    private static final HashMap<String, String> strs = new HashMap<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("easycmd.yml");
        File file = new File(plugin.getDataFolder(), "easycmd.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable")) {
                continue;
            }
            if (yaml.isList(path)) {
                cmds.put(path, yaml.getStringList(path));
            } else {
                strs.put(path, yaml.getString(path));
            }
        }

        Bukkit.getPluginManager().registerEvents(new EasyCommand(), plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        String cmd = event.getMessage();
        if (cmds.containsKey(cmd.substring(1))) {
            event.setCancelled(true);
            for (String line : cmds.get(cmd.substring(1))) {
                player.performCommand(line);
            }
        } else {
            String[] labels = cmd.substring(1).split(" ");
            StringBuilder result = new StringBuilder();
            for (String s : labels) {
                String label = s;
                for (String str : strs.keySet()) {
                    if (label.equalsIgnoreCase(str)) {
                        label = strs.get(str);
                    }
                }
                result.append(label);
                result.append(" ");
            }
            event.setMessage("/" + result);
        }
    }
}
