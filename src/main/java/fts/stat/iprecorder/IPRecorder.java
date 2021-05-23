package fts.stat.iprecorder;

import fts.FunctionalToolSet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;

public class IPRecorder implements Listener {
    public static void initialize(FunctionalToolSet plugin) {
        Bukkit.getPluginManager().registerEvents(new IPRecorder(), plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/ips/", player.getName() + ".yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set("ip", player.getAddress().toString());
        yaml.set("uuid", player.getUniqueId());
        try {
            yaml.save(file);
        } catch (IOException e) {
            FunctionalToolSet.getInstance().getLogger().warning("保存玩家" + player.getName() + "信息时出现错误！");
        }
    }
}
