package net.Indyuce.mmocore.comp.holograms;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class HologramSupport {
	/**
	 * Displays a message using a hologram
	 * 
	 * @param loc     The location at which the message should be displayed
	 * @param message The message to display
	 * @param player  Source player, can be null in some cases source player for
	 *                instance for regen holograms
	 */
	public abstract void displayIndicator(Location loc, String message, @Nullable Player player);

	public void displayIndicator(Location loc, String message) {
		displayIndicator(loc, message, null);
	}

	public void displayIndicator(Player player, String message) {
		displayIndicator(player.getLocation().add(0, 1, 0), message, player);
	}
}
