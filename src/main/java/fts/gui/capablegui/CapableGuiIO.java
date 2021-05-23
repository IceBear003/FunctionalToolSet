package fts.gui.capablegui;

import fts.FunctionalToolSet;
import fts.spi.ItemFactory;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapableGuiIO {
    public static void load(Player player) {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/capableguis/",
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
                lore.add(ResourceUtils.getLang("lore-delgui"));
                ItemStack item = createItem(type, 0, yaml.getString(path + ".name"), lore);
                inv.addItem(item);
            }
            invs.add(inv);
        }
        if (invs.size() == 0) {
            invs.add(initInventory());
        }
        CapableGui.capableGuis.put(player.getUniqueId(), invs);
    }

    public static void save(Player player) {
        if (!CapableGui.capableGuis.containsKey(player.getUniqueId())) {
            return;
        }
        ArrayList<Inventory> invs = CapableGui.capableGuis.get(player.getUniqueId());
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/capableguis/",
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
            FunctionalToolSet.getInstance().getLogger().info(
                    ResourceUtils.getSpecialLang("error-while-save-gui",
                            new ArrayList<String>() {{
                                add("{player}");
                                add(player.getName());
                            }}));
        }
    }

    public static Inventory initInventory() {
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

    public static ItemStack createItem(Material type, int id, String name, List<String> lore) {
        ItemStack item = new ItemStack(type, 1, (short) id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setDurability((short) id);
        return item;
    }

    public static boolean hasNull(Inventory inv) {
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
}
