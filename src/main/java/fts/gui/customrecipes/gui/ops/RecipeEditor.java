package fts.gui.customrecipes.gui.ops;

import fts.FunctionalToolSet;
import fts.gui.customrecipes.gui.shared.RecipeSawer;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class RecipeEditor implements Listener {
    public static Inventory inv;
    public static HashSet<UUID> openers = new HashSet<UUID>();

    public static void initialize() {
        inv = Bukkit.createInventory(new RecipeEditorHolder(), 9, "合成系统管理台");
        Material type;
        try {
            type = Material.valueOf("STAINED_GLASS_PANE");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        inv.setItem(1, getItemStack(Material.PAPER, 0, "§6添加新配方", new ArrayList<>()));
        inv.setItem(3, getItemStack(Material.BOOK, 0, "§6查看已有配方", new ArrayList<>()));
        try {
            type = Material.valueOf("SIGN");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_SIGN");
        }
        inv.setItem(5, getItemStack(type, 0, "§6配置已有配方", new ArrayList<String>()));
        try {
            type = Material.valueOf("SKULL_ITEM");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_SKULL_ITEM");
        }
        inv.setItem(7, getItemStack(type, 0, "§6关于本插件",
                new ArrayList<String>() {
                    {
                        add("§e作者： §r§l南外丶仓鼠");
                        add("§e作者QQ： §r§l1294243258");
                        add("§e作者爱发电： §r§lhttp://afdian.net/@HamsterYDS");
                    }
                }));
        Bukkit.getPluginManager().registerEvents(new RecipeEditor(), FunctionalToolSet.getInstance());
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
        if (event.getInventory().getHolder() instanceof RecipeEditorHolder) {
            openers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeEditorHolder) {
            openers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (openers.contains(player.getUniqueId()) && inv != null) {
            event.setCancelled(true);
        }
        if (inv != null) {
            if (inv.getHolder() instanceof RecipeEditorHolder) {
                int slot = event.getSlot();
                switch (slot) {
                    case 1:
                        player.openInventory(RecipeAdder.getInv());
                        break;
                    case 3:
                        player.openInventory(RecipeSawer.chooseInv);
                        break;
                    case 5:
                        player.sendMessage("§6[合成系统]§r 配置已有配方参数的指令：");
                        player.sendMessage("§6★ §r /cr delete <编号> - 删除某个配方");
                        player.sendMessage("§6★ §r /cr money <编号> <需要的金钱> - 设置配方合成需要的金钱");
                        player.sendMessage("§6★ §r /cr exp <编号> <需要的经验等级> - 设置配方合成需要的经验等级");
                        player.sendMessage("§6★ §r /cr percent <编号> <成功率> - 设置配方合成的成功率（0-100）");
                        player.sendMessage("§6★ §r /cr special <编号> <触发概率> - 设置指定编号的配方在合成时，触发特殊成品的几率（0-100）");
                        player.closeInventory();
                        break;
                    case 7:
                        player.sendMessage("§6[合成系统]§r 支持作者： http://afdian.net/@HamsterYDS");
                        player.closeInventory();
                        break;
                }
            }
        }
    }

    public static class RecipeEditorHolder implements FTSInvHolder {
        public static final RecipeEditorHolder INSTANCE = new RecipeEditorHolder();
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
