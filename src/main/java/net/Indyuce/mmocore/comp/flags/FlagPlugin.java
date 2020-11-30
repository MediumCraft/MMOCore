package net.Indyuce.mmocore.comp.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FlagPlugin {
//	public boolean isPvpAllowed(Location loc);

	boolean isFlagAllowed(Player player, CustomFlag flag);

	boolean isFlagAllowed(Location loc, CustomFlag flag);

	enum CustomFlag {
		SKILLS;

		public String getPath() {
			return "mmocore-" + name().toLowerCase();
		}
	}
}
