package utes;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class LanguageUtils {
    private static UntilTheEndServer plugin;
    private static YamlConfiguration lang;

    public static void initialize(UntilTheEndServer plugin) {
        LanguageUtils.plugin = plugin;

        plugin.saveResource("config.yml", false);
        plugin.saveResource(plugin.getConfig().getString("language"), false);

        File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("language"));
        lang = YamlConfiguration.loadConfiguration(file);
    }

    public static String getLang(String path) {
        return lang.getString(path);
    }

    public static String getPapi(Player player, String origin) {
        try {
            return PlaceholderAPI.setPlaceholders(player, origin);
        } catch (NoClassDefFoundError error) {
            return origin;
        }
    }
}
