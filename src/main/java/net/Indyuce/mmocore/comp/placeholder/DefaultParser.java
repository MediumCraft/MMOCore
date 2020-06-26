package net.Indyuce.mmocore.comp.placeholder;

import org.bukkit.OfflinePlayer;

import net.asangarin.hexcolors.ColorParse;

public class DefaultParser implements PlaceholderParser {
	@Override
	public String parse(OfflinePlayer player, String string) {
		return new ColorParse('&', string.replace("%player%", player.getName())).toChatColor();
	}
}
