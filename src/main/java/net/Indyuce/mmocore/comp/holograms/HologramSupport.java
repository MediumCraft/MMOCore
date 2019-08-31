package net.Indyuce.mmocore.comp.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class HologramSupport {

	/*
	 * the third argument is the player which the hologram needs to be hidden
	 * from to prevent the indicator from taking too much space on the player
	 * screen
	 */
	public abstract void displayIndicator(Location loc, String message, Player player);

	public void displayIndicator(Location loc, String message) {
		displayIndicator(loc, message, null);
	}

	public void displayIndicator(Player player, String message) {
		displayIndicator(player.getLocation().add(0, 1, 0), message, player);
	}
}
