package fts.pluginmanage;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class PluginManager {
    private static FunctionalToolSet main;
    private static org.bukkit.plugin.PluginManager BukkitPluginManager;
    private static Class BukkitPluginManager$;
    private static Field plugin$;
    private static Field command$;
    private static Field nameSpace$;
    private static Field knownCommands$;
    private static SimpleCommandMap simpleCommandMap;
    private static List<Plugin> pluginList;
    private static Map<String, Plugin> namespacesMap;
    private static Map<String, Command> registeredCommandsMap;

    static {
        BukkitPluginManager = Bukkit.getPluginManager();
        BukkitPluginManager$ = BukkitPluginManager.getClass();
        try {
            plugin$ = BukkitPluginManager$.getDeclaredField("plugins");
            plugin$.setAccessible(true);
            pluginList = (List<Plugin>) plugin$.get(BukkitPluginManager);

            nameSpace$ = BukkitPluginManager$.getDeclaredField("lookupNames");
            nameSpace$.setAccessible(true);
            namespacesMap = (Map<String, Plugin>) nameSpace$.get(BukkitPluginManager);

            command$ = BukkitPluginManager$.getDeclaredField("commandMap");
            command$.setAccessible(true);
            simpleCommandMap = (SimpleCommandMap) command$.get(BukkitPluginManager);
            knownCommands$ = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommands$.setAccessible(true);
            registeredCommandsMap = (Map<String, Command>) knownCommands$.get(simpleCommandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            FunctionalToolSet.getInstance().getLogger().info(
                    ResourceUtils.getLang("error-while-use-nms")
            );
            e.printStackTrace();
        }
    }

    public static void initialize(FunctionalToolSet plugin) {
        main = plugin;
    }

    public static void load(CommandSender sender, String fileName, boolean hasInfo) {
        File file = null;
        if (fileName.endsWith(".jar")) {
            file = new File("plugins", fileName);
        } else {
            file = new File("plugins", fileName + ".jar");
        }
        Plugin plugin = null;
        try {
            plugin = Bukkit.getPluginManager().loadPlugin(file);
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            for (File pluginFile : new File("plugins").listFiles()) {
                try {
                    PluginDescriptionFile desc = main.getPluginLoader().getPluginDescription(pluginFile);
                    if (desc.getName().equalsIgnoreCase(fileName)) {
                        file = pluginFile;
                        try {
                            plugin = Bukkit.getPluginManager().loadPlugin(file);
                        } catch (InvalidPluginException invalidPluginException) {
                            ResourceUtils.sendMessage(sender, "error-while-load-plugin");
                            return;
                        }
                        break;
                    }
                } catch (InvalidDescriptionException var11) {

                }
            }
        }
        plugin.onLoad();
        Bukkit.getPluginManager().enablePlugin(plugin);
        if (hasInfo) {
            ResourceUtils.sendMessage(sender, "plugin-load-successfully");
        }
    }

    public static void unload(CommandSender sender, String pluginName, boolean hasInfo) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            ResourceUtils.sendMessage(sender, "error-while-unload-plugin");
            return;
        }
        pluginList.remove(plugin);
        if (namespacesMap != null) {
            Object[] keySet = namespacesMap.keySet().toArray().clone();
            for (int index = 0; index < keySet.length; index++) {
                String nameSpace = (String) keySet[index];
                if (namespacesMap.get(nameSpace) == plugin) {
                    namespacesMap.remove(nameSpace);
                }
            }
        }
        if (registeredCommandsMap != null) {
            Object[] keySet = registeredCommandsMap.keySet().toArray().clone();
            for (int index = 0; index < keySet.length; index++) {
                Command originCmd = registeredCommandsMap.get((String) keySet[index]);
                if (!(originCmd instanceof PluginCommand)) {
                    continue;
                }
                PluginCommand cmd = (PluginCommand) originCmd;
                if (cmd.getPlugin() == plugin) {
                    registeredCommandsMap.remove(keySet[index]);
                    cmd.unregister(simpleCommandMap);
                }
            }
        }

        HandlerList.unregisterAll(plugin);
        Bukkit.getPluginManager().disablePlugin(plugin);
        System.gc();
        if (hasInfo) {
            ResourceUtils.sendMessage(sender, "plugin-unload-successfully");
        }
    }

    public static void reload(CommandSender sender, String pluginName, boolean hasInfo) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            ResourceUtils.sendMessage(sender, "error-while-reload-plugin");
            return;
        }
        unload(sender, pluginName, false);
        load(sender, pluginName, false);
        if (hasInfo) {
            ResourceUtils.sendMessage(sender, "plugin-reload-successfully");
        }
    }
}
