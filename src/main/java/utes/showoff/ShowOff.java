package utes.showoff;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import utes.NMSManager;
import utes.UntilTheEndServer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/* TODO
 * utes.showoff.can
 * utes.showoff.ignorecd
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

        UntilTheEndServer.pm
                .addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(UntilTheEndServer.getInstance())
                        .serverSide().listenerPriority(ListenerPriority.LOW).gamePhase(GamePhase.PLAYING).optionAsync()
                        .options(ListenerOptions.SKIP_PLUGIN_VERIFIER).types(PacketType.Play.Server.CHAT)) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        PacketType packetType = event.getPacketType();
                        if (packetType.equals(PacketType.Play.Server.CHAT)) {
                            if (packet.getChatTypes().getValues().get(0) != EnumWrappers.ChatType.SYSTEM)
                                return;
                            WrappedChatComponent warppedComponent = packet.getChatComponents().getValues().get(0);
                            String json = warppedComponent.getJson();

                            BaseComponent[] origin = ComponentSerializer.parse(json);
                            String message = TextComponent.toLegacyText(origin);

                            for(String str:owners.keySet()){
                                if(message.contains(str)){
                                    BaseComponent[] adapted=getBaseComponents(Bukkit.getPlayer(owners.get(str)),message);
                                    String newJson = ComponentSerializer.toString(adapted);
                                    warppedComponent.setJson(newJson);
                                    packet.getChatComponents().write(0, warppedComponent);
                                }
                            }
                        }
                    }
                });
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("utes.showoff.can"))
            return;
        String message = event.getMessage();
        if (message.contains(string)) {
            if (lastShowOffStamp.containsKey(player.getUniqueId())) {
                long lastUse = lastShowOffStamp.get(player.getUniqueId());
                if (System.currentTimeMillis() - lastUse < cooldown * 1000 && !player.hasPermission("utes.showoff.ignorecd")) {
                    player.sendMessage("炫耀物品失败，请等待冷却！");
                }
            }
            lastShowOffStamp.put(player.getUniqueId(), System.currentTimeMillis());
            owners.put(message,player.getUniqueId());
        }
    }

    private static HashMap<String,UUID> owners=new HashMap<String,UUID>();

    private static BaseComponent[] getBaseComponents(Player player,String message){
        String[] tmp = message.split(string);
        HashMap<Integer,Integer> indexes = new HashMap<Integer,Integer>();
        for (int i = 1; i < tmp.length; i++) {
            if (tmp[i].length() <= 0) continue;
            if (!(tmp[i].charAt(0) <= '9' && tmp[i].charAt(0) >= '1')) continue;
            indexes.put(i,Integer.valueOf(tmp[i].charAt(0) - 49));
            tmp[i]=tmp[i].substring(1);
        }

        ArrayList<TextComponent> textes = new ArrayList<TextComponent>();

        int cnt=1,tot=0;
        String first = tmp[0];
        textes.add(new TextComponent(first));
        for (int i = 1; i < tmp.length; i++) {
            if(indexes.containsKey(i)) {
                ItemStack item = player.getInventory().getItem(indexes.get(i));
                textes.add(itemToTextComponent(item)); cnt++;
            }
            textes.add(new TextComponent(tmp[i])); cnt++;
        }
        String newMessage="";
        TextComponent[] tmps = new TextComponent[cnt];
        for(TextComponent text:textes){
            tmps[tot++]=text;

        }
        return (BaseComponent[]) tmps;
    }

    private static String getJsonMessage(ItemStack itemStack) {
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

    private static TextComponent itemToTextComponent(ItemStack item) {
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