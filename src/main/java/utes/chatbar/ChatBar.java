package utes.chatbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ChatBar implements Listener {
    public static YamlConfiguration yaml;
    private static HashMap<String, UUID> owners = new HashMap<String, UUID>();
    private static ArrayList<UUID> cooldowning = new ArrayList<UUID>();
    private static String legacyHover;

    public ChatBar() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "chatbar.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("chatbar.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable"))
            return;

        legacyHover = yaml.getString("hover");

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
                            BaseComponent[] components = ComponentSerializer.parse(json);
                            BaseComponent[] results = new BaseComponent[100];
                            int tot = 0;

                            String message = TextComponent.toLegacyText(components);
//TODO
                            for (String str : owners.keySet()) {
                                if (message.contains(str)) {
                                    Player player = Bukkit.getPlayer(owners.get(str));
                                    System.out.println(player.getName());
                                    String playerName = player.getName();
                                    owners.remove(str);
                                    String[] fixes = str.split(playerName);

                                    for (int i = 0; i < fixes.length; i++) {
                                        String passage = fixes[i];
                                        results[tot++] = TextComponent.fromLegacyText(passage)[0];
                                        if (i == fixes.length - 1)
                                            break;
                                        BaseComponent tmp = getBaseComponents(player);
                                        results[tot++] = tmp;
                                    }

                                    components[components.length - 1].setClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/say " + str));
                                    components[components.length - 1].setHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§c点击+1复读")));

                                    break;
                                }
                            }

                            String newJson = ComponentSerializer.toString(results);
                            warppedComponent.setJson(newJson);
                            packet.getChatComponents().write(0, warppedComponent);
                        }
                    }
                });
    }

    private static BaseComponent getBaseComponents(Player player) {
        BaseComponent component = TextComponent.fromLegacyText(player.getName())[0];
        String legacy = UntilTheEndServer.getPapi(player, legacyHover);
        BaseComponent[] hover = TextComponent.fromLegacyText(legacy);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + player.getName() + " "));
        return component;
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        event.setMessage(message + " §c§l[+1]");
        owners.put(message, player.getUniqueId());
    }
}
