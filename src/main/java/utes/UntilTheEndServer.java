package utes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import utes.information.NoLoginQuitMessage;
import utes.information.TranslateMessage;
import utes.lift.IronBlockLift;
import utes.onlinetimes.OnlineTimes;
import utes.particle.ParticleOverHead;
import utes.particle.ParticleUnderFeet;
import utes.randomcredit.RandomCredits;
import utes.rtp.RandomTeleport;
import utes.scoreboard.ScoreBoard;
import utes.xpfly.XPFly;

public class UntilTheEndServer extends JavaPlugin {
	private static UntilTheEndServer instance;
	public static ProtocolManager pm;

	@Override public void onEnable() {
		try {
		System.out.println("[UntilTheEndServer] 正在启用核心功能插件UTES中...");
		instance=this;
		
		System.out.println("[UntilTheEndServer] 正在启用收发包控制中...");
		pm=ProtocolLibrary.getProtocolManager();

		System.out.println("[UntilTheEndServer] 正在注册指令中...");
		this.getCommand("utes").setExecutor(new UTESCommands());
		
		System.out.println("[UntilTheEndServer] 正在启用随机传送功能中...");
		new RandomTeleport();
		
		System.out.println("[UntilTheEndServer] 正在启用经验飞行功能中...");
		new XPFly();
		
		System.out.println("[UntilTheEndServer] 正在启用计分板功能中...");
		new ScoreBoard();
		
		System.out.println("[UntilTheEndServer] 正在启用铁块电梯功能中...");
		new IronBlockLift();
		
		System.out.println("[UntilTheEndServer] 正在启用屏蔽进出信息功能中...");
		new NoLoginQuitMessage();
		
		System.out.println("[UntilTheEndServer] 正在启用增加信息前缀功能中...");
		new TranslateMessage();
		
		System.out.println("[UntilTheEndServer] 正在启用统计在线时间功能中...");
		new OnlineTimes();
		
		System.out.println("[UntilTheEndServer] 正在启用粒子特效功能中...");
		new ParticleOverHead();
		new ParticleUnderFeet();
		
		System.out.println("[UntilTheEndServer] 正在启用随机抽奖功能中...");
		new RandomCredits();
		
		} catch(Exception exception) {
			System.out.println("[UntilTheEndServer] 哎呀这步好像出了些小问题呢！");
			exception.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		for(Player player:Bukkit.getOnlinePlayers()) {
			player.kickPlayer("服务器重载，请稍后再进");
		}
	}

	public static UntilTheEndServer getInstance() {
		return instance;
	}
}
