package utes.trueexplode;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;
import utes.UntilTheEndServer;

import java.util.List;

public class TrueExplode implements Listener {
    public TrueExplode(){
        Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(BlockExplodeEvent event){
        if(event.isCancelled())
            return;
        event.setCancelled(true);
        createTrueExplode(event.blockList());
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event){
        if(event.isCancelled())
            return;
        event.setCancelled(true);
        createTrueExplode(event.blockList());
    }
    private static void createTrueExplode(List<Block> blocks){
        for (Block block : blocks){
            float x = (float)(-1.0D + 2.0 * Math.random());
            float y = (float)(1.0 * Math.random());
            float z = (float)(-1.0D + 2.0 * Math.random());
            FallingBlock fallingblock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
            fallingblock.setDropItem(false);
            fallingblock.setFireTicks(200);
            fallingblock.setVelocity(new Vector(x, y, z));
            block.setType(Material.AIR);
        }
    }
}