package utes.information;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import utes.ResourceUtils;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.List;

public class TranslateMessage {
    private static List<String> origins;
    private static List<String> adapteds;
    private static String prefix;

    public static void initialize(UntilTheEndServer plugin) {
        ResourceUtils.autoUpdateConfigs("information.yml");
        File file = new File(plugin.getDataFolder(), "information.yml");
        YamlConfiguration yaml;
        yaml = YamlConfiguration.loadConfiguration(file);

        origins = yaml.getStringList("origins");
        adapteds = yaml.getStringList("adapted");
        prefix = yaml.getString("prefix");

        BaseComponent[] prefixAdapted = TextComponent.fromLegacyText(prefix);

        UntilTheEndServer.pm
                .addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin)
                        .serverSide().listenerPriority(ListenerPriority.LOW).gamePhase(GamePhase.PLAYING).optionAsync()
                        .options(ListenerOptions.SKIP_PLUGIN_VERIFIER).types(PacketType.Play.Server.CHAT)) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        PacketType packetType = event.getPacketType();
                        if (packetType.equals(PacketType.Play.Server.CHAT)) {
                            if (packet.getChatTypes().getValues().get(0) != ChatType.SYSTEM) {
                                return;
                            }
                            WrappedChatComponent warppedComponent = packet.getChatComponents().getValues().get(0);

                            String json = warppedComponent.getJson();
                            for (int index = 0; index < origins.size(); index++) {
                                json = json.replace(origins.get(index), adapteds.get(index));
                            }
                            boolean flag = json.contains("{ignore}");
                            json = json.replace("{ignore}", "");

                            BaseComponent[] origin = ComponentSerializer.parse(json);
                            BaseComponent[] adapted = new BaseComponent[origin.length];
                            int tot = 0;
                            String message = TextComponent.toLegacyText(origin);
                            if (!message.contains(prefix) && !flag) {
                                adapted = new BaseComponent[prefixAdapted.length + origin.length];
                                for (BaseComponent component : prefixAdapted) {
                                    adapted[tot++] = component;
                                }
                            }
                            for (BaseComponent component : origin) {
                                adapted[tot++] = component;
                            }

                            String newJson = ComponentSerializer.toString(adapted);

                            warppedComponent.setJson(newJson);
                            packet.getChatComponents().write(0, warppedComponent);
                        }
                    }
                });

    }
}
