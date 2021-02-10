package utes.capablegui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.EnchantingTable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import utes.ItemFactory;
import utes.UntilTheEndServer;
import utes.api.BlockApi;
import utes.api.UTEInvHolder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/*
 * utes.capablegui.opengui
 * utes.capablegui.addgui
 * utes.capablegui.workbench
 * utes.capablegui.enderchest
 * utes.capablegui.container
 * utes.capablegui.enchant
 * utes.capablegui.merchant
 */
public class CapableGui implements Listener {
    private static final HashMap<UUID, ArrayList<Inventory>> choseGuis = new HashMap<>();
    private static final HashSet<UUID> openers = new HashSet<>();
    private static final HashMap<UUID, Location> operating = new HashMap<>();

    public static void initialize(UntilTheEndServer plugin) {
        Bukkit.getPluginManager().registerEvents(new CapableGui(), plugin);
    }

    public static void openGui(Player player) {
        if (player.hasPermission("utes.capablegui.opengui")) {
            player.openInventory(choseGuis.get(player.getUniqueId()).get(0));
        } else {
            player.sendMessage("你没有使用远程操控容器的权限！");
        }
    }

    private static void openWorkbench(Player player) {
        if (player.hasPermission("utes.capablegui.workbench")) {
            player.openWorkbench(null, true);
        } else {
            player.sendMessage("你没有使用便携工作台的权限！");
        }
    }

    private static void openEnderChest(Player player) {
        if (player.hasPermission("utes.capablegui.enderchest")) {
            player.openInventory(player.getEnderChest());
        } else {
            player.sendMessage("你没有使用便携末影箱的权限！");
        }
    }

    private static void openSpecialContainer(Player player, Location loc) {
        if (!player.hasPermission("utes.capablegui.container")) {
            player.sendMessage("你没有使用远程容器的权限！");
            return;
        }
        Block block = loc.getBlock();
        if (!(block.getState() instanceof Container)) {
            player.sendMessage("该方块不是容器！");
            return;
        }
        Container container = (Container) block.getState();
        player.openInventory(container.getInventory());
        operating.put(player.getUniqueId(), loc);
    }

    private static void openEnchant(Player player, Location loc) {
        if (!player.hasPermission("utes.capablegui.enchant")) {
            player.sendMessage("你没有使用远程附魔台的权限！");
            return;
        }
        Block block = loc.getBlock();
        if (!(block.getState() instanceof EnchantingTable)) {
            return;
        }
        player.openEnchanting(loc, true);
    }

    private static void openMerchant(Player player, Villager villager) {
        if (!player.hasPermission("utes.capablegui.merchant")) {
            player.sendMessage("你没有使用远程商店的权限！");
            return;
        }
        if (villager == null) {
            player.sendMessage("村民不存在");
            return;
        }
        if (villager.isDead()) {
            player.sendMessage("村民已死亡");
            return;
        }
        player.openMerchant(villager, true);
    }

    public static void addItemStack(Player player, Location loc, String name) {
        if (!player.hasPermission("utes.capablegui.addgui")) {
            player.sendMessage("你没有权限增加远程容器！");
            return;
        }

        if (!(loc.getBlock().getState() instanceof Container) && !(loc.getBlock().getType().toString().contains("ENCHANT") &&
                loc.getBlock().getType().toString().contains("TABLE"))) {
            player.sendMessage("您指向的方块不是容器！");
            return;
        }

        BlockBreakEvent event = new BlockBreakEvent(loc.getBlock(), player);
        event.setDropItems(false);
        event.setExpToDrop(0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage("这个容器不属于你！");
            return;
        }

        ArrayList<Inventory> guis = choseGuis.get(player.getUniqueId());
        Inventory inv = guis.get(guis.size() - 1);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(BlockApi.locToStr(loc));
        lore.add("");
        lore.add("§6shift+左击 删除本远程容器/商店");
        ItemStack item = createItem(loc.getBlock().getType(), 0, name, lore);

        if (hasNull(inv)) {
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
            guis.add(initInventory());
            addItemStack(player, loc, name);
        }

        player.sendMessage("添加远程容器" + name + "成功！");
    }

