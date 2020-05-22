package net.Indyuce.mmocore.comp.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FlagPlugin {
//	public boolean isPvpAllowed(Location loc);

	public boolean isFlagAllowed(Player player, CustomFlag flag);

	public boolean isFlagAllowed(Location loc, CustomFlag flag);

	public enum CustomFlag {
		SKILLS;

		public String getPath() {
			return "mmocore-" + name().toLowerCase();
		}
	}
}
