package fts.stat.cardpoints;

import fts.FunctionalToolSet;
import fts.spi.ResourceUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CardPoints {
    public static final HashMap<String, List<String>> rewards = new HashMap<>();
    public static final HashMap<String, Integer> needs = new HashMap<>();
    public static final HashMap<String, Boolean> consumes = new HashMap<>();
    public static HashMap<UUID, IPlayerCardPoints> stats = new HashMap<>();

    public static void initialize(FunctionalToolSet plugin) {
        ResourceUtils.autoUpdateConfigs("cardpoints.yml");
        File file = new File(plugin.getDataFolder(), "cardpoints.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.getBoolean("enable")) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            stats.put(player.getUniqueId(), CardPointsIO.load(player));
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

        Bukkit.getPluginManager().registerEvents(new CardPointsIO(), plugin);
    }

    public static void getReward(Player player, String reward, boolean isDouble) {
        IPlayerCardPoints stat = stats.get(player.getUniqueId());
        int need = needs.get(reward);

        if (stat.points < need) {
            ResourceUtils.sendMessage(player, "not-enough-cardpoints");
            return;
        }
        if (stat.received.contains(reward)) {
            ResourceUtils.sendMessage(player, "already-received-reward");
            return;
        }
        if (isDouble && (!player.hasPermission("fts.stat.cardpoints.double"))) {
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

    public static void takePoints(CommandSender sender, Player player, int points) {
        if (player != null) {
            if (sender.hasPermission("fts.stat.cardpoints.take")) {
                IPlayerCardPoints stat = stats.get(player.getUniqueId());
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
            if (sender.hasPermission("fts.stat.cardpoints.give")) {
                IPlayerCardPoints stat = stats.get(player.getUniqueId());
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
            if (sender.hasPermission("fts.stat.cardpoints.set")) {
                IPlayerCardPoints stat = stats.get(player.getUniqueId());
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
            if (!sender.hasPermission("fts.stat.cardpoints.check")) {
                ResourceUtils.sendMessage(sender, "no-permission-checkpoints");
                return;
            }
            IPlayerCardPoints stat = stats.get(player.getUniqueId());
            ResourceUtils.sendSpecialMessage(sender, "check-points",
                    new ArrayList<String>() {
                        {
                            add("{player}");
                            add(player.getName());
                            add("{points}");
                            add(String.valueOf(stat.points));
                        }
                    });
        } else {
            ResourceUtils.sendMessage(sender, "no-such-a-player");
        }
    }
}
