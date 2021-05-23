package fts.mechanism.world.chunkrestore;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class ChunkRestore implements Listener {
    public static void regenChunk(Chunk chunk) {
        World world = chunk.getWorld();
        world.unloadChunk(chunk);
        world.regenerateChunk(chunk.getX(), chunk.getZ());
        for (BlockPopulator populator : world.getGenerator().getDefaultPopulators(world)) {
            populator.populate(world, new Random(), chunk);
        }
        world.loadChunk(chunk);
    }
}
