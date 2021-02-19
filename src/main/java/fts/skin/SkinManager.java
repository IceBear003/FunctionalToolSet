package fts.skin;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

public class SkinManager {
    private static FunctionalToolSet main;
    private static HashMap<String, String> urls = new HashMap<String, String>();

    public static void initialize(FunctionalToolSet plugin) {
        main = plugin;

        ResourceUtils.autoUpdateConfigs("skins.yml");
        File file = new File(plugin.getDataFolder(), "skins.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for (String path : yaml.getKeys(false)) {
            urls.put(path, yaml.getString(path));
        }
    }

    public static void setSkin(CommandSender sender, Player player, String skinName) {
        String value = getValue(player, skinName);
        if (value.equalsIgnoreCase("")) {
            return;
        }
        PropertyMap props = ((CraftPlayer) player).getHandle().getProfile().getProperties();
        props.clear();
        props.put("textures", new Property("textures", value, (String) null));
        update(player);

        sender.sendMessage("设置完成");
    }

    private static String getValue(Player player, String skinName) {
        if (!urls.containsKey(skinName)) {
            return "";
        }
        String url = urls.get(skinName);
        String json = "{\n" +
                "  \"timestamp\" : " + System.currentTimeMillis() + ",\n" +
                "  \"profileId\" : \"" + player.getUniqueId().toString().replace("-", "") + "\",\n" +
                "  \"profileName\" : \"" + player.getName() + "\",\n" +
                "  \"textures\" : {\n" +
                "    \"SKIN\" : {\n" +
                "      \"url\" : \"" + url + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return Base64.encodeBase64String(json.getBytes());
    }

    private static void update(Player player) {
        for (Player each : Bukkit.getOnlinePlayers()) {
            try {
                each.hidePlayer(main, player);
            } catch (NoSuchMethodError ignored) {
                each.hidePlayer(player);
            }
            try {
                each.showPlayer(main, player);
            } catch (NoSuchMethodError ignored) {
                each.showPlayer(player);
            }
        }
    }
}
