package fts.chunkrestore;

import fts.FunctionalToolSet;
import fts.ResourceUtils;
import fts.api.BlockApi;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.BlockPopulator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class ChunkRestore implements Listener {
    private static YamlConfiguration yaml;
    private static int judgeTime;
    private static HashMap<String, Long> lastChangeStamps = new HashMap<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("chunkrestore.yml");
        File file = new File(plugin.getDataFolder(), "chunkrestore.yml");
        yaml = YamlConfiguration.loadConfiguration(file);

        //        if (!yaml.getBoolean("enable")) {
        //            return;
        //        }

        //        judgeTime = yaml.getInt("judgeTime");
        //
        //        load();
        //        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void regenChunk(Chunk chunk) {
        World world = chunk.getWorld();
        world.unloadChunk(chunk);
        world.regenerateChunk(chunk.getX(), chunk.getZ());
        for (BlockPopulator populator : world.getGenerator().getDefaultPopulators(world)) {
            populator.populate(world, new Random(), chunk);
        }
        world.loadChunk(chunk);
    }

    public static void save() {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/chunkrestore/chunkstamps.yml");
        YamlConfiguration stampYaml = YamlConfiguration.loadConfiguration(file);
        for (String toString : lastChangeStamps.keySet()) {
            stampYaml.set(toString, lastChangeStamps.get(toString));
        }
        try {
            stampYaml.save(file);
        } catch (IOException e) {
            FunctionalToolSet.getInstance().getLogger().info("区块时间戳保存错误！");
        }
    }

    public static void load() {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/chunkrestore/chunkstamps.yml");
        YamlConfiguration stampYaml = YamlConfiguration.loadConfiguration(file);
        for (String path : stampYaml.getKeys(false)) {
            lastChangeStamps.put(path, yaml.getLong(path));
        }
    }

    @EventHandler
    public void onLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String toString = BlockApi.locToStr(new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ()));
        if (event.isNewChunk()) {
            lastChangeStamps.put(toString, System.currentTimeMillis());
        }
        if ((!event.isNewChunk()) && chunk.getTileEntities().length == 0) {
            if (!lastChangeStamps.containsKey(toString)) {
                lastChangeStamps.put(toString, System.currentTimeMillis());
            }
            long lastChangeStamp = lastChangeStamps.get(toString);
            if (System.currentTimeMillis() - lastChangeStamp >= judgeTime * 1000) {
                regenChunk(chunk);
                lastChangeStamps.put(toString, System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onChange(BlockBreakEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        lastChangeStamps.put(BlockApi.locToStr(new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ())), System.currentTimeMillis());
    }

    @EventHandler
    public void onChange(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        lastChangeStamps.put(BlockApi.locToStr(new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ())), System.currentTimeMillis());
    }
}
