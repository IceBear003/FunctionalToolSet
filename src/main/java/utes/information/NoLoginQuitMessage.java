package utes.information;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import utes.UntilTheEndServer;

public class NoLoginQuitMessage implements Listener{
	public NoLoginQuitMessage() {
		if(TranslateMessage.yaml.getBoolean("enableJoinMessage")) {
			Bukkit.getPluginManager().registerEvents(this, UntilTheEndServer.getInstance());
		}
	}
	@EventHandler public void onJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
	}
	@EventHandler public void onQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
	}
}
