package fts.trueexplode;

import fts.FunctionalToolSet;
import fts.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;

public class TrueExplode implements Listener {
    public static void initialize(FunctionalToolSet plugin) {

        ResourceUtils.autoUpdateConfigs("trueexplode.yml");
        File file = new File(plugin.getDataFolder(), "trueexplode.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(new TrueExplode(), plugin);
    }

    private static void createTrueExplode(List<Block> blocks) {
        for (Block block : blocks) {
            float x = (float) (-1.0D + 2.0 * Math.random());
            float y = (float) (1.0 * Math.random());
            float z = (float) (-1.0D + 2.0 * Math.random());
            FallingBlock fallingblock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
            fallingblock.setDropItem(false);
            fallingblock.setFireTicks(200);
            fallingblock.setVelocity(new Vector(x, y, z));
            block.setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(true);
        createTrueExplode(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(true);
        createTrueExplode(event.blockList());
    }
}
