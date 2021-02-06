package utes.showoff;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import utes.NMSManager;
import utes.UntilTheEndServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/* TODO
 * utes.showoff
 */
public class ShowOff implements Listener {
    private static HashMap<UUID, Long> lastShowOffStamp = new HashMap<UUID, Long>();
    private static YamlConfiguration yaml;
    private static int cooldown;
    private static String string;

    public ShowOff() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "showoff.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("showoff.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }
        cooldown = yaml.getInt("cooldown");
        string = yaml.getString("string");

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("utes.showoff")) return;
        String message = event.getMessage();
        if (message.contains(string)) {
            String[] tmp = message.split(string);
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            for (int i = 1; i < tmp.length; i++) {
                if (tmp[i].length() <= 0) continue;
                if (!(tmp[i].charAt(0) <= '9' && tmp[i].charAt(0) >= '1')) continue;
                indexes.add(Integer.valueOf(tmp[i].charAt(0) - 48));
            }

            if (lastShowOffStamp.containsKey(player.getUniqueId())) {
                long lastUse = lastShowOffStamp.get(player.getUniqueId());
                if (System.currentTimeMillis() - lastUse < cooldown * 1000) {
                    player.sendMessage("炫耀物品失败，请等待冷却！");
                    return;
                }
                lastShowOffStamp.put(player.getUniqueId(), System.currentTimeMillis());
            }

            ArrayList<TextComponent> textes = new ArrayList<TextComponent>();

            int tot = 0;
            for (int i = 0; i < tmp.length; i++) {
                if (i == 0) {
                    String first = tmp[i];
                    textes.add(new TextComponent(first));
                } else {
                    ItemStack item = player.getInventory().getItem(indexes.get(tot));
                    textes.add(itemToTextComponent(item));
                    textes.add(new TextComponent(tmp[i].replace(string + indexes.get(tot++), "")));
                }
            }

            TextComponent[] tmps = (TextComponent[]) textes.toArray();
            player.spigot().sendMessage((BaseComponent[]) tmps);
        }
    }

    public String getJsonMessage(ItemStack itemStack) {
        Class clazz1 = NMSManager.getClass("inventory.CraftItemStack"),
                clazz2 = NMSManager.getClass("ItemStack"),
                clazz3 = NMSManager.getClass("NBTTagCompound");
        Method method1, method2;
        try {
            method1 = clazz1.getMethod("asNMSCopy",
                    new Class[]{
                            ItemStack.class
                    });
            method2 = clazz2.getMethod("save",
                    new Class[]{
                            clazz3
                    });
        } catch (NoSuchMethodException e) {
            UntilTheEndServer.getInstance().getLogger().info("nms内部错误，请检查版本！");
            return null;
        }

        Object result;
        try {
            result = method2.invoke(
                    method1.invoke(null, new Object[]{itemStack}),
                    new Object[]{clazz3.newInstance()});
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            UntilTheEndServer.getInstance().getLogger().info("nms内部错误，请检查版本！");
            return null;
        }

        return result.toString();
    }

    public TextComponent itemToTextComponent(ItemStack item) {
        String json = getJsonMessage(item);
        BaseComponent[] hoverEventComponents = {new TextComponent(json)};
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);

        TextComponent component = new TextComponent("§b[" +
                item.getAmount() + "×" +
                getName(item) + "]");
        component.setHoverEvent(event);
        return component;
    }

    private static String getName(ItemStack item) {
        if (item.hasItemMeta())
            if (item.getItemMeta().hasDisplayName())
                return item.getItemMeta().getDisplayName();
        return item.getType().toString();
    }
}
