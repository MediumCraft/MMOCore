package net.Indyuce.mmocore.api;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.MMOCore;
import io.lumine.mythic.lib.MythicLib;

public class ConfigMessage {
	private final List<String> messages;

	public ConfigMessage(String key) {
		messages = MMOCore.plugin.configManager.getMessage(key);
	}

	public ConfigMessage addPlaceholders(String... placeholders) {
		for (int n = 0; n < messages.size(); n++) {
			String line = messages.get(n);
			for (int j = 0; j < placeholders.length - 1; j += 2) {
				String placeholder = placeholders[j];
				if (line.contains("{" + placeholder + "}"))
					line = line.replace("{" + placeholder + "}", placeholders[j + 1]);
			}
			messages.set(n, line);
		}
		return this;
	}

	public void send(CommandSender sender) {
		messages.forEach(line -> sender.sendMessage(MythicLib.plugin.parseColors(line)));
	}

	public void send(Collection<? extends Player> players) {
		players.forEach(player -> messages.forEach(line -> player.sendMessage(MMOCore.plugin.placeholderParser.parse(player, line))));
	}

	public void sendAsJSon(Player player) {
		messages.forEach(line -> MythicLib.plugin.getVersion().getWrapper().sendJson(player, MythicLib.plugin.parseColors(line)));
	}
}
