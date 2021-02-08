package utes.easycmd;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/*
 * TODO
 * utes.easycmd
 */
public class EasyCommand implements Listener {
    private static final HashMap<String, List<String>> cmds = new HashMap<String, List<String>>();
    private static final HashMap<String, String> strs = new HashMap<String, String>();
    private static YamlConfiguration yaml;

    public EasyCommand() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "easycmd.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("easycmd.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable"))
                continue;
            if (yaml.isList(path))
                cmds.put(path, yaml.getStringList(path));
            else
                strs.put(path, yaml.getString(path));
        }

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("utes.easycmd")) return;
        String cmd = event.getMessage();
        if (cmds.containsKey(cmd.substring(1))) {
            event.setCancelled(true);
            for (String line : cmds.get(cmd.substring(1)))
                player.performCommand(line);
        } else {
            String[] labels = cmd.substring(1).split(" ");
            String result = "";
            for (int i = 0; i < labels.length; i++) {
                String label = labels[i];
                for (String str : strs.keySet()) {
                    if (label.equalsIgnoreCase(str))
                        label = strs.get(str);
                }
                result += label;
                result += " ";
            }
            event.setMessage("/" + result);
        }
    }
}
