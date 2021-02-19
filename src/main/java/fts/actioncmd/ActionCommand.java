package fts.actioncmd;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ActionCommand {
    public static final HashMap<String, IActions> standardActions = new HashMap<>();
    public static final HashMap<UUID, DoneActions> playerActions = new HashMap<>();
    public static int judgeTime;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("actcmd.yml");
        File file = new File(plugin.getDataFolder(), "actcmd.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }
        judgeTime = yaml.getInt("judgeTime");

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerActions.put(player.getUniqueId(), new DoneActions(player.getUniqueId()));
        }

        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable") || path.equalsIgnoreCase("judgeTime")) {
                continue;
            }

            List<ActionType> types = new ArrayList<>();
            for (String toString : yaml.getStringList(path + ".actions")) {
                types.add(ActionType.valueOf(toString));
            }
            IActions actions = new IActions(types,
                    yaml.getIntegerList(path + ".values"),
                    yaml.getStringList(path + ".cmds"),
                    yaml.getStringList(path + ".messages"));
            standardActions.put(path, actions);
        }
        Bukkit.getPluginManager().registerEvents(new ActionListener(), plugin);
    }
}
