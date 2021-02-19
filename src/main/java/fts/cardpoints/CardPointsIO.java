package fts.cardpoints;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CardPointsIO implements Listener {
    public static IPlayerCardPoints load(Player player) {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/cardpoints/",
                player.getUniqueId().toString() + ".yml");
        if (!file.exists()) {
            return (new IPlayerCardPoints(0, new ArrayList<>()));
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return new IPlayerCardPoints(yaml.getInt("points"), yaml.getStringList("received"));
    }

    public static void save(Player player) {
        IPlayerCardPoints stat = CardPoints.stats.get(player.getUniqueId());
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/cardpoints/",
                player.getUniqueId().toString() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set("points", stat.points);
        yaml.set("received", stat.received);
        try {
            yaml.save(file);
        } catch (IOException e) {
            FunctionalToolSet.getInstance().getLogger().info(
                    ResourceUtils.getSpecialLang("error-while-save-points",
                            new ArrayList<String>() {
                                {
                                    add("{player}");
                                    add(player.getName());
                                }
                            })
            );
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CardPoints.stats.put(player.getUniqueId(), CardPointsIO.load(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CardPointsIO.save(player);
        CardPoints.stats.remove(player.getUniqueId());
    }
}
