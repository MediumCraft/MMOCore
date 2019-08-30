package net.Indyuce.mmocore.comp.placeholder;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class DefaultParser implements PlaceholderParser {
	@Override
	public String parse(OfflinePlayer player, String string) {
		return ChatColor.translateAlternateColorCodes('&', string.replace("%player%", player.getName()));
	}
}
