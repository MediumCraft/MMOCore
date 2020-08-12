package net.Indyuce.mmocore.comp.placeholder;

import org.bukkit.OfflinePlayer;

import net.mmogroup.mmolib.MMOLib;

public class DefaultParser implements PlaceholderParser {

	@Override
	public String parse(OfflinePlayer player, String string) {
		return MMOLib.plugin.parseColors(string.replace("%player%", player.getName()));
	}
}
