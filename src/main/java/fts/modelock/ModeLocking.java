package fts.modelock;

import fts.FunctionalToolSet;
import fts.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Collection;

/* TODO
 * fts.modelock
 */
public class ModeLocking implements Listener {
    private static boolean lockMode;
    private static boolean lockHealth;
    private static boolean lockFoodLevel;
    private static boolean lockFlying;
    private static boolean lockEffect;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("modelock.yml");
        File file = new File(plugin.getDataFolder(), "modelock.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        lockMode = yaml.getBoolean("lockMode");
        lockHealth = yaml.getBoolean("lockHealth");
        lockFoodLevel = yaml.getBoolean("lockFoodLevel");
        lockFlying = yaml.getBoolean("lockFlying");
        lockEffect = yaml.getBoolean("lockEffect");

        Bukkit.getPluginManager().registerEvents(new ModeLocking(), plugin);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("fts.modelock")) {
            return;
        }
        GameMode mode = player.getGameMode();
        double health = player.getHealth();
        int foodLevel = player.getFoodLevel();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        boolean isFlying = player.isFlying();
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lockMode) {
                    player.setGameMode(mode);
                }
                if (lockHealth) {
                    //TODO
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                    player.setHealth(health);
                }
                if (lockFoodLevel) {
                    player.setFoodLevel(foodLevel);
                }
                if (lockFlying) {
                    player.setFlying(isFlying);
                }
                if (lockEffect) {
                    player.addPotionEffects(effects);
                }
            }
        }.runTaskLater(FunctionalToolSet.getInstance(), 2L);

    }
}
