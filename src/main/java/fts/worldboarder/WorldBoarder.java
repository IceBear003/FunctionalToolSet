package fts.worldboarder;

import fts.FunctionalToolSet;
import fts.spi.BlockApi;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WorldBoarder implements Listener {
    private static HashMap<String, BoarderType> types = new HashMap<>();
    private static HashMap<String, Boarder> boarders = new HashMap<>();
    private static ArrayList<String> particles = new ArrayList<>();
    private static ArrayList<UUID> players = new ArrayList<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("boarder.yml");
        File file = new File(plugin.getDataFolder(), "boarder.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (String path : yaml.getKeys(false)) {
            if (path.equalsIgnoreCase("enable")) {
                continue;
            }
            BoarderType type = BoarderType.valueOf(yaml.getString(path + ".type"));
            boolean isTransparent = yaml.getBoolean(path + ".isTransparent");
            switch (type) {
                case RECTANGLE:
                    boarders.put(path, new RectangleBoarder(isTransparent,
                            yaml.getInt(path + ".x1"), yaml.getInt(path + ".z1"),
                            yaml.getInt(path + ".x2"), yaml.getInt(path + ".z2")));
                    break;
                case ROUND:
                    boarders.put(path, new RoundBoarder(isTransparent, yaml.getInt(path + ".radius")));
                    break;
            }
            types.put(path, type);
        }

        Bukkit.getPluginManager().registerEvents(new WorldBoarder(), plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (boarders.containsKey(world.getName())) {
            if (player.hasPermission("fts.boarder.ignore")) {
                return;
            }
            Location loc = event.getTo().getBlock().getLocation();
            Boarder boarder = boarders.get(world.getName());
            BoarderType type = types.get(world.getName());

            boolean flag = false;
            switch (type) {
                case RECTANGLE:
                    flag = ((RectangleBoarder) boarder).isOutOfWorld(loc);
                    break;
                case ROUND:
                    flag = ((RoundBoarder) boarder).isOutOfWorld(loc);
                    break;
            }

            if (flag) {
                if (boarder.isTransparent && (!players.contains(player.getUniqueId()))) {
                    Location otherSide = boarder.getTheOtherSide(loc);
                    player.teleport(otherSide);
                    ResourceUtils.sendMessage(player, "go-to-the-other-side-1");
                    ResourceUtils.sendMessage(player, "go-to-the-other-side-2");
                    players.add(player.getUniqueId());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            players.remove(player.getUniqueId());
                            ResourceUtils.sendMessage(player, "go-to-the-other-side-3");
                        }
                    }.runTaskLater(FunctionalToolSet.getInstance(), 100L);
                } else {
                    event.setCancelled(true);
                    if (particles.contains(BlockApi.locToStr(loc))) {
                        return;
                    }
                    world.spawnParticle(Particle.BARRIER, loc, 1);
                    particles.add(BlockApi.locToStr(loc));
                    ResourceUtils.sendMessage(player, "move-out-of-the-boarder");
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (boarders.containsKey(world.getName())) {
            if (player.hasPermission("fts.boarder.ignore")) {
                return;
            }
            Location loc = event.getTo().getBlock().getLocation();
            Boarder boarder = boarders.get(world.getName());
            BoarderType type = types.get(world.getName());

            boolean flag = false;
            switch (type) {
                case RECTANGLE:
                    flag = ((RectangleBoarder) boarder).isOutOfWorld(loc);
                    break;
                case ROUND:
                    flag = ((RoundBoarder) boarder).isOutOfWorld(loc);
                    break;
            }

            if (flag) {
                event.setCancelled(true);
                ResourceUtils.sendMessage(player, "tp-out-of-the-boarder");
            }
        }
    }


}
