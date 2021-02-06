package utes.customexp;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.HashMap;

public class CustomExpMechenism implements Listener {
    private static YamlConfiguration yaml;
    private static final HashMap<Integer, Integer> expNeedToUpgrade = new HashMap<Integer, Integer>();

    public CustomExpMechenism() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "customexp.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("customexp.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable")) continue;
            int level = Integer.valueOf(path) - 1;
            int exp = yaml.getInt(path);
            expNeedToUpgrade.put(level, exp);
        }

        System.out.println(expNeedToUpgrade);

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    @EventHandler
    public void onExp(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int exp = event.getAmount();
        int level = player.getLevel();
        float current = player.getExp() * expNeedToUpgrade.get(level);
        if (expNeedToUpgrade.containsKey(level)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (current + exp >= expNeedToUpgrade.get(level)) {
                        player.setLevel(player.getLevel() + 1);
                        if (expNeedToUpgrade.containsKey(level + 1))
                            player.setExp((current + exp - expNeedToUpgrade.get(level)) / expNeedToUpgrade.get(level));
                        else
                            player.setExp((current + exp - expNeedToUpgrade.get(level)) / getExpToLevel(level + 1));
                    } else if (current + exp < 0) {
                        player.setLevel(player.getLevel() - 1);
                        if (expNeedToUpgrade.containsKey(level - 1)) {
                            player.setExp((expNeedToUpgrade.get(level - 1) + current + exp) / expNeedToUpgrade.get(level - 1));
                        } else {
                            player.setExp((getExpToLevel(level - 1) + current + exp) / getExpToLevel(level - 1));
                        }
                    } else {
                        player.setExp((current + exp) / expNeedToUpgrade.get(level));
                    }
                }
            }.runTaskLater(UntilTheEndServer.getInstance(), 1L);
        }
    }

    private static int getExpToLevel(int level) {
        if (level <= 15)
            return 2 * level + 7;
        else if (level <= 30)
            return 5 * level - 38;
        else
            return 9 * level - 158;
    }
}
