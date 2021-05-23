package fts.gui.customrecipes.gui.player;

import fts.FunctionalToolSet;
import fts.gui.customrecipes.CustomRecipes;
import fts.gui.customrecipes.gui.shared.RecipeSawer;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RecipeWorkbench implements Listener {
    public static HashMap<UUID, Inventory> invs = new HashMap<UUID, Inventory>();
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

    public static Inventory initInv() {
        Inventory inv = Bukkit.createInventory(new WorkbenchHolder(), 54, "合成面板");
        Material type;
        try {
            type = Material.valueOf("STAINED_GLASS_PANE");
        } catch (Exception exception) {
            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, getItemStack(type, 15, "§8边框", new ArrayList<String>()));
        }
        inv.setItem(10, getItemStack(type, 0, "§6此处物品合成失败不会消失", new ArrayList<>()));
        inv.setItem(11, getItemStack(type, 0, "§6此处物品合成失败不会消失", new ArrayList<>()));
        inv.setItem(12, getItemStack(type, 7, "§6此处物品合成失败50%几率消失", new ArrayList<>()));
        inv.setItem(13, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));
        inv.setItem(19, getItemStack(type, 0, "§6此处物品合成失败不会消失", new ArrayList<>()));
        inv.setItem(20, getItemStack(type, 0, "§6此处物品合成失败不会消失", new ArrayList<>()));
        inv.setItem(21, getItemStack(type, 7, "§6此处物品合成失败50%几率消失", new ArrayList<>()));
        inv.setItem(22, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));
        inv.setItem(28, getItemStack(type, 7, "§6此处物品合成失败50%几率消失", new ArrayList<>()));
        inv.setItem(29, getItemStack(type, 7, "§6此处物品合成失败50%几率消失", new ArrayList<>()));
        inv.setItem(30, getItemStack(type, 7, "§6此处物品合成失败50%几率消失", new ArrayList<>()));
        inv.setItem(31, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));
        inv.setItem(37, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));
        inv.setItem(38, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));
        inv.setItem(39, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));
        inv.setItem(40, getItemStack(type, 3, "§6此处物品合成失败时会消失", new ArrayList<>()));

        inv.setItem(16, getItemStack(Material.SLIME_BALL, 0, "§6点击合成", new ArrayList<>()));
        inv.setItem(25, new ItemStack(Material.AIR));
        inv.setItem(34, getItemStack(Material.MAP, 0, "§6查询所有配方", new ArrayList<>()));
        inv.setItem(43, getItemStack(Material.BARRIER, 0, "§6提成道具",
                new ArrayList<String>() {
                    {
                        add("§r将提成道具摆放于此");
                        add("§r合成时消耗，用以提升成功率");
                    }
                }
        ));
        return inv;
    }

    public static int getId(Recipe recipe) {
        for (int id : FileManager.recipes.keySet()) {
            if (FileManager.recipes.get(id).equals(recipe)) {
                return id;
            }
        }
        return -1;
    }

    public static int getRaise(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasLore()) {
                for (String line : meta.getLore()) {
                    if (line.contains("§6提升成功率:§r ")) {
                        line = line.replace("§6提升成功率:§r ", "");
                        line = line.replace("/100", "");
                        return Integer.parseInt(line);
                    }
                }
            }
        }
        return -1;
    }

    public static Recipe goCraft(Player player, HashMap<Integer, ItemStack> materials) {
        for (Recipe recipe : FileManager.recipes.values()) {
            if (recipe.materials.equals(materials)) {
                if (player.getLevel() < recipe.exp) {
                    player.sendMessage("§6[合成系统]§r 合成所需经验不足！");
                    return null;
                }
                if (FunctionalToolSet.vaultEconomy.getBalance(player) < recipe.money) {
                    player.sendMessage("§6[合成系统]§r 合成所需货币不足！");
                    return null;
                }
                return recipe;
            }
        }
        player.sendMessage("§6[合成系统]§r 合成所需物品不足或多余！");
        return null;
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
        if (event.getInventory().getHolder() instanceof WorkbenchHolder) {
            openers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof WorkbenchHolder) {
            openers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (openers.contains(event.getWhoClicked().getUniqueId())) {
            for (int slot : event.getInventorySlots()) {
                if (!slots.contains(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (openers.contains(player.getUniqueId()) && inv != null) {
            if ((inv.getHolder() instanceof WorkbenchHolder) && !slots.contains(event.getSlot())) {
                event.setCancelled(true);
            }
        }
        if (inv != null) {
            if (inv.getHolder() instanceof WorkbenchHolder) {
                int slot = event.getSlot();
                if (slots.contains(slot)) {
                    if (inv.getItem(slot) != null) {
                        ItemStack item = inv.getItem(slot);
                        Material type;
                        try {
                            type = Material.valueOf("STAINED_GLASS_PANE");
                        } catch (Exception exception) {
                            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
                        }
                        if (item.getType() == type) {
                            if (item.hasItemMeta()) {
                                if (item.getItemMeta().hasDisplayName()) {
                                    if (item.getItemMeta().getDisplayName().contains("合成失败")) {
                                        event.setCancelled(true);
                                        if (event.getCursor() != null) {
                                            event.setCurrentItem(event.getCursor());
                                            event.setCursor(new ItemStack(Material.AIR));
                                        } else {
                                            event.setCurrentItem(new ItemStack(Material.AIR));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (slot == 16) {
                    if (inv.getItem(25) != null) {
                        if (inv.getItem(25).getType() != Material.AIR) {
                            player.sendMessage("§6[合成系统]§r 请先取下成品栏的物品！");
                            return;
                        }
                    }
                    for (int slot$ : slots) {
                        if (inv.getItem(slot$) == null) {
                            continue;
                        }
                        if (inv.getItem(slot$).getType() == Material.AIR) {
                            continue;
                        }
                        ItemStack item = inv.getItem(slot$);
                        Material type;
                        try {
                            type = Material.valueOf("STAINED_GLASS_PANE");
                        } catch (Exception exception) {
                            type = Material.valueOf("LEGACY_STAINED_GLASS_PANE");
                        }
                        if (item.getType() == type) {
                            if (item.hasItemMeta()) {
                                if (item.getItemMeta().hasDisplayName()) {
                                    if (item.getItemMeta().getDisplayName().contains("合成失败")) {
                                        inv.setItem(slot$, new ItemStack(Material.AIR));
                                    }
                                }
                            }
                        }
                    }
                    HashMap<Integer, ItemStack> materials = new HashMap<>();
                    for (int slot$ : slots) {
                        if (inv.getItem(slot$) == null) {
                            continue;
                        }
                        if (inv.getItem(slot$).getType() == Material.AIR) {
                            continue;
                        }
                        materials.put(slot$, inv.getItem(slot$));
                    }
                    Recipe recipe = goCraft(player, materials);
                    if (recipe == null) {
                        return;
                    }
                    boolean removeExtra = true;
                    double percent = recipe.percent;
                    if (inv.getItem(43) != null) {
                        if (inv.getItem(43).getType() != Material.BARRIER) {
                            int raise = getRaise(inv.getItem(43));
                            if (raise == -1) {
                                removeExtra = false;
                            } else {
                                percent += raise;
                            }
                        }
                    }
                    int id = getId(recipe);
                    if (!FileManager.stats.containsKey(player.getUniqueId())) {
                        FileManager.load(player);
                    }
                    if (FileManager.stats.get(player.getUniqueId()).recipeCache.containsKey(id)) {
                        int times = FileManager.stats.get(player.getUniqueId()).recipeCache.get(id);
                        percent += times * CustomRecipes.successEx;
                    }
                    player.setLevel((int) (player.getLevel() - recipe.exp));
                    FunctionalToolSet.vaultEconomy.withdrawPlayer(player, recipe.money);
                    if (Math.random() * 100 <= percent) {
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
                        if (removeExtra) {
                            inv.setItem(43, new ItemStack(Material.AIR));
                        }

                        if (Math.random() * 100 < recipe.specialPercent) {
                            inv.setItem(25, recipe.specialResult.clone());
                            if (recipe.percent != 100) {
                                player.sendMessage("§6[合成系统]§r 合成成功，本配方下次合成成功率重置！");
                            } else {
                                player.sendMessage("§6[合成系统]§r 合成成功！");
                            }
                            player.sendMessage("§6[合成系统]§r 你触发了特殊合成，得到了异于预期的成品！");
                        } else {
                            inv.setItem(25, recipe.result.clone());
                            if (recipe.percent != 100) {
                                player.sendMessage("§6[合成系统]§r 合成成功，本配方下次合成成功率重置！");
                            } else {
                                player.sendMessage("§6[合成系统]§r 合成成功！");
                            }
                        }

                        FileManager.stats.get(player.getUniqueId()).recipeCache.put(id, 0);
                        FileManager.save(player);
                    } else {
                        if (Math.random() <= 0.5) {
                            inv.setItem(12, new ItemStack(Material.AIR));
                        }
                        if (Math.random() <= 0.5) {
                            inv.setItem(21, new ItemStack(Material.AIR));
                        }
                        if (Math.random() <= 0.5) {
                            inv.setItem(28, new ItemStack(Material.AIR));
                        }
                        if (Math.random() <= 0.5) {
                            inv.setItem(29, new ItemStack(Material.AIR));
                        }
                        if (Math.random() <= 0.5) {
                            inv.setItem(30, new ItemStack(Material.AIR));
                        }

                        inv.setItem(13, new ItemStack(Material.AIR));
                        inv.setItem(22, new ItemStack(Material.AIR));
                        inv.setItem(31, new ItemStack(Material.AIR));
                        inv.setItem(37, new ItemStack(Material.AIR));
                        inv.setItem(38, new ItemStack(Material.AIR));
                        inv.setItem(39, new ItemStack(Material.AIR));
                        inv.setItem(40, new ItemStack(Material.AIR));

                        if (removeExtra) {
                            inv.setItem(43, new ItemStack(Material.AIR));
                        }

                        player.sendMessage("§6[合成系统]§r 合成失败，本配方下次合成成功率增加" + CustomRecipes.successEx + "%！");

                        if (!FileManager.stats.containsKey(player.getUniqueId())) {
                            FileManager.load(player);
                        }
                        if (FileManager.stats.get(player.getUniqueId()).recipeCache == null) {
                            FileManager.load(player);
                        }
                        int times = FileManager.
                                stats.
                                get(player.getUniqueId())
                                .recipeCache.
                                        get(id);
                        FileManager.stats.get(player.getUniqueId()).recipeCache.put(id, times + 1);
                        FileManager.save(player);
                    }
                }
                if (slot == 25) {
                    if (inv.getItem(25) == null) {
                        return;
                    } else {
                        event.setCursor(inv.getItem(25).clone());
                        inv.setItem(25, new ItemStack(Material.AIR));
                    }
                }
                if (slot == 43) {
                    if (inv.getItem(43) == null) {
                        inv.setItem(43, event.getCursor().clone());
                        event.setCursor(new ItemStack(Material.AIR));
                    } else {
                        if (inv.getItem(43).getType() == Material.BARRIER) {
                            inv.setItem(43, event.getCursor().clone());
                            event.setCursor(new ItemStack(Material.AIR));
                        } else {
                            ItemStack tmp = inv.getItem(43).clone();
                            inv.setItem(43, event.getCursor().clone());
                            event.setCursor(tmp);
                        }
                    }
                }
                if (slot == 34) {
                    player.openInventory(RecipeSawer.chooseInv);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        invs.put(player.getUniqueId(), initInv());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        invs.remove(player.getUniqueId());
    }

    public static class WorkbenchHolder implements FTSInvHolder {
        public static final WorkbenchHolder INSTANCE = new WorkbenchHolder();
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
