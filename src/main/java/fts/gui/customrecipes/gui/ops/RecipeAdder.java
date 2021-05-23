package fts.gui.customrecipes.gui.ops;

import fts.gui.customrecipes.stat.FileManager;
import fts.gui.customrecipes.stat.Recipe;
import fts.spi.FTSInvHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RecipeAdder implements Listener {
    public static Inventory inv;
    public static HashSet<Integer> slots = new HashSet<Integer>() {{
        add(10);
        add(11);
        add(12);
        add(13);
        add(19);
        add(20);
        add(21);
        add(22);
        add(28);
        add(29);
        add(30);
        add(31);
        add(37);
        add(38);
        add(39);
        add(40);
    }};
    public static HashSet<UUID> openers = new HashSet<UUID>();

    public static Inventory getInv() {
        inv = Bukkit.createInventory(new RecipeAdderHolder(), 54, "添加新配方");
        Material type;
        try {
            type = Material.valueOf("STAINED_GLASS_PANE");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        inv.setItem(10, new ItemStack(Material.AIR));
        inv.setItem(11, new ItemStack(Material.AIR));
        inv.setItem(12, new ItemStack(Material.AIR));
        inv.setItem(13, new ItemStack(Material.AIR));
        inv.setItem(19, new ItemStack(Material.AIR));
        inv.setItem(20, new ItemStack(Material.AIR));
        inv.setItem(21, new ItemStack(Material.AIR));
        inv.setItem(22, new ItemStack(Material.AIR));
        inv.setItem(28, new ItemStack(Material.AIR));
        inv.setItem(29, new ItemStack(Material.AIR));
        inv.setItem(30, new ItemStack(Material.AIR));
        inv.setItem(31, new ItemStack(Material.AIR));
        inv.setItem(37, new ItemStack(Material.AIR));
        inv.setItem(38, new ItemStack(Material.AIR));
        inv.setItem(39, new ItemStack(Material.AIR));
        inv.setItem(40, new ItemStack(Material.AIR));


        inv.setItem(16, getItemStack(Material.SLIME_BALL, 0, "§6添加配方", new ArrayList<>()));
        inv.setItem(25, getItemStack(Material.DIRT, 0, "§6配方属性",
                new ArrayList<String>() {
                    {
                        add("§r配方属性将会在添加后自动生成默认属性");
                        add("§r若要配置属性，请先添加配方，再使用指令配置");
                    }
                }));
        inv.setItem(43, new ItemStack(Material.AIR));

        return inv;
    }

    public static void addRecipe(Inventory inv) {
        HashMap<Integer, ItemStack> materials = new HashMap<>();
        for (int slot : slots) {
            if (inv.getItem(slot) == null) {
                continue;
            }
            if (inv.getItem(slot).getType() == Material.AIR) {
                continue;
            }
            materials.put(slot, inv.getItem(slot));
        }
        Recipe recipe = new Recipe(
                inv.getItem(43),
                materials,
                0.0,
                0.0,
                100.0,
                new ItemStack(Material.AIR),
                -1);
        FileManager.addRecipe(recipe);
    }

    private static ItemStack getItemStack(Material type, int data, String name, List<String> lore) {
        ItemStack item = new ItemStack(type);
        item.setDurability((short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeAdderHolder) {
            openers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeAdderHolder) {
            openers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (openers.contains(player.getUniqueId()) && inv != null) {
            if ((inv.getHolder() instanceof RecipeAdderHolder) &&
                    !slots.contains(event.getSlot()) && event.getSlot() != 43) {
                event.setCancelled(true);
            }
        }
        if (inv != null) {
            if (inv.getHolder() instanceof RecipeAdderHolder) {
                int slot = event.getSlot();
                if (slot == 16) {
                    if (inv.getItem(43) == null) {
                        player.sendMessage("§6[合成系统]§r 添加失败，请设置成品！");
                        return;
                    }
                    addRecipe(inv);
                    player.closeInventory();
                    player.sendMessage("§6[合成系统]§r 添加成功。");
                }
            }
        }
    }

    public static class RecipeAdderHolder implements FTSInvHolder {
        public static final RecipeAdderHolder INSTANCE = new RecipeAdderHolder();
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
