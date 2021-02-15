package fts.cardpoints;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CardPointRewards implements Listener {
    private static final HashMap<String, List<String>> rewards = new HashMap<>();
    private static final HashMap<String, Integer> needs = new HashMap<>();
    private static final HashMap<String, Boolean> consumes = new HashMap<>();
    public static HashMap<UUID, IPlayer> stats = new HashMap<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("cardpoints.yml");
        File file = new File(plugin.getDataFolder(), "cardpoints.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            stats.put(player.getUniqueId(), loadYaml(player));
        }

        int startDate = yaml.getInt("startDate");
        if (startDate == -1) {
            yaml.set("startDate", LocalDate.now().getDayOfMonth());
            try {
                yaml.save(file);
            } catch (IOException e) {
                FunctionalToolSet.getInstance().getLogger().info(
                        ResourceUtils.getLang("error-while-save-time")
                );
            }
        }
        int period = yaml.getInt("period");

        for (String path : yaml.getKeys(false)) {
            if (!path.startsWith("reward")) {
                continue;
            }
            needs.put(path, yaml.getInt(path + ".need"));
            rewards.put(path, yaml.getStringList(path + ".reward"));
            consumes.put(path, yaml.getBoolean(path + ".consume"));
        }

        LocalDate date = LocalDate.now();
        if (Math.abs(date.getDayOfMonth()) - startDate > period) {
            File dataFile = new File(plugin.getDataFolder() + "/cardpoints/");
            dataFile.delete();
        }

        Bukkit.getPluginManager().registerEvents(new CardPointRewards(), plugin);
    }

    public static void getReward(Player player, String reward, boolean isDouble) {
        IPlayer stat = stats.get(player.getUniqueId());
        int need = needs.get(reward);

        if (stat.points < need) {
            ResourceUtils.sendMessage(player, "not-enough-cardpoints");
            return;
        }
        if (stat.received.contains(reward)) {
            ResourceUtils.sendMessage(player, "already-received-reward");
            return;
        }
        if (isDouble && (!player.hasPermission("fts.cardpoints.double"))) {
            ResourceUtils.sendMessage(player, "no-permission-double-receive");
            return;
        }

        if (consumes.get(reward)) {
            ResourceUtils.sendSpecialMessage(player, "receive-reward-consume",
                    new ArrayList<String>() {
                        {
                            add("{isDouble}");
                            add(isDouble ? "双倍" : "");
                            add("{points}");
                            add(String.valueOf(need));
                        }
                    });
            stat.points -= need;
        } else {
            ResourceUtils.sendSpecialMessage(player, "receive-reward-unconsume",
                    new ArrayList<String>() {
                        {
                            add("{isDouble}");
                            add(isDouble ? "双倍" : "");
                        }
                    });
        }

        for (String str : rewards.get(reward)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("{player}", player.getName()));
            if (isDouble) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("{player}", player.getName()));
            }
        }

        stat.received.add(reward);
    }

    private static IPlayer loadYaml(Player player) {
        File file = new File(FunctionalToolSet.getInstance().getDataFolder() + "/cardpoints/",
                player.getUniqueId().toString() + ".yml");
        if (!file.exists()) {
            return (new IPlayer(0, new ArrayList<>()));
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return new IPlayer(yaml.getInt("points"), yaml.getStringList("received"));
    }

    public static void saveYaml(Player player) {
        IPlayer stat = stats.get(player.getUniqueId());
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

    public static void takePoints(CommandSender sender, Player player, int points) {
        if (player != null) {
            if (sender.hasPermission("fts.cardpoints.take")) {
                IPlayer stat = stats.get(player.getUniqueId());
                stat.points -= points;
                ResourceUtils.sendSpecialMessage(player, "take-points",
                        new ArrayList<String>() {
                            {
                                add("{points}");
                                add(String.valueOf(points));
                            }
                        });
                ResourceUtils.sendMessage(sender, "successfully-modify-points");
            } else {
                ResourceUtils.sendMessage(sender, "no-permission-modify-points");
            }
        } else {
            ResourceUtils.sendMessage(sender, "no-such-a-player");
        }
    }

    public static void givePoints(CommandSender sender, Player player, int points) {
        if (player != null) {
            if (sender.hasPermission("fts.cardpoints.give")) {
                IPlayer stat = stats.get(player.getUniqueId());
                stat.points += points;
                ResourceUtils.sendSpecialMessage(player, "add-points",
                        new ArrayList<String>() {
                            {
                                add("{points}");
                                add(String.valueOf(points));
                            }
                        });
                ResourceUtils.sendMessage(sender, "successfully-modify-points");
            } else {
                ResourceUtils.sendMessage(sender, "no-permission-modify-points");
            }
        } else {
            ResourceUtils.sendMessage(sender, "no-such-a-player");
        }
    }

    public static void setPoints(CommandSender sender, Player player, int points) {
        if (player != null) {
            if (sender.hasPermission("fts.cardpoints.set")) {
                IPlayer stat = stats.get(player.getUniqueId());
                stat.points = points;
                ResourceUtils.sendSpecialMessage(player, "set-points",
                        new ArrayList<String>() {
                            {
                                add("{points}");
                                add(String.valueOf(points));
                            }
                        });
                ResourceUtils.sendMessage(sender, "successfully-modify-points");
            } else {
                ResourceUtils.sendMessage(sender, "no-permission-modify-points");
            }
        } else {
            ResourceUtils.sendMessage(sender, "no-such-a-player");
        }
    }

    public static void checkPoints(CommandSender sender, Player player) {
        if (player != null) {
            if (!sender.hasPermission("fts.cardpoints.check")) {
                ResourceUtils.sendMessage(sender, "no-permission-checkpoints");
                return;
            }
            IPlayer stat = stats.get(player.getUniqueId());
            ResourceUtils.sendSpecialMessage(sender, "check-points",
                    new ArrayList<String>() {
                        {
                            add("{player}");
                            add(player.getName());
                            add("{points}");
                            String.valueOf(stat.points);
                        }
                    });
        } else {
            ResourceUtils.sendMessage(sender, "no-such-a-player");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        stats.put(player.getUniqueId(), loadYaml(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        saveYaml(player);
        stats.remove(player.getUniqueId());
    }

    public static class IPlayer {
        private final List<String> received;
        public int points;

        public IPlayer(int points, List<String> received) {
            this.points = points;
            this.received = received;
        }
    }
}
