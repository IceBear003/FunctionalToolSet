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
    private static HashMap<String, Property> urls = new HashMap<String, Property>();

    public static void initialize(FunctionalToolSet plugin) {
        main = plugin;

        ResourceUtils.autoUpdateConfigs("skins.yml");
        File file = new File(plugin.getDataFolder(), "skins.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for (String path : yaml.getKeys(false)) {
            String url = yaml.getString(path);
            urls.put(path, new Property("textures", getValue(url), null));
        }
    }

    public static void setSkin(CommandSender sender, Player player, String skinName) {
        if (!urls.containsKey(skinName)) {
            return;
        }
        PropertyMap props = ((CraftPlayer) player).getHandle().getProfile().getProperties();
        props.get("textures").clear();
        System.out.println(urls.get(skinName));
        props.put("textures", urls.get(skinName));
        update(player);

        sender.sendMessage("设置完成");
    }

    private static String getValue(String url) {
        String json = "{\n" +
                "  \"timestamp\" : 1613780104071,\n" +
                "  \"profileId\" : \"0a1c1a3b135643f28ca58d0120c1e976\",\n" +
                "  \"profileName\" : \"HamsterYDS\",\n" +
                "  \"signatureRequired\" : false,\n" +
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
            each.hidePlayer(player);
            each.showPlayer(player);
        }
    }
}
