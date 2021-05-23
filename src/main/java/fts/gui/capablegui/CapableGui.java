package fts.gui.capablegui;

import fts.FunctionalToolSet;
import fts.spi.BlockApi;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.EnchantingTable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CapableGui {
    public static final HashMap<UUID, ArrayList<Inventory>> capableGuis = new HashMap<>();

    public static void initialize(FunctionalToolSet plugin) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            CapableGuiIO.load(player);
        }

        Bukkit.getPluginManager().registerEvents(new CapableGuiListener(), plugin);
    }

    public static void openGui(Player player) {
        if (player.hasPermission("fts.gui.capablegui.opengui")) {
            player.openInventory(capableGuis.get(player.getUniqueId()).get(0));
        } else {
            ResourceUtils.sendMessage(player, "no-permission-operate-gui");
        }
    }

    public static void openWorkbench(Player player) {
        if (player.hasPermission("fts.gui.capablegui.workbench")) {
            player.openWorkbench(null, true);
        } else {
            ResourceUtils.sendMessage(player, "no-permission-use-capable-workbench");
        }
    }

    public static void openEnderChest(Player player) {
        if (player.hasPermission("fts.gui.capablegui.enderchest")) {
            player.openInventory(player.getEnderChest());
        } else {
            ResourceUtils.sendMessage(player, "no-permission-use-capable-enderchest");
        }
    }

    public static void openSpecialContainer(Player player, Location loc) {
        if (!player.hasPermission("fts.gui.capablegui.container")) {
            ResourceUtils.sendMessage(player, "no-permission-capable-container");
            return;
        }
        Block block = loc.getBlock();
        if (!(block.getState() instanceof Container)) {
            ResourceUtils.sendMessage(player, "not-a-container");
            return;
        }
        Container container = (Container) block.getState();
        player.openInventory(container.getInventory());
        CapableGuiListener.operating.put(player.getUniqueId(), loc);
    }

    public static void openEnchant(Player player, Location loc) {
        if (!player.hasPermission("fts.gui.capablegui.enchant")) {
            ResourceUtils.sendMessage(player, "no-permission-capable-enchanttable");
            return;
        }
        Block block = loc.getBlock();
        if (!(block.getState() instanceof EnchantingTable)) {
            return;
        }
        player.openEnchanting(loc, true);
    }

    public static void openMerchant(Player player, Villager villager) {
        if (!player.hasPermission("fts.gui.capablegui.merchant")) {
            ResourceUtils.sendMessage(player, "no-permission-capable-merchant");
            return;
        }
        if (villager == null) {
            ResourceUtils.sendMessage(player, "villager-not-exist");
            return;
        }
        if (villager.isDead()) {
            ResourceUtils.sendMessage(player, "villager-already-die");
            return;
        }
        player.openMerchant(villager, true);
    }

    public static void addGui(Player player, Location loc, String name) {
        if (!player.hasPermission("fts.gui.capablegui.addgui")) {
            ResourceUtils.sendMessage(player, "no-permission-addgui");
            return;
        }

        if (!(loc.getBlock().getState() instanceof Container) && !(loc.getBlock().getType().toString().contains("ENCHANT") &&
                loc.getBlock().getType().toString().contains("TABLE"))) {
            ResourceUtils.sendMessage(player, "not-a-block");
            return;
        }

        BlockBreakEvent event = new BlockBreakEvent(loc.getBlock(), player);
        event.setDropItems(false);
        event.setExpToDrop(0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ResourceUtils.sendMessage(player, "no-permission-use-container");
            return;
        }

        ArrayList<Inventory> guis = capableGuis.get(player.getUniqueId());
        Inventory inv = guis.get(guis.size() - 1);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(BlockApi.locToStr(loc));
        lore.add("");
        lore.add(ResourceUtils.getLang("lore-delgui"));
        ItemStack item = CapableGuiIO.createItem(loc.getBlock().getType(), 0, name, lore);

        if (CapableGuiIO.hasNull(inv)) {
            for (int i = 0; i < inv.getSize(); i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, item);
                    break;
                }
                if (inv.getItem(i).getType() == Material.AIR) {
                    inv.setItem(i, item);
                    break;
                }
            }
        } else {
            guis.add(CapableGuiIO.initInventory());
            addGui(player, loc, name);
        }

        ResourceUtils.sendSpecialMessage(player, "successfully-add-gui",
                new ArrayList<String>() {{
                    add("{name}");
                    add(name);
                }});
    }

    public static void addGui(Player player, Villager villager, String name) {
        if (!player.hasPermission("fts.gui.capablegui.addgui")) {
            ResourceUtils.sendMessage(player, "no-permission-addmerchant");
            return;
        }

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, villager, EntityDamageEvent.DamageCause.CUSTOM, 0.1);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ResourceUtils.sendMessage(player, "villager-not-own");
            return;
        }

        if (villager == null) {
            return;
        }

        ArrayList<Inventory> guis = capableGuis.get(player.getUniqueId());
        Inventory inv = guis.get(guis.size() - 1);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("uuid:" + villager.getUniqueId());
        lore.add("");
        lore.add(ResourceUtils.getLang("lore-delgui"));
        ItemStack item = CapableGuiIO.createItem(Material.EMERALD, 0, name, lore);

        if (CapableGuiIO.hasNull(inv)) {
            for (int i = 0; i < inv.getSize(); i++) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, item);
                    break;
                }
                if (inv.getItem(i).getType() == Material.AIR) {
                    inv.setItem(i, item);
                    break;
                }
            }
        } else {
            guis.add(CapableGuiIO.initInventory());
            addGui(player, villager, name);
        }

        ResourceUtils.sendSpecialMessage(player, "successfully-add-merchant",
                new ArrayList<String>() {{
                    add("{name}");
                    add(name);
                }});
    }
}
