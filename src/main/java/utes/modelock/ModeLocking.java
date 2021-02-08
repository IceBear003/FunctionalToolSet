package utes.modelock;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.Collection;

/* TODO
 * utes.modelock
 */
public class ModeLocking implements Listener {
    private static YamlConfiguration yaml;
    private static boolean lockMode;
    private static boolean lockHealth;
    private static boolean lockFoodLevel;
    private static boolean lockFlying;
    private static boolean lockEffect;

    public ModeLocking() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "modelock.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("modelock.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        lockMode = yaml.getBoolean("lockMode");
        lockHealth = yaml.getBoolean("lockHealth");
        lockFoodLevel = yaml.getBoolean("lockFoodLevel");
        lockFlying = yaml.getBoolean("lockFlying");
        lockEffect = yaml.getBoolean("lockEffect");

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("utes.modelock")) {
            return;
        }
        GameMode mode = player.getGameMode();
        double health = player.getHealth();
        int foodLevel = player.getFoodLevel();
        double maxHealth = player.getMaxHealth();
        boolean isFlying = player.isFlying();
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lockMode)
                    player.setGameMode(mode);
                if (lockHealth) {
                    player.setMaxHealth(maxHealth);
                    player.setHealth(health);
                }
                if (lockFoodLevel)
                    player.setFoodLevel(foodLevel);
                if (lockFlying)
                    player.setFlying(isFlying);
                if (lockEffect)
                    player.addPotionEffects(effects);
            }
        }.runTaskLater(UntilTheEndServer.getInstance(), 2L);

    }
}
