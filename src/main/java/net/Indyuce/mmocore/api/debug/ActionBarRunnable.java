package net.Indyuce.mmocore.api.debug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionBarRunnable extends BukkitRunnable {

	/*
	 * how to enable it: set 'debug' to true in the config and use the
	 * 'debug-action-bar' string parameter. hot changes, only need /mmocore
	 * reload.
	 */

	@Override
	public void run() {
		Bukkit.getOnlinePlayers().forEach(player -> sendActionBar(player));
	}

	private void sendActionBar(Player player) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MMOCore.plugin.placeholderParser.parse(player, getMessage())));
	}

	private String getMessage() {
		String str = MMOCore.plugin.getConfig().getString("debug-action-bar.format");
		return str == null ? "" : str;
	}
}
