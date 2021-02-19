package fts.capablegui;

import fts.spi.BlockApi;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class CapableGuiListener implements Listener {
    public static final HashMap<UUID, Location> operating = new HashMap<>();
    private static final HashSet<UUID> openers = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Inventory inv = event.getClickedInventory();
        if (inv == null) {
            return;
        }
        if (inv.getHolder() instanceof HolderChoseGui) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            ItemStack item = inv.getItem(slot);
            if (item == null) {
                return;
            }
            if (item.getType() == Material.AIR) {
                return;
            }

            if (slot == 10) {
                CapableGui.openWorkbench(player);
            } else if (slot == 37) {
                CapableGui.openEnderChest(player);
            } else if (slot == 47) {
                ArrayList<Inventory> invs = CapableGui.capableGuis.get(player.getUniqueId());
                if (invs.indexOf(inv) == invs.size() - 1) {
                    return;
                }
                player.openInventory(invs.get(invs.indexOf(inv) + 1));
            } else if (slot == 45) {
                ArrayList<Inventory> invs = CapableGui.capableGuis.get(player.getUniqueId());
                if (invs.indexOf(inv) == 0) {
                    return;
                }
                player.openInventory(invs.get(invs.indexOf(inv) - 1));
            } else if (slot % 9 > 2) {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    inv.setItem(slot, new ItemStack(Material.AIR));
                    ResourceUtils.sendMessage(player, "successfully-del-gui");
                    return;
                }
                String lore = item.getItemMeta().getLore().get(0);
                if (item.getType().toString().contains("ENCHANT") && item.getType().toString().contains("TABLE")) {
                    CapableGui.openEnchant(player, BlockApi.strToLoc(lore));
                } else if (lore.startsWith("uuid:")) {
                    Villager villager = (Villager) Bukkit.getEntity(UUID.fromString(lore.replace("uuid:", "")));
                    CapableGui.openMerchant(player, villager);
                } else {
                    CapableGui.openSpecialContainer(player, BlockApi.strToLoc(item.getItemMeta().getLore().get(0)));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onOperate(InventoryClickEvent event) {
        if (openers.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof HolderChoseGui) {
            openers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onCloseGui(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof HolderChoseGui) {
            openers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (operating.containsKey(player.getUniqueId())) {
            Block block = operating.get(player.getUniqueId()).getBlock();
            block.getState().update();
            operating.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CapableGuiIO.save(player);
        CapableGui.capableGuis.remove(player.getUniqueId());
        if (operating.containsKey(player.getUniqueId())) {
            Block block = operating.get(player.getUniqueId()).getBlock();
            block.getState().update();
            operating.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CapableGuiIO.load(player);
    }
}
