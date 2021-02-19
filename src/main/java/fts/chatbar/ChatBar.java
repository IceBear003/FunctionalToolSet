package fts.chatbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ChatBar {
    public static YamlConfiguration yaml;
    protected static HashMap<String, UUID> owners = new HashMap<>();
    protected static HashMap<String, HashSet<UUID>> cues = new HashMap<>();
    protected static String legacyHover;
    protected static boolean hoverEnable;
    protected static boolean tellEnable;
    protected static boolean repeatEnable;
    protected static String repeatButtom;
    protected static String repeatHover;
    protected static boolean cueEnable;
    protected static String cueTitle;
    protected static String cueSubtitle;
    protected static String cueColor;
    protected static boolean papiEnable;


    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("chatbar.yml");
        File file = new File(plugin.getDataFolder(), "chatbar.yml");
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
        cueEnable = yaml.getBoolean("cueEnable");
        cueTitle = yaml.getString("cueTitle");
        cueSubtitle = yaml.getString("cueSubtitle");
        cueColor = yaml.getString("cueColor").replace("&", "§");
        papiEnable = yaml.getBoolean("papiEnable");

        Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);

        FunctionalToolSet.pm
                .addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin)
                        .serverSide().listenerPriority(ListenerPriority.NORMAL).gamePhase(GamePhase.PLAYING).optionAsync()
                        .options(ListenerOptions.SKIP_PLUGIN_VERIFIER).types(PacketType.Play.Server.CHAT)) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        PacketType packetType = event.getPacketType();
                        if (packetType.equals(PacketType.Play.Server.CHAT)) {
                            if (packet.getChatTypes() == null) {
                                return;
                            }
                            if (packet.getChatTypes().getValues().get(0) != EnumWrappers.ChatType.SYSTEM) {
                                return;
                            }
                            WrappedChatComponent warppedComponent = packet.getChatComponents().getValues().get(0);
                            String json = warppedComponent.getJson();
                            BaseComponent[] components = ComponentSerializer.parse(json);
                            ArrayList<BaseComponent> results = new ArrayList<>();
                            boolean isChat = false;
                            String message = TextComponent.toLegacyText(components);

                            for (String str : owners.keySet()) {
                                if (message.contains(str.replace("§r", "§f"))) {
                                    Player player = Bukkit.getPlayer(owners.get(str));
                                    owners.remove(str);
                                    String playerName = player.getDisplayName();
                                    String[] fixes = message.split(playerName.replace("§r", "§f"));

                                    for (int i = 0; i < fixes.length; i++) {
                                        String passage = fixes[i];
                                        if (i == fixes.length - 1) {
                                            results.addAll(Arrays.asList(TextComponent.fromLegacyText(passage)));
                                        } else {
                                            if (i == 0) {
                                                results.addAll(Arrays.asList(TextComponent.fromLegacyText(passage)));
                                                BaseComponent[] tmp = getBaseComponents(player, playerName);
                                                results.addAll(Arrays.asList(tmp));
                                            } else {
                                                results.addAll(Arrays.asList(TextComponent.fromLegacyText(passage + playerName)));
                                            }
                                        }
                                    }

                                    results.get(results.size() - 1).setClickEvent(
                                            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, str.replace("§", "&")));
                                    results.get(results.size() - 1).setHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(repeatHover)));

                                    isChat = true;
                                    break;
                                }
                            }

                            if (!isChat) {
                                results.addAll(Arrays.asList(components));
                            }

                            boolean containsCue = false;
                            BaseComponent[] tmp = new BaseComponent[results.size()];
                            int tot = 0;
                            for (BaseComponent element : results) {
                                tmp[tot++] = element;
                            }

                            ArrayList<BaseComponent> finalResult = new ArrayList<>();
                            for (String str : cues.keySet()) {
                                if (message.contains(str.replace("§r", "§f"))) {
                                    HashSet<UUID> players = (HashSet<UUID>) cues.get(str).clone();
                                    cues.remove(str);
                                    for (BaseComponent origin : tmp) {
                                        for (UUID uuid : players) {
                                            Player other = Bukkit.getPlayer(uuid);
                                            String otherName = other.getName();
                                            if (origin.toLegacyText().startsWith(cueColor + "@" + otherName)) {
                                                BaseComponent[] otherComponents = getBaseComponents(other, cueColor + "@" + otherName);
                                                finalResult.addAll(Arrays.asList(otherComponents));
                                                break;
                                            } else {
                                                finalResult.add(origin);
                                            }
                                        }
                                    }
                                    containsCue = true;
                                    break;
                                }
                            }

                            if (!containsCue) {
                                finalResult.addAll(Arrays.asList(tmp));
                            }

                            BaseComponent[] tmp2 = new BaseComponent[finalResult.size()];
                            int tot2 = 0;
                            for (BaseComponent element : finalResult) {
                                tmp2[tot2++] = element;
                            }

                            String newJson = ComponentSerializer.toString(tmp2);
                            warppedComponent.setJson(newJson);
                            packet.getChatComponents().write(0, warppedComponent);
                        }
                    }
                });
    }

    private static BaseComponent[] getBaseComponents(Player player, String displayName) {
        BaseComponent[] components = TextComponent.fromLegacyText(displayName);
        String legacy = ResourceUtils.getPapi(player, legacyHover);
        BaseComponent[] hover = TextComponent.fromLegacyText(legacy);
        if (hoverEnable) {
            for (BaseComponent component : components) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            }
        }
        if (tellEnable) {
            for (BaseComponent component : components) {
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + player.getName() + " "));
            }
        }
        return components;
    }
}
