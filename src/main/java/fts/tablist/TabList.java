package fts.tablist;

import fts.FunctionalToolSet;
import fts.spi.NMSManager;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TabList implements Listener {
    private static List<String> head;
    private static List<String> foot;
    private static String headMsg = "";
    private static String footMsg = "";

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("tablist.yml");
        File file = new File(plugin.getDataFolder(), "tablist.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        head = yaml.getStringList("head");
        foot = yaml.getStringList("foot");

        for (int index = 0; index < head.size(); index++) {
            if (index == head.size() - 1) {
                headMsg += head.get(index);
            } else {
                headMsg += head.get(index) + "\n";
            }
        }
        for (int index = 0; index < foot.size(); index++) {
            if (index == foot.size() - 1) {
                footMsg += foot.get(index);
            } else {
                footMsg += foot.get(index) + "\n";
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player, playerAmount);
        }

        Bukkit.getPluginManager().registerEvents(new TabList(), plugin);
    }

    private static Class Packet;
    private static Class ChatSerializer;
    private static Class CraftPlayer;
    private static Class PacketPlayOutPlayerListHeaderFooter;
    private static Method a;

    static {
        Packet = NMSManager.getClass("Packet");
        ChatSerializer = NMSManager.getClass("IChatBaseComponent$ChatSerializer");
        CraftPlayer = NMSManager.getClass("entity.CraftPlayer");
        PacketPlayOutPlayerListHeaderFooter = NMSManager.getClass("PacketPlayOutPlayerListHeaderFooter");
        try {
            a = ChatSerializer.getMethod("a", Class.forName("java.lang.String"));
        } catch (NoSuchMethodException | ClassNotFoundException | NullPointerException e) {
            FunctionalToolSet.getInstance().getLogger().info("Java内部错误！");
        }
    }

    private static int playerAmount = 0;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player, playerAmount);
        }
    }

    private static void updateTabList(Player player, int playerAmount) {
        try {
            Object header = a.invoke(null, "{\"text\":\"" + ResourceUtils.getPapi(player, headMsg) + "\"}");
            Object footer = a.invoke(null, "{\"text\":\"" + ResourceUtils.getPapi(player, footMsg) + "\"}");

            Object packet = PacketPlayOutPlayerListHeaderFooter.newInstance();

            Field fieldA, fieldB;
            try {
                fieldA = packet.getClass().getDeclaredField("a");
                fieldB = packet.getClass().getDeclaredField("b");
            } catch (NoSuchFieldException e) {
                fieldA = packet.getClass().getDeclaredField("header");
                fieldB = packet.getClass().getDeclaredField("footer");
            }
            fieldA.setAccessible(true);
            fieldA.set(packet, header);
            fieldB.setAccessible(true);
            fieldB.set(packet, footer);

            Object craftPlayer = CraftPlayer.cast(player);
            Method getHandle = craftPlayer.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(craftPlayer, null);

            Field fieldC = handle.getClass().getDeclaredField("playerConnection");
            fieldC.setAccessible(true);
            Object playerConnection = fieldC.get(handle);
            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", Packet);
            sendPacket.invoke(playerConnection, packet);
        } catch (IllegalAccessException | NullPointerException |
                InvocationTargetException | NoSuchFieldException |
                InstantiationException | NoSuchMethodException e) {
            FunctionalToolSet.getInstance().getLogger().info("NMS内部错误！请检查版本！");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player, playerAmount);
        }
    }
}
