package fts.gui.customrecipes.gui.shared;

import fts.gui.customrecipes.CustomRecipes;
import fts.gui.customrecipes.gui.player.RecipeWorkbench;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RecipeSawer implements Listener {
    public static Inventory chooseInv;
    public static ArrayList<Inventory> invs_1 = new ArrayList<>(); //常规
    public static ArrayList<Inventory> invs_2 = new ArrayList<>(); //非常规
    public static HashMap<ItemStack, Inventory> items = new HashMap<>();
    public static HashSet<UUID> openers_sawer = new HashSet<UUID>();
    public static HashSet<UUID> openers_choose = new HashSet<UUID>();
    public static HashSet<UUID> openers_craft = new HashSet<UUID>();
    public static HashMap<Inventory, String> names = new HashMap<>();

    public static void updateInvs() {
        chooseInv = Bukkit.createInventory(new RecipeChooseHolder(), 9, "选择配方种类");
        Material type;
        try {
            type = Material.valueOf("STAINED_GLASS_PANE");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
        }
        for (int i = 0; i < 9; i++) {
            chooseInv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        try {
            type = Material.valueOf("EMPTY_MAP");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_EMPTY_MAP");
        }
        chooseInv.setItem(2, getItemStack(type, 0, "§6合成系统", new ArrayList<String>() {{
            add("§8- 此系统中合成成功率100%");
        }}));
        chooseInv.setItem(6, getItemStack(Material.MAP, 0, "§6合成系统EX", new ArrayList<String>() {{
            add("§8- 此系统中合成成功率非100%，有几率返还物品");
        }}));

        invs_1 = new ArrayList<>();
        invs_2 = new ArrayList<>();
        items = new HashMap<>();

        Inventory inv = getSawerInv();
        invs_1.add(inv);
        int tot = 8;
        for (int id : FileManager.recipes.keySet()) {
            Recipe recipe = FileManager.recipes.get(id);
            if (recipe.percent != 100) {
                continue;
            }
            inv.setItem(++tot, recipe.result);
            items.put(recipe.result, getCraftInv(id, recipe));
            if (tot == 44) {
                invs_1.add(getSawerInv());
                inv = invs_1.get(invs_1.size() - 1);
                tot = 9;
            }
        }

        inv = getSawerInv();
        invs_2.add(inv);
        tot = 8;
        for (int id : FileManager.recipes.keySet()) {
            Recipe recipe = FileManager.recipes.get(id);
            if (recipe.percent == 100) {
                continue;
            }
            inv.setItem(++tot, recipe.result);
            items.put(recipe.result, getCraftInv(id, recipe));
            if (tot == 44) {
                invs_2.add(getSawerInv());
                inv = invs_2.get(invs_2.size() - 1);
                tot = 9;
            }
        }
    }

    public static Inventory getSawerInv() {
        Inventory inv = Bukkit.createInventory(new RecipeSawerHolder(), 54, "查询合成");
        Material type;
        try {
            type = Material.valueOf("STAINED_GLASS_PANE");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
        }
        for (int i = 0; i <= 8; i++) {
            inv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        for (int i = 45; i <= 53; i++) {
            inv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        inv.setItem(45, getItemStack(Material.PAPER, 0, "§a上一页", new ArrayList<String>()));
        inv.setItem(53, getItemStack(Material.PAPER, 0, "§a下一页", new ArrayList<String>()));
        return inv;
    }

    public static Inventory adapt(Player player, Inventory inv) {
        Inventory result = Bukkit.createInventory(inv.getHolder(), inv.getSize(), names.get(inv));
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null) {
                result.setItem(i, inv.getItem(i));
            }
        }

        int id = Integer.parseInt(names.get(inv).replace("查询合成:编号", ""));
        Recipe recipe = FileManager.recipes.get(id);

        if (recipe.percent == 100) {
            result.setItem(43, getItemStack(Material.BARRIER, 0, "§e常规合成", new ArrayList<>()));
        } else {
            int percent = (int) recipe.percent;
            if (!FileManager.stats.containsKey(player.getUniqueId())) {
                FileManager.load(player);
            }
            if (FileManager.stats.get(player.getUniqueId()).recipeCache.containsKey(id)) {
                int times = FileManager.stats.get(player.getUniqueId()).recipeCache.get(id);
                percent += times * CustomRecipes.successEx;
            }
            result.setItem(43, getItemStack(Material.BARRIER, 0, "§e成功率：§a" + percent + "/100", new ArrayList<>()));
        }

        result.setItem(53, getItemStack(Material.PAPER, 0, "§e返回合成面板", new ArrayList<>()));

        return result;
    }

    public static Inventory getCraftInv(int id, Recipe recipe) {
        Material type;
        try {
            type = Material.valueOf("STAINED_GLASS_PANE");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
        }

        Inventory inv = Bukkit.createInventory(new RecipeCraftHolder(), 54, "查询合成:编号" + id);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        inv.setItem(10, recipe.materials.get(10));
        inv.setItem(11, recipe.materials.get(11));
        inv.setItem(12, recipe.materials.get(12));
        inv.setItem(13, recipe.materials.get(13));
        inv.setItem(19, recipe.materials.get(19));
        inv.setItem(20, recipe.materials.get(20));
        inv.setItem(21, recipe.materials.get(21));
        inv.setItem(22, recipe.materials.get(22));
        inv.setItem(28, recipe.materials.get(28));
        inv.setItem(29, recipe.materials.get(29));
        inv.setItem(30, recipe.materials.get(30));
        inv.setItem(31, recipe.materials.get(31));
        inv.setItem(37, recipe.materials.get(37));
        inv.setItem(38, recipe.materials.get(38));
        inv.setItem(39, recipe.materials.get(39));
        inv.setItem(40, recipe.materials.get(40));

        inv.setItem(16, recipe.result);
        inv.setItem(25, getItemStack(type, 6, "§6点击自动摆放到合成面板中", new ArrayList<>()));
        inv.setItem(34, getItemStack(Material.POTION, 0, "§6其他属性消耗",
                new ArrayList<String>() {
                    {
                        add("§6消耗金币：§a" + recipe.money);
                        add("§6消耗经验等级：§a" + recipe.exp);
                    }
                }
        ));

        names.put(inv, "查询合成:编号" + id);
        return inv;
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
        if (event.getInventory().getHolder() instanceof RecipeSawerHolder) {
            openers_sawer.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeSawerHolder) {
            openers_sawer.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onOpen_choose(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeChooseHolder) {
            openers_choose.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose_choose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeChooseHolder) {
            openers_choose.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClick_choose(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (openers_choose.contains(player.getUniqueId()) && inv != null) {
            event.setCancelled(true);
        }
        if (inv != null) {
            if (inv.getHolder() instanceof RecipeChooseHolder) {
                int slot = event.getSlot();
                if (slot == 2) {
                    player.openInventory(invs_1.get(0));
                }
                if (slot == 6) {
                    player.openInventory(invs_2.get(0));
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (openers_sawer.contains(player.getUniqueId()) && inv != null) {
            event.setCancelled(true);
        }
        if (inv != null) {
            if (inv.getHolder() instanceof RecipeSawerHolder) {
                int slot = event.getSlot();
                switch (slot) {
                    case 45:
                        if (invs_1.contains(inv)) {
                            if (invs_1.indexOf(inv) - 1 >= 0) {
                                player.openInventory(invs_1.get(invs_1.indexOf(inv) - 1));
                            }
                        }
                        if (invs_2.contains(inv)) {
                            if (invs_2.indexOf(inv) - 1 >= 0) {
                                player.openInventory(invs_2.get(invs_2.indexOf(inv) - 1));
                            }
                        }
                        return;
                    case 53:
                        if (invs_1.contains(inv)) {
                            if (invs_1.indexOf(inv) + 1 < invs_1.size()) {
                                player.openInventory(invs_1.get(invs_1.indexOf(inv) + 1));
                            }
                        }
                        if (invs_2.contains(inv)) {
                            if (invs_2.indexOf(inv) + 1 < invs_2.size()) {
                                player.openInventory(invs_2.get(invs_2.indexOf(inv) + 1));
                            }
                        }
                        return;
                }
                ItemStack item = inv.getItem(slot);
                if (item == null) {
                    return;
                }
                if (item.getType() == Material.AIR) {
                    return;
                }
                if (items.containsKey(item)) {
                    player.openInventory(adapt(player, items.get(item)));
                    names.remove(inv);
                }
            }
        }
    }

    @EventHandler
    public void onOpen_craft(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeCraftHolder) {
            openers_craft.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose_craft(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RecipeCraftHolder) {
            openers_craft.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClick_craft(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (openers_craft.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
        if (inv != null) {
            if (inv.getHolder() instanceof RecipeCraftHolder) {
                int slot = event.getSlot();
                switch (slot) {
                    case 25:
                        for (int guiSlot : RecipeWorkbench.slots) {
                            if (RecipeWorkbench.invs.get(player.getUniqueId()).getItem(guiSlot) != null) {

                                player.sendMessage("§6[合成系统]§r 请先清空合成面板上放着的物品！");
                                return;
                            }
                        }
                        PlayerInventory playerInv = player.getInventory();
                        Recipe recipe = FileManager.recipes.get(Integer.parseInt(names.get(inv).replace("查询合成:编号", "")));
                        names.remove(inv);
                        for (int slot$ : recipe.materials.keySet()) {
                            ItemStack need = recipe.materials.get(slot$).clone();
                            int amount = need.getAmount();
                            int tot = 0;
                            need.setAmount(1);
                            for (ItemStack item : playerInv.getContents()) {
                                if (item == null) {
                                    continue;
                                }
                                ItemStack tmp = item.clone();
                                tmp.setAmount(1);
                                if (tmp.equals(need)) {
                                    tot += item.getAmount();
                                }
                            }
                            if (tot >= amount) {
                                for (int i = 0; i < playerInv.getSize(); i++) {
                                    ItemStack item = playerInv.getItem(i);
                                    if (item == null) {
                                        continue;
                                    }
                                    ItemStack tmp = item.clone();
                                    tmp.setAmount(1);
                                    if (tmp.equals(need)) {
                                        int amount$ = item.getAmount();
                                        if (amount$ <= amount) {
                                            playerInv.setItem(i, new ItemStack(Material.AIR));
                                            amount -= amount$;
                                        } else {
                                            item.setAmount(amount$ - amount);
                                            playerInv.setItem(i, item);
                                            amount = 0;
                                        }
                                        if (amount <= 0) {
                                            break;
                                        }
                                    }
                                }
                                RecipeWorkbench.invs.get(player.getUniqueId()).setItem(slot$,
                                        recipe.materials.get(slot$).clone());
                            }
                        }
                        player.openInventory(RecipeWorkbench.invs.get(player.getUniqueId()));
                        player.sendMessage("§6[合成系统]§r 自动摆放完成。");
                        return;
                    case 53:
                        player.openInventory(RecipeWorkbench.invs.get(player.getUniqueId()));
                        return;
                }
                ItemStack item = inv.getItem(slot);
                if (item == null) {
                    return;
                }
                if (item.getType() == Material.AIR) {
                    return;
                }
                if (items.containsKey(item)) {
                    if (!items.get(item).getItem(16).equals(item)) {
                        player.openInventory(adapt(player, items.get(item)));
                    }
                }
            }
        }
    }

    public static class RecipeSawerHolder implements FTSInvHolder {
        public static final RecipeSawerHolder INSTANCE = new RecipeSawerHolder();
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

    public static class RecipeChooseHolder implements FTSInvHolder {
        public static final RecipeChooseHolder INSTANCE = new RecipeChooseHolder();
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

    public static class RecipeCraftHolder implements FTSInvHolder {
        public static final RecipeCraftHolder INSTANCE = new RecipeCraftHolder();
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
