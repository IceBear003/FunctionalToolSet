package fts.info.chatbar;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class ChatListener extends ChatBar implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!repeatEnable) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPapi(AsyncPlayerChatEvent event) {
        if (!papiEnable || !FunctionalToolSet.hasPapi) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("fts.chat.papi")) {
            return;
        }
        event.setMessage(ResourceUtils.getPapi(player, event.getMessage()));
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
        if (!player.hasPermission("fts.cue.use")) {
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