    public static void addItemStack(Player player, Villager villager, String name) {
        if (!player.hasPermission("utes.capablegui.addgui")) {
            player.sendMessage("你没有权限增加远程商店！");
            return;
        }

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, villager, EntityDamageEvent.DamageCause.CUSTOM, 0.1);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage("这个村民不属于你！");
            return;
        }

        if (villager == null) {
            return;
        }

        ArrayList<Inventory> guis = choseGuis.get(player.getUniqueId());
        Inventory inv = guis.get(guis.size() - 1);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("uuid:" + villager.getUniqueId());
        lore.add("");
        lore.add("§6shift+左击 删除本远程容器/商店");
        ItemStack item = createItem(Material.EMERALD, 0, name, lore);

        if (hasNull(inv)) {
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
            guis.add(initInventory());
            addItemStack(player, villager, name);
        }

        player.sendMessage("添加远程商店" + name + "成功！");
    }

    private static boolean hasNull(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                return true;
            }
            if (inv.getItem(i).getType() == Material.AIR) {
                return true;
            }
        }
        return false;
    }

    private static void load(Player player) {
        File file = new File(UntilTheEndServer.getInstance().getDataFolder() + "/capableguis/",
                player.getUniqueId().toString() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ArrayList<Inventory> invs = new ArrayList<>();
        if (file.exists()) {
            Inventory inv = initInventory();
            for (String path : yaml.getKeys(false)) {
                if (!hasNull(inv)) {
                    invs.add(inv);
                    inv = initInventory();
                }
                String typeStr = "";
                for (int i = 0; i < path.length(); i++) {
                    if (path.charAt(i) <= '9' && path.charAt(i) >= '0') {
                        typeStr = path.substring(0, i);
                    }
                }
                Material type = Material.valueOf(typeStr);
                ArrayList<String> lore = new ArrayList<>();
                lore.add(yaml.getString(path + ".loc"));
                lore.add("");
                lore.add("§6shift+左击 删除本远程容器/商店");
                ItemStack item = createItem(type, 0, yaml.getString(path + ".name"), lore);
                inv.addItem(item);
            }
            invs.add(inv);
        }
        if (invs.size() == 0) {
            invs.add(initInventory());
        }
        choseGuis.put(player.getUniqueId(), invs);
    }

    private static void save(Player player) {
        if (!choseGuis.containsKey(player.getUniqueId())) {
            return;
        }
        ArrayList<Inventory> invs = choseGuis.get(player.getUniqueId());
        File file = new File(UntilTheEndServer.getInstance().getDataFolder() + "/capableguis/",
                player.getUniqueId().toString() + ".yml");
        file.delete();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int tot = 0;
        for (Inventory inv : invs) {
            for (int i = 0; i <= 53; i++) {
                if (i % 9 == 0 || i % 9 == 1 | i % 9 == 2) {
                    continue;
                }
                ItemStack item = inv.getItem(i);
                if (item == null) {
                    continue;
                }
                if (item.getType() == Material.AIR) {
                    continue;
                }
                String toString = item.getItemMeta().getLore().get(0);
                yaml.set(item.getType().toString() + tot + ".name", item.getItemMeta().getDisplayName());
                yaml.set(item.getType().toString() + tot + ".loc", toString);
                tot++;
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Inventory initInventory() {
        ItemStack frame = createItem(ItemFactory.valueOf("STAINED_GLASS_PANE"), 15, "§8边框", new ArrayList<>());
        Inventory inv = Bukkit.createInventory(new HolderChoseGui(), 54, "§l远程操控");
        for (int i = 0; i <= 53; i++) {
            if (i % 9 == 0 || i % 9 == 1 | i % 9 == 2) {
                inv.setItem(i, frame);
            }
        }
        inv.setItem(10, createItem(ItemFactory.valueOf("WORKBENCH"), 0, "§6便携式工作台", new ArrayList<>()));
        inv.setItem(37, createItem(Material.ENDER_CHEST, 0, "§6便携式潜影箱", new ArrayList<>()));
        inv.setItem(45, createItem(Material.PAPER, 0, "§6上一页", new ArrayList<>()));
        inv.setItem(47, createItem(Material.PAPER, 0, "§e下一页", new ArrayList<>()));
        return inv;
    }

    private static ItemStack createItem(Material type, int id, String name, List<String> lore) {
        ItemStack item = new ItemStack(type, 1, (short) id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setDurability((short) id);
        return item;
    }

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
                openWorkbench(player);
            } else if (slot == 37) {
                openEnderChest(player);
            } else if (slot == 47) {
                ArrayList<Inventory> invs = choseGuis.get(player.getUniqueId());
                if (invs.indexOf(inv) == invs.size() - 1) {
                    return;
                }
                player.openInventory(invs.get(invs.indexOf(inv) + 1));
            } else if (slot == 45) {
                ArrayList<Inventory> invs = choseGuis.get(player.getUniqueId());
                if (invs.indexOf(inv) == 0) {
                    return;
                }
                player.openInventory(invs.get(invs.indexOf(inv) - 1));
            } else if (slot % 9 > 2) {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    inv.setItem(slot, new ItemStack(Material.AIR));
                    player.sendMessage("删除远程容器/商店成功");
                    return;
                }
                String lore = item.getItemMeta().getLore().get(0);
                if (item.getType().toString().contains("ENCHANT") && item.getType().toString().contains("TABLE")) {
                    openEnchant(player, BlockApi.strToLoc(lore));
                } else if (lore.startsWith("uuid:")) {
                    Villager villager = (Villager) Bukkit.getEntity(UUID.fromString(lore.replace("uuid:", "")));
                    openMerchant(player, villager);
                } else {
                    openSpecialContainer(player, BlockApi.strToLoc(item.getItemMeta().getLore().get(0)));
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
        save(player);
        choseGuis.remove(player.getUniqueId());
        if (operating.containsKey(player.getUniqueId())) {
            Block block = operating.get(player.getUniqueId()).getBlock();
            block.getState().update();
            operating.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        load(player);
    }

    private static class HolderChoseGui implements UTEInvHolder {
        public static final HolderChoseGui INSTANCE = new HolderChoseGui();
        private String name;

        @Override
        public String getCustomName() {
            return name;
        }

        @Override
        public void setCustomName(String name) {
            this.name = name;
        }
    }
}
