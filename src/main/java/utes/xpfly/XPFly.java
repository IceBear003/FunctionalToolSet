package utes.xpfly;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import utes.UntilTheEndServer;

//TODO
/*
 * utes.xpfly.slowexhaust
 * utes.xpfly
 */
public class XPFly {
	private static ArrayList<UUID> flyingPlayers=new ArrayList<UUID>();
	private static YamlConfiguration yaml;
	private static int exhaustSpeed;
	
	public XPFly() {
		File file=new File(UntilTheEndServer.getInstance().getDataFolder(),"xpfly.yml");
		if(!file.exists()) 
			UntilTheEndServer.getInstance().saveResource("xpfly.yml",false);
		yaml=YamlConfiguration.loadConfiguration(file);
		if(!yaml.getBoolean("enable")) {
			return;
		}
		
		exhaustSpeed=yaml.getInt("exhaustSpeed");
		
		new BukkitRunnable() {
			long counter=0;
			@Override
			public void run() {
				counter++;
				for(UUID uuid:(ArrayList<UUID>)flyingPlayers.clone()) {
					Player player=Bukkit.getPlayer(uuid);
					if(player.getExp()<exhaustSpeed&&player.getLevel()==0) {
						player.sendMessage("您没有足够的经验，自动停止飞行");
						cancelFly(player);
					}

					float currentExp = player.getExp();
					float newExp = currentExp - exhaustSpeed;

					if (newExp < 0.0f) {
						player.setLevel(player.getLevel() - 1>=0?player.getLevel() - 1:0);
						player.setExp(1.0f + newExp);
					}

					if(player.hasPermission("utes.xpfly.slowexhaust")) {
						if(counter%2==0)
							player.setExp(newExp);
					} else
						player.setExp(newExp);
					
					if(player.isOnGround()&&player.isSneaking()) {
						player.sendMessage("您已经落地，自动停止飞行");
						cancelFly(player);
					}
				}
			}
			
		}.runTaskTimer(UntilTheEndServer.getInstance(),0L,20L);
	}

	public static void initXPFly(Player player) {
		if(!player.hasPermission("utes.xpfly")) {
			player.sendMessage("您没有权限使用经验飞行！");
			return;
		}
		if(flyingPlayers.contains(player.getUniqueId())) {
			cancelFly(player);
			player.sendMessage("经验飞行已经关闭");
		}else {
			goFly(player);
		}
	}
	
	private static void goFly(Player player) {
		if(player.getTotalExperience()<exhaustSpeed) {
			player.sendMessage("您没有足够的经验飞行！");
			return;
		}
		flyingPlayers.add(player.getUniqueId());
		player.setAllowFlight(true);
		player.setFlying(true);
		player.sendMessage("经验飞行已经开启");
	}
	private static void cancelFly(Player player) {
		flyingPlayers.remove(player.getUniqueId());
		player.setAllowFlight(false);
		player.setFlying(false);
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,100,4));
	}
}
