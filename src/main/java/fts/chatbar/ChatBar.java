package fts.chatbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fts.FunctionalToolSet;
import fts.ResourceUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

/*
 * TODO
 * fts.cue
 */
public class ChatBar implements Listener {
    public static YamlConfiguration yaml;
    private static HashMap<String, UUID> owners = new HashMap<>();
    private static HashMap<String, HashSet<UUID>> cues = new HashMap<>();
    private static String legacyHover;
    private static boolean hoverEnable;
    private static boolean tellEnable;
    private static boolean repeatEnable;
    private static String repeatButtom;
    private static String repeatHover;
    private static boolean cueEnable;
    private static String cueTitle;
    private static String cueSubtitle;
    private static String cueColor;


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

        Bukkit.getPluginManager().registerEvents(new ChatBar(), plugin);

        FunctionalToolSet.pm
                .addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin)
                        .serverSide().listenerPriority(ListenerPriority.NORMAL).gamePhase(GamePhase.PLAYING).optionAsync()
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
                            boolean isChat = false;
                            String message = TextComponent.toLegacyText(components);

                            for (String str : owners.keySet()) {
                                if (message.contains(str.replace("§r", "§f"))) {
                                    Player player = Bukkit.getPlayer(owners.get(str));
                                    owners.remove(str);
                                    String playerName = player.getDisplayName();
                                    String[] fixes = ResourceUtils.getPapi(player, message).split(playerName.replace("§r", "§f"));

                                    for (int i = 0; i < fixes.length; i++) {
                                        String passage = fixes[i];
                                        results.addAll(Arrays.asList(TextComponent.fromLegacyText(passage)));
                                        if (i == fixes.length - 1) {
                                            break;
                                        }
                                        BaseComponent[] tmp = getBaseComponents(player, playerName);
                                        results.addAll(Arrays.asList(tmp));
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
                                                BaseComponent[] otherComponents = getBaseComponents(other, cueColor + "@" + cueColor + otherName);
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!repeatEnable) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        event.setMessage(ResourceUtils.getPapi(event.getPlayer(), event.getMessage()));
        if (!repeatEnable) {
            return;
        }
        Player player = event.getPlayer();
        String message = event.getMessage();
        event.setMessage(message + repeatButtom);
        owners.put(message, player.getUniqueId());
    }

    @EventHandler
    public void onTab(PlayerChatTabCompleteEvent event) {
        if (!cueEnable) {
            return;
        }
        String current = event.getLastToken();
        if (current.contains("@")) {
            int index = current.lastIndexOf('@');
            String uncompletedName = current.substring(index + 1);

            ArrayList<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (current.equalsIgnoreCase("")) {
                    names.add(current.substring(0, index + 1) + player.getName());
                }
                if (player.getName().startsWith(uncompletedName)) {
                    names.add(current.substring(0, index + 1) + player.getName());
                }
            }

            event.getTabCompletions().addAll(names);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCue(AsyncPlayerChatEvent event) {
        if (!cueEnable) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("fts.cue")) {
            return;
        }
        String message = event.getMessage();
        String[] fixes = message.split("@");
        if (fixes.length == 1) {
            return;
        }
        HashSet<UUID> players = new HashSet<>();

        boolean hasCue = false;

        HashSet<Integer> notPlayerIndexes = new HashSet<>();

        for (int i = 1; i < fixes.length; i++) {
            boolean hasPlayer = false;
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (fixes[i].startsWith(other.getName())) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            other.sendTitle(cueTitle.replace("{player}", player.getName()),
                                    cueSubtitle.replace("{player}", player.getName()), 10, 70, 20);
                        }
                    }.runTaskLater(FunctionalToolSet.getInstance(), 3L);
                    fixes[i] = other.getName() + fixes[i].replaceFirst(other.getName(), "§r");
                    players.add(other.getUniqueId());
                    hasCue = true;
                    hasPlayer = true;
                    break;
                }
            }
            if (!hasPlayer) {
                notPlayerIndexes.add(i);
            }
        }
        if (hasCue) {
            String result = "";
            for (int i = 0; i < fixes.length - 1; i++) {
                if (notPlayerIndexes.contains(i + 1)) {
                    result += fixes[i] + "@";
                } else {
                    result += fixes[i] + cueColor + "@";
                }
            }
            result += fixes[fixes.length - 1];
            event.setMessage(result);
            cues.put(result, players);
            owners.put(result, player.getUniqueId());
        }
    }
}
