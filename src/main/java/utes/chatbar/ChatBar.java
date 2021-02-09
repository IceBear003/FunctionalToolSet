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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import utes.LanguageUtils;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ChatBar implements Listener {
    public static YamlConfiguration yaml;
    private static HashMap<String, UUID> owners = new HashMap<>();
    private static String legacyHover;
    private static boolean hoverEnable;
    private static boolean tellEnable;
    private static boolean repeatEnable;
    private static String repeatButtom;
    private static String repeatHover;


    public static void initialize(UntilTheEndServer plugin) {
        File file = new File(plugin.getDataFolder(), "chatbar.yml");
        if (!file.exists()) {
            plugin.saveResource("chatbar.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        legacyHover = yaml.getString("hover");
        hoverEnable = yaml.getBoolean("hoverEnable");
        tellEnable = yaml.getBoolean("tellEnable");
        repeatEnable = yaml.getBoolean("repeatEnable");
        repeatButtom = yaml.getString("repeatButtom");
        repeatHover = yaml.getString("repeatHover");

        Bukkit.getPluginManager().registerEvents(new ChatBar(), plugin);

        UntilTheEndServer.pm
                .addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin)
                        .serverSide().listenerPriority(ListenerPriority.LOW).gamePhase(GamePhase.PLAYING).optionAsync()
                        .options(ListenerOptions.SKIP_PLUGIN_VERIFIER).types(PacketType.Play.Server.CHAT)) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        PacketType packetType = event.getPacketType();
                        if (packetType.equals(PacketType.Play.Server.CHAT)) {
                            if (packet.getChatTypes().getValues().get(0) != EnumWrappers.ChatType.SYSTEM) {
                                return;
                            }
                            WrappedChatComponent warppedComponent = packet.getChatComponents().getValues().get(0);
                            String json = warppedComponent.getJson();
                            BaseComponent[] components = ComponentSerializer.parse(json);
                            ArrayList<BaseComponent> results = new ArrayList<>();
                            boolean flag = false;
                            String message = TextComponent.toLegacyText(components);

                            for (String str : owners.keySet()) {
                                if (message.contains(str)) {
                                    Player player = Bukkit.getPlayer(owners.get(str));
                                    owners.remove(str);
                                    String playerName = player.getName();

                                    String[] fixes = LanguageUtils.getPapi(player, message).split(playerName);

                                    for (int i = 0; i < fixes.length; i++) {
                                        String passage = fixes[i];
                                        results.addAll(Arrays.asList(TextComponent.fromLegacyText(passage)));
                                        if (i == fixes.length - 1) {
                                            break;
                                        }
                                        BaseComponent tmp = getBaseComponents(player);
                                        results.add(tmp);
                                    }

                                    results.get(results.size() - 1).setClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/say " + str));
                                    results.get(results.size() - 1).setHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(repeatHover)));

                                    flag = true;
                                    break;
                                }
                            }

                            if (!flag) {
                                return;
                            }

                            BaseComponent[] tmp = new BaseComponent[results.size()];
                            int tot = 0;
                            for (BaseComponent element : results) {
                                tmp[tot++] = element;
                            }

                            String newJson = ComponentSerializer.toString(tmp);
                            warppedComponent.setJson(newJson);
                            packet.getChatComponents().write(0, warppedComponent);
                        }
                    }
                });
    }

    private static BaseComponent getBaseComponents(Player player) {
        BaseComponent component = TextComponent.fromLegacyText(player.getName())[0];
        String legacy = LanguageUtils.getPapi(player, legacyHover);
        BaseComponent[] hover = TextComponent.fromLegacyText(legacy);
        if (hoverEnable) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        }
        if (tellEnable) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + player.getName() + " "));
        }
        return component;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setMessage(LanguageUtils.getPapi(event.getPlayer(), event.getMessage()));
        if (!repeatEnable) {
            return;
        }
        Player player = event.getPlayer();
        String message = event.getMessage();
        event.setMessage(message + repeatButtom);
        owners.put(message, player.getUniqueId());
    }
}
