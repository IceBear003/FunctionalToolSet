package utes.chatbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import utes.UntilTheEndServer;

import java.io.File;

//TODO
public class ChatBar {
    public static YamlConfiguration yaml;

    public ChatBar() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "chatbar.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("chatbar.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable"))
            return;

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
                            BaseComponent[] answers = components;

                            String message = TextComponent.toLegacyText(components);
//                            if (message.lastIndexOf("[start]") != message.indexOf("[start]"))
//                                return;
//                            if (message.lastIndexOf("[end]") != message.indexOf("[end]"))
//                                return;

                            if (message.contains("[start]") && message.contains("[end]")) {
                                String tmp1 = message.split("[" + "start" + "]")[2];
                                String tmp2 = message.split("[" + "end" + "]")[1];
                                String result = getMaxString(tmp1, tmp2);

                                System.out.println(message.split("[" + "start" + "]")[2]);
                                System.out.println(message.split("[" + "end" + "]")[1]);
                                String origin_result = "[start]" + result + "[end]";
                                json.replace(origin_result, "[按钮]");

                                int tot = 0;
                                for (BaseComponent component : components) {
                                    if (component.toLegacyText().contains("[按钮]")) {
                                        component.setBold(true);
                                        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, result));
                                    }
                                    answers[tot] = component;
                                    tot++;
                                }
                            }

                            String newJson = ComponentSerializer.toString(answers);
                            warppedComponent.setJson(newJson);
                            packet.getChatComponents().write(0, warppedComponent);
                        }
                    }
                });
    }

    private static String getMaxString(String str1, String str2) {
        String max = null;
        String min = null;
        max = (str1.length() > str2.length() ? str1 : str2);
        min = max.equals(str1) ? str2 : str1;
        for (int i = 0; i < min.length(); i++) {
            for (int start = 0, end = min.length() - i; end != min.length() + 1; start++, end++) {
                String sub = min.substring(start, end);
                if (max.contains(sub))
                    return sub;
            }
        }
        return null;
    }
}
