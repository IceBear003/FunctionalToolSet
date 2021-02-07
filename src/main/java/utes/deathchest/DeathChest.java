package utes.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import utes.UntilTheEndServer;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

/*TODO
 * utes.deathchest
 */

public class DeathChest implements Listener {
    private static YamlConfiguration yaml;
    private static boolean storeExp;
    private static boolean showMeesage;
    private static boolean onlyOwnerCanOpen;

    public DeathChest() {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "deathchest.yml");
        if (!file.exists())
            UntilTheEndServer.getInstance().saveResource("deathchest.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }
        storeExp = yaml.getBoolean("storeExp");
        showMeesage = yaml.getBoolean("showMessage");
        onlyOwnerCanOpen = yaml.getBoolean("onlyOwnerCanOpen");

        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }

    private static final HashMap<Location, UUID> messageArmor = new HashMap<Location, UUID>();
    private static final HashMap<Location, UUID> owner = new HashMap<Location, UUID>();
    private static final HashMap<Location, Integer> storeLevel = new HashMap<Location, Integer>();
    private static final HashMap<Location, Float> storeExperience = new HashMap<Location, Float>();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasPermission("utes.deathchest")) {
            return;
        }

        Location dieLoc = player.getLocation().getBlock().getLocation().clone().add(0, 1, 0);
        dieLoc.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) dieLoc.getBlock().getState();
        for (ItemStack item : player.getInventory().getContents()) {
            if(item==null)
                continue;
            if (chest.getInventory().contains(Material.AIR)) {
                chest.getInventory().addItem(item.clone());
            } else {
                dieLoc.getWorld().dropItemNaturally(dieLoc, item);
            }
        }

        event.getDrops().clear();
        player.getInventory().clear();
        owner.put(dieLoc, player.getUniqueId());

        if (showMeesage) {
            ArmorStand armor = (ArmorStand) dieLoc.getWorld().spawnEntity(dieLoc.clone().add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
            armor.setVisible(false);
            armor.setCustomNameVisible(true);
            armor.setCustomName("§e玩家§r" + player.getName() + "§e死前掉落的遗物，打开箱子以获取");
            messageArmor.put(dieLoc, armor.getUniqueId());
        }

        if (storeExp) {
            storeLevel.put(dieLoc, player.getLevel());
            storeExperience.put(dieLoc, player.getExp());
            player.setLevel(0);
            player.setExp(0);
        }
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        Location loc = event.getClickedBlock().getLocation();
        if (owner.containsKey(loc)) {
            if (onlyOwnerCanOpen && owner.get(loc) != player.getUniqueId()) {
                event.setCancelled(true);
                player.sendMessage("这个箱子只有死者本人才能打开！");
                return;
            }
            if (storeExp) {
                player.setLevel(storeLevel.get(loc));
                player.setExp(storeExperience.get(loc));
                storeLevel.remove(loc);
                storeExperience.remove(loc);
            }
            if (showMeesage) {
                Entity entity = Bukkit.getEntity(messageArmor.get(loc));
                entity.remove();
            }
            owner.remove(loc);
        }
    }
}
