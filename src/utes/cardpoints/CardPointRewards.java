package utes.cardpoints;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import utes.UntilTheEndServer;

//TODO 
/*
 * utes.cardpoints.double
 */
public class CardPointRewards implements Listener {
	private static YamlConfiguration yaml;
	public static HashMap<UUID, IPlayer> stats = new HashMap<UUID, IPlayer>();
	private static HashMap<String, List<String>> rewards = new HashMap<String, List<String>>();
	private static HashMap<String, Integer> needs = new HashMap<String, Integer>();
	private static HashMap<String, Boolean> consumes = new HashMap<String, Boolean>();
	private static int startDate;
	private static int period;

	public CardPointRewards() {
		File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "cardpoints.yml");
		if (!file.exists())
			UntilTheEndServer.getInstance().saveResource("cardpoints.yml", false);
		yaml = YamlConfiguration.loadConfiguration(file);

		startDate = yaml.getInt("startDate");
		if (startDate == -1) {
			yaml.set("startDate", LocalDate.now().getDayOfMonth());
			try {
				yaml.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		period = yaml.getInt("period");

		for (String path : yaml.getKeys(false)) {
			if (!path.startsWith("reward"))
				continue;
			needs.put(path, yaml.getInt(path + ".need"));
			rewards.put(path, yaml.getStringList(path + ".reward"));
			consumes.put(path, yaml.getBoolean(path + ".consume"));
		}

		LocalDate date = LocalDate.now();
		if (Math.abs(date.getDayOfMonth()) - startDate > period) {
			File dataFile = new File(UntilTheEndServer.getInstance().getDataFolder() + "/cardpoints/");
			dataFile.delete();
		}

		Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
	}

	public static void getReward(Player player, String reward, boolean isDouble) {
		IPlayer stat = stats.get(player.getUniqueId());
		int need = needs.get(reward);

		if (stat.points < need) {
			player.sendMessage("您的积分不足，无法领取此礼包");
			return;
		}
		if (stat.received.contains(reward)) {
			player.sendMessage("您的本赛季已经领取过此礼包");
			return;
		}
		if (isDouble && (!player.hasPermission("utes.cardpoints.double"))) {
			player.sendMessage("您的没有权限双倍领取此礼包");
			return;
		}

		if (consumes.get(reward)) {
			player.sendMessage("您" + (isDouble ? "双倍" : "") + "领取此礼包，消耗了§e" + need + "§r积分");
			stat.points -= need;
		} else
			player.sendMessage("您" + (isDouble ? "双倍" : "") + "领取了此礼包");

		for (String str : rewards.get(reward)) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("{player}", player.getName()));
			if (isDouble)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("{player}", player.getName()));
		}

		stat.received.add(reward);
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

	private static IPlayer loadYaml(Player player) {
		File file = new File(UntilTheEndServer.getInstance().getDataFolder() + "/cardpoints/",
				player.getUniqueId().toString() + ".yml");
		if (!file.exists())
			return (new IPlayer(0, new ArrayList<String>()));
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		IPlayer stat = new IPlayer(yaml.getInt("points"), yaml.getStringList("received"));
		return stat;
	}

	private static void saveYaml(Player player) {
		IPlayer stat = stats.get(player.getUniqueId());
		File file = new File(UntilTheEndServer.getInstance().getDataFolder() + "/cardpoints/",
				player.getUniqueId().toString() + ".yml");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set("points", stat.points);
		yaml.set("received", stat.received);
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void takePoints(CommandSender sender, Player player, int points) {
		if (player != null)
			if (sender.hasPermission("utes.cardpoints.take")) {
				IPlayer stat = stats.get(player.getUniqueId());
				stat.points -= points;
				player.sendMessage("您的积分减少了§6" + points + "§r点");
				sender.sendMessage("操作成功");
			} else
				sender.sendMessage("您没有权限！");
		else
			sender.sendMessage("玩家不存在或不在线！");
	}

	public static void givePoints(CommandSender sender, Player player, int points) {
		if (player != null)
			if (sender.hasPermission("utes.cardpoints.give")) {
				IPlayer stat = stats.get(player.getUniqueId());
				stat.points += points;
				player.sendMessage("您的积分增加了§6" + points + "§r点");
				sender.sendMessage("操作成功");
			} else
				sender.sendMessage("您没有权限！");
		else
			sender.sendMessage("玩家不存在或不在线！");
	}

	public static void setPoints(CommandSender sender, Player player, int points) {
		if (player != null)
			if (sender.hasPermission("utes.cardpoints.set")) {
				IPlayer stat = stats.get(player.getUniqueId());
				stat.points = points;
				player.sendMessage("您的积分被设置为§6" + points + "§r点");
				sender.sendMessage("操作成功");
			} else
				sender.sendMessage("您没有权限！");
		else
			sender.sendMessage("玩家不存在或不在线！");
	}

	public static void checkPoints(CommandSender sender, Player player) {
		if (player != null) {
			IPlayer stat = stats.get(player.getUniqueId());
			sender.sendMessage("玩家" + player.getName() + "拥有积分§e" + stat.points + "§r点");
		} else
			sender.sendMessage("玩家不存在或不在线！");
	}

	public static class IPlayer {
		public int points; 
		private List<String> received;

		public IPlayer(int points, List<String> received) {
			this.points = points;
			this.received = received;
		}
	}
}
