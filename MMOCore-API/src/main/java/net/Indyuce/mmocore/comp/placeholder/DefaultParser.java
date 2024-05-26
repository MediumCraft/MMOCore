package net.Indyuce.mmocore.comp.placeholder;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.OfflinePlayer;

import io.lumine.mythic.lib.MythicLib;

public class DefaultParser implements PlaceholderParser {

	@Override
	@BackwardsCompatibility(version = "1.12-SNAPSHOT")
	public String parse(OfflinePlayer player, String string) {
		// TODO remove use of confusing non-PAPI %player% placeholder
		return MythicLib.plugin.parseColors(string.replace("%player%", player.getName()));
	}
}
