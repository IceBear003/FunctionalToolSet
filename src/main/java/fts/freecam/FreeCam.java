package fts.freecam;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FreeCam implements Listener {
    private static int maxDist;
    private static int time;
    private static int cd;

    private static HashMap<UUID, UUID> mobs = new HashMap<UUID, UUID>();
    private static HashMap<UUID, Long> lastUseStamp = new HashMap<UUID, Long>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("freecam.yml");
        File file = new File(plugin.getDataFolder(), "freecam.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        maxDist = yaml.getInt("maxDist");
        time = yaml.getInt("time");
        cd = yaml.getInt("cooldown");

        Bukkit.getPluginManager().registerEvents(new FreeCam(), plugin);
    }

    public static void goFreeCam(Player player) {
        if (!player.hasPermission("fts.freecam.use")) {
            ResourceUtils.sendMessage(player, "no-permission-use-freecam");
            return;
        }
        if (!player.hasPermission("fts.freecam.ignorecd")) {
            if (lastUseStamp.containsKey(player.getUniqueId())) {
                if (System.currentTimeMillis() - lastUseStamp.get(player.getUniqueId()) < cd * 1000) {
                    ResourceUtils.sendSpecialMessage(player, "freecam-cooldowning",
                            new ArrayList<String>() {
                                {
                                    add("{time}");
                                    add(String.valueOf(cd - (System.currentTimeMillis() - lastUseStamp.get(player.getUniqueId())) / 1000));
                                }
                            });
                    return;
                }
            }
        }
        lastUseStamp.put(player.getUniqueId(), System.currentTimeMillis());

        player.setGameMode(GameMode.SPECTATOR);
        Location origin = player.getLocation().clone();
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, time * 20, 0));

        Villager mob = (Villager) player.getWorld().spawnEntity(origin, EntityType.VILLAGER);
        mob.setCustomNameVisible(true);
        mob.setCustomName(
                ResourceUtils.getSpecialLang("freecam-custom-name", new ArrayList<String>() {{
                    add("{player}");
                    add(player.getName());
                }}));

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);

        double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getBaseValue();
        mob.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);

        mob.setHealth(player.getHealth());
        mob.setAI(false);
        mob.setCanPickupItems(false);

        mobs.put(mob.getUniqueId(), player.getUniqueId());

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    goBack();
                    return;
                }
                if (player.isDead()) {
                    return;
                }
                if (!mobs.containsKey(mob.getUniqueId())) {
                    return;
                }
                counter++;
                for (Entity entity : mob.getNearbyEntities(20, 20, 20)) {
                    if (entity instanceof Monster) {
                        ((Monster) entity).setTarget(mob);
                    }
                }

                if (counter < time) {
                    Location current = player.getLocation();
                    if (current.getWorld().getName().equalsIgnoreCase(origin.getWorld().getName())) {
                        if (current.distance(origin) <= maxDist) {
                            player.sendTitle(
                                    ResourceUtils.getLang("freecam-going-title"),
                                    ResourceUtils.getSpecialLang("freecam-going-subtitle",
                                            new ArrayList<String>() {
                                                {
                                                    add("{time}");
                                                    add(String.valueOf((time - counter)));
                                                }
                                            }),
                                    10, 70, 20);
                            return;
                        }
                    } else {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.sendTitle(ResourceUtils.getLang("freecam-stop-title"),
                                ResourceUtils.getLang("freecam-stop-change-world"),
                                10, 70, 20);
                        mob.remove();
                        mobs.remove(mob.getUniqueId());
                        cancel();
                        return;
                    }
                }

                player.sendTitle(ResourceUtils.getLang("freecam-stop-title"),
                        ResourceUtils.getLang("freecam-stop-out-of-rule"),
                        10, 70, 20);
                goBack();
                return;
            }

            public void goBack() {
                player.teleport(origin);
                player.setGameMode(GameMode.SURVIVAL);
                mob.remove();
                mobs.remove(mob.getUniqueId());
                cancel();
            }
        }.runTaskTimer(FunctionalToolSet.getInstance(), 0L, 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (mobs.containsKey(entity.getUniqueId())) {
            Player player = Bukkit.getPlayer(mobs.get(entity.getUniqueId()));

            if (player == null) {
                return;
            }

            player.damage(event.getFinalDamage());
            player.sendTitle(ResourceUtils.getLang("freecam-stop-title"),
                    ResourceUtils.getLang("freecam-stop-be-damaged"),
                    10, 70, 20);

            player.teleport(entity.getLocation());
            player.setGameMode(GameMode.SURVIVAL);
            entity.remove();
            mobs.remove(entity.getUniqueId());
        }
    }
}
