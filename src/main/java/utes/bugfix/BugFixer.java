package utes.bugfix;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

/* TODO
 * utes.ignorebugs
 */
public class BugFixer implements Listener {
    public static void initialize(UntilTheEndServer plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("utes.ignorebugs")) {
                        continue;
                    }
                    player.getInventory().remove(Material.BOOK_AND_QUILL);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
