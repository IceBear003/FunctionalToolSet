package fts.gui.checkplayer;

import fts.FunctionalToolSet;
import fts.spi.BlockApi;
import fts.spi.ItemFactory;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CheckContainers implements Listener {
    private static int judgeTime;
    private static HashMap<UUID, List<Inventory>> playerInvs = new HashMap<>();
    private static HashSet<UUID> openers = new HashSet<>();
    private static ArrayList<UUID> operators = new ArrayList<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("checkplayer.yml");
        File file = new File(plugin.getDataFolder(), "checkplayer.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("checkContainers")) {
            return;
        }

        judgeTime = yaml.getInt("judgeTime");

        Bukkit.getPluginManager().registerEvents(new CheckContainers(), plugin);
    }

    public static Inventory getContainers(OfflinePlayer player, Player seeker) {
        if (player == null) {
            return null;
        }
        if (!player.hasPlayedBefore()) {
            return null;
        }
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/checkcontainers/", player.getUniqueId().toString() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        List<Inventory> invs = new ArrayList<>();
        Inventory inv = initGui(player);
        int tot = 0;
        for (String path : yaml.getKeys(false)) {
            if (yaml.getString(path).equalsIgnoreCase("ENDER_CHEST")) {
                ItemStack item = new ItemStack(Material.ENDER_CHEST);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§c" + turnToString(Math.toIntExact((System.currentTimeMillis() - Long.parseLong(path)) / 1000)) + "前 打开");
                item.setItemMeta(meta);
                inv.addItem(item);
            } else {
                Location loc = BlockApi.strToLoc(yaml.getString(path));
                ItemStack item;
                if (loc.getBlock() != null) {
                    item = new ItemStack(loc.getBlock().getType());
                } else {
                    item = new ItemStack(Material.CHEST);
                }
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    continue;
                }
                meta.setDisplayName("§c" + turnToString(Math.toIntExact((System.currentTimeMillis() - Long.parseLong(path)) / 1000)) + "前 打开");
                List<String> lore = new ArrayList<>();
                lore.add("§e世界:§6" + loc.getWorld().getName());
                lore.add("§ex:§6" + loc.getBlockX());
                lore.add("§ey:§6" + loc.getBlockY());
                lore.add("§ez:§6" + loc.getBlockZ());
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.addItem(item);
            }
            if (!hasNull(inv)) {
                invs.add(inv);
                inv = initGui(player);
            }
        }
        if (hasNull(inv)) {
            invs.add(inv);
        }
        playerInvs.put(seeker.getUniqueId(), invs);
        return invs.get(0);

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

    private static Inventory initGui(OfflinePlayer player) {
        Inventory inv = Bukkit.createInventory(new HolderCheckContainerGui(), 54, "玩家" + player.getName() + "的容器打开记录");
        inv.setItem(45, createItem(Material.PAPER, 0, "§e上一页", new ArrayList<>()));
        for (int i = 0; i <= 6; i++) {
            inv.setItem(46 + i, createItem(ItemFactory.valueOf("STAINED_GLASS_PANE"), 15, "§e边框", new ArrayList<>()));
        }
        inv.setItem(53, createItem(Material.PAPER, 0, "§e下一页", new ArrayList<>()));
        return inv;
    }

    private static ItemStack createItem(Material type, int id, String name, List<String> lore) {
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setDurability((short) id);
        return item;
    }

    public static String turnToString(int time) {
        if (time <= 0) {
            return "";
        }
        int d = time / 3600 / 24;
        int h = time % (3600 * 24) / 3600;
        int m = time % 3600 / 60;
        int s = time % 60;
        if (d == 0) {
            if (h == 0) {
                if (m == 0) {
                    return s + "秒";
                } else {
                    return m + "分钟" + s + "秒";
                }
            } else {
                return h + "小时" + m + "分钟" + s + "秒";
            }
        } else {
            return d + "天" + h + "小时" + m + "分钟" + s + "秒";
        }
    }

    private static void save(HumanEntity player, Location loc) {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/checkcontainers/", player.getUniqueId().toString() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (loc != null) {
            yaml.set(String.valueOf(System.currentTimeMillis()), BlockApi.locToStr(loc));
        } else {
            yaml.set(String.valueOf(System.currentTimeMillis()), "ENDER_CHEST");
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            FunctionalToolSet.getInstance().getLogger().info(
                    ResourceUtils.getSpecialLang("error-while-save-container",
                            new ArrayList<String>() {
                                {
                                    add("{player}");
                                    add(player.getName());
                                }
                            })
            );
        }
    }

    private static void clear(Player player) {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/checkcontainers/", player.getUniqueId().toString() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String path : yaml.getKeys(false)) {
            if (System.currentTimeMillis() - Long.parseLong(path) >= judgeTime * 1000) {
                yaml.set(path, null);
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            FunctionalToolSet.getInstance().getLogger().info(
                    ResourceUtils.getSpecialLang("error-while-save-container",
                            new ArrayList<String>() {
                                {
                                    add("{player}");
                                    add(player.getName());
                                }
                            }));
        }
    }

    @EventHandler
    public void onOpenGui(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof HolderCheckContainerGui) {
            openers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onCloseGui(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof HolderCheckContainerGui) {
            openers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onOperate(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (openers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
        Inventory inv = event.getClickedInventory();
        if (inv.getHolder() instanceof HolderCheckContainerGui) {
            int slot = event.getSlot();
            ItemStack item = inv.getItem(slot);
            if (item == null) {
                return;
            }
            if (slot == 45) {
                List<Inventory> invs = playerInvs.get(player.getUniqueId());
                int index = invs.indexOf(inv);
                player.openInventory(invs.get(Math.max(index - 1, 0)));
            }
            if (slot == 53) {
                List<Inventory> invs = playerInvs.get(player.getUniqueId());
                int index = invs.indexOf(inv);
                player.openInventory(invs.get(Math.min(index + 1, invs.size() - 1)));
            }
            if (slot < 45) {
                List<String> lore = item.getItemMeta().getLore();
                try {
                    Location loc = new Location(Bukkit.getWorld(lore.get(0).replace("§e世界:§6", "")),
                            Integer.parseInt(lore.get(1).replace("§ex:§6", "")),
                            Integer.parseInt(lore.get(2).replace("§ey:§6", "")),
                            Integer.parseInt(lore.get(3).replace("§ez:§6", "")));
                    Container container = (Container) loc.getBlock().getState();
                    operators.add(player.getUniqueId());
                    player.openInventory(container.getInventory());
                    playerInvs.remove(player.getUniqueId());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            operators.remove(player.getUniqueId());
                        }
                    }.runTaskLater(FunctionalToolSet.getInstance(), 2L);
                } catch (Exception e) {
                    if (item.getType() == Material.ENDER_CHEST) {
                        OfflinePlayer owner = Bukkit.getOfflinePlayer(event.getView().getTitle().replace("玩家", "").replace("的容器打开记录", ""));
                        operators.add(player.getUniqueId());
                        player.openInventory(CheckInventory.getEnderChest(owner));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                operators.remove(player.getUniqueId());
                            }
                        }.runTaskLater(FunctionalToolSet.getInstance(), 2L);
                        return;
                    }
                    ResourceUtils.sendMessage(player, "cannot-open-container");
                    player.closeInventory();
                    playerInvs.remove(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clear(player);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (operators.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        Inventory inv = event.getInventory();
        if (inv == null) {
            return;
        }
        if (inv.getType() == InventoryType.ENDER_CHEST) {
            save(event.getPlayer(), null);
            return;
        }
        Location loc = inv.getLocation();
        if (loc == null) {
            return;
        }
        save(event.getPlayer(), loc);
    }
}
