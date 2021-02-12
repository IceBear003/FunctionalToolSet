package fts.linkingdig;

import fts.FunctionalToolSet;
import fts.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class LinkingDig implements Listener {
    private static final HashSet<Location> linked = new HashSet<>();
    private static double exhaustSpeed;
    private static List<String> axe;
    private static List<String> pickaxe;
    private static List<String> spade;
    private static List<String> scissor;
    private static List<String> sword;

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("linkingdig.yml");
        File file = new File(plugin.getDataFolder(), "linkingdig.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        exhaustSpeed = yaml.getDouble("exhaustSpeed");
        axe = yaml.getStringList("AXE");
        pickaxe = yaml.getStringList("PICKAXE");
        spade = yaml.getStringList("SPADE");
        scissor = yaml.getStringList("SCISSOR");
        sword = yaml.getStringList("SWORD");

        Bukkit.getPluginManager().registerEvents(new LinkingDig(), plugin);
    }

    private static HashSet<Location> getNearbyBlocks(Material blockType, Location location, Player player, ItemStack tool) {
        HashSet<Location> blocks = new HashSet<>();
        Material toolType = tool.getType();
        double percent = 1;
        double foodlevel = player.getFoodLevel();

        if (tool.containsEnchantment(Enchantment.DURABILITY)) {
            int level = tool.getEnchantmentLevel(Enchantment.DURABILITY);
            percent /= (1 + level);
        }

        Queue<Location> queue = new LinkedList<>();
        ArrayList<Location> visited = new ArrayList<>();
        queue.offer(location);
        visited.add(location);

        while ((!queue.isEmpty()) && foodlevel >= 0 && tool.getDurability() <= toolType.getMaxDurability()) {
            Location currentLoc = queue.peek();
            queue.poll();
            visited.add(currentLoc);

            if (currentLoc.getBlock() == null) {
                continue;
            }

            linked.add(currentLoc);
            BlockBreakEvent event = new BlockBreakEvent(currentLoc.getBlock(), player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                linked.remove(currentLoc);
                continue;
            }

            int size = blocks.size();
            blocks.add(currentLoc);

            if (blocks.size() != size) {
                foodlevel -= exhaustSpeed;
                if (Math.random() <= percent) {
                    tool.setDurability((short) (tool.getDurability() + 1));
                }
            }

            ArrayList<Location> tmps = new ArrayList<>();
            tmps.add(currentLoc.clone().add(-1, 0, 0));
            tmps.add(currentLoc.clone().add(1, 0, 0));
            tmps.add(currentLoc.clone().add(0, 1, 0));
            tmps.add(currentLoc.clone().add(0, -1, 0));
            tmps.add(currentLoc.clone().add(0, 0, 1));
            tmps.add(currentLoc.clone().add(0, 0, -1));

            for (Location loc : tmps) {
                if (visited.contains(loc)) {
                    continue;
                }
                if (loc.getBlock().getType() != blockType) {
                    continue;
                }
                queue.offer(loc);
            }
        }

        if (tool.getDurability() >= tool.getType().getMaxDurability() - 1) {
            tool.setType(Material.AIR);
        }
        player.setFoodLevel((int) foodlevel);
        return blocks;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDig(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!player.hasPermission("fts.linkdig.use")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        if (linked.contains(block.getLocation())) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) {
            return;
        }
        Material toolType = tool.getType();
        Material blockType = block.getType();
        boolean flag = false;
        if (toolType.toString().contains("AXE")
                && !toolType.toString().contains("PICKAXE")) {
            for (String str : axe) {
                if (blockType.toString().contains(str)) {
                    flag = true;
                }
            }
        }
        if (toolType.toString().contains("PICKAXE")) {
            for (String str : pickaxe) {
                if (blockType.toString().contains(str)) {
                    flag = true;
                }
            }
        }
        if (toolType.toString().contains("SPADE")) {
            for (String str : spade) {
                if (blockType.toString().contains(str)) {
                    flag = true;
                }
            }
        }
        if (toolType.toString().contains("SWORD")) {
            for (String str : sword) {
                if (blockType.toString().contains(str)) {
                    flag = true;
                }
            }
        }
        if (toolType.toString().contains("SHEARS")) {
            for (String str : scissor) {
                if (blockType.toString().contains(str)) {
                    flag = true;
                }
            }
        }
        if (flag) {
            for (Location nearBlock : getNearbyBlocks(blockType, block.getLocation(), player, tool)) {
                nearBlock.getBlock().breakNaturally();
                linked.remove(nearBlock);
            }
        }
    }
}
