package fts.mechanism.player.xplimit;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class ExpLimit implements Listener {
    public static String limitOrigin;
    public static String[] limitLore;
    public static String msg;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("xplimit.yml");
        File file = new File(plugin.getDataFolder(), "xplimit.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);


        limitOrigin = yaml.getString("lore");
        limitLore = yaml.getString("lore").split("%exp%");
        msg = yaml.getString("msg");

        Bukkit.getPluginManager().registerEvents(new ExpLimit(), plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerInventory inv = player.getInventory();

                    int held = inv.getHeldItemSlot();
                    ItemStack item = inv.getItemInMainHand();

                    if (player.getLevel() < needLevel(item)) {
                        player.sendMessage(msg);
                        try {
                            inv.setHeldItemSlot(held + 1);
                        } catch (Exception exception) {
                            inv.setHeldItemSlot(held - 1);
                        }
                    }

                    item = inv.getItemInOffHand();
                    if (player.getLevel() < needLevel(item)) {
                        player.sendMessage(msg);
                        inv.setItemInOffHand(null);
                        player.getWorld().dropItem(player.getLocation(), item);
                    }

                    item = inv.getHelmet();
                    if (player.getLevel() < needLevel(item)) {
                        player.sendMessage(msg);
                        inv.setHelmet(new ItemStack(Material.AIR));
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                    item = inv.getChestplate();
                    if (player.getLevel() < needLevel(item)) {
                        player.sendMessage(msg);
                        inv.setChestplate(new ItemStack(Material.AIR));
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                    item = inv.getLeggings();
                    if (player.getLevel() < needLevel(item)) {
                        player.sendMessage(msg);
                        inv.setLeggings(new ItemStack(Material.AIR));
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                    item = inv.getBoots();
                    if (player.getLevel() < needLevel(item)) {
                        player.sendMessage(msg);
                        inv.setBoots(new ItemStack(Material.AIR));
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static int needLevel(ItemStack item) {
        if (item == null) {
            return -1;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasLore()) {
                for (String line : meta.getLore()) {
                    for (String tmp : limitLore) {
                        line = line.replace(tmp, "");
                    }
                    try {
                        Integer level = Integer.parseInt(line);
                        return level;
                    } catch (Exception exception) {
                        continue;
                    }
                }
            }
        }
        return -1;
    }

    @EventHandler
    public void onHeld(PlayerSwapHandItemsEvent event) {
        ItemStack item = event.getMainHandItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasLore()) {
                for (String line : meta.getLore()) {
                    for (String tmp : limitLore) {
                        line = line.replace(tmp, "");
                    }
                    try {
                        Integer level = Integer.parseInt(line);
                        if (event.getPlayer().getLevel() < level) {
                            event.getPlayer().sendMessage(msg);
                            event.setCancelled(true);
                        }
                    } catch (Exception exception) {

                    }
                }
            }
        }
        item = event.getOffHandItem();
        meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasLore()) {
                for (String line : meta.getLore()) {
                    for (String tmp : limitLore) {
                        line = line.replace(tmp, "");
                    }
                    try {
                        Integer level = Integer.parseInt(line);
                        if (event.getPlayer().getLevel() < level) {
                            event.getPlayer().sendMessage(msg);
                            event.setCancelled(true);
                        }
                    } catch (Exception exception) {

                    }
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.hasItem()) {
            ItemStack item = event.getItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        for (String tmp : limitLore) {
                            line = line.replace(tmp, "");
                        }
                        try {
                            Integer level = Integer.parseInt(line);
                            if (event.getPlayer().getLevel() < level) {
                                event.getPlayer().sendMessage(msg);
                                event.setCancelled(true);
                            }
                        } catch (Exception exception) {

                        }
                    }
                }
            }
        }
    }

}
