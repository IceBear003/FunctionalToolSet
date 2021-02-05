package utes.information;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import utes.UntilTheEndServer;

public class TranslateMessage {
	public static YamlConfiguration yaml;
	private static List<String> origins;
	private static List<String> adapteds;
	private static String prefix;

	public TranslateMessage() {
		File file = new File(UntilTheEndServer.getInstance().getDataFolder(), "information.yml");
		if (!file.exists())
			UntilTheEndServer.getInstance().saveResource("information.yml", false);
		yaml = YamlConfiguration.loadConfiguration(file);

		origins = yaml.getStringList("origins");
		adapteds = yaml.getStringList("adapted");
		prefix = yaml.getString("prefix");

		UntilTheEndServer.pm
				.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(UntilTheEndServer.getInstance())
						.serverSide().listenerPriority(ListenerPriority.LOW).gamePhase(GamePhase.PLAYING).optionAsync()
						.options(ListenerOptions.SKIP_PLUGIN_VERIFIER).types(PacketType.Play.Server.CHAT)) {
					@Override
					public void onPacketSending(PacketEvent event) {
						PacketContainer packet = event.getPacket();
						PacketType packetType = event.getPacketType();
						if (packetType.equals(PacketType.Play.Server.CHAT)) {
							if (packet.getChatTypes().getValues().get(0) != ChatType.SYSTEM)
								return;
							WrappedChatComponent warppedComponent = packet.getChatComponents().getValues().get(0);
							String json = warppedComponent.getJson();

							BaseComponent[] origin = ComponentSerializer.parse(json);
							String message = TextComponent.toLegacyText(origin);
							if (!message.contains(prefix)&&!message.contains("{ignore}")) {
								message = prefix + message;
							}
							message=message.replace("{ignore}","");

							for (int index = 0; index < origins.size(); index++)
								message = message.replace(origins.get(index), adapteds.get(index));

							BaseComponent[] adapted = TextComponent.fromLegacyText(message);
							String newJson = ComponentSerializer.toString(adapted);

							warppedComponent.setJson(newJson);
							packet.getChatComponents().write(0, warppedComponent);
						}
					}
				});

	}
}
