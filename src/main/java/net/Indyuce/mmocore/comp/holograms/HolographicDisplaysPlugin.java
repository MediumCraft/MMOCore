package net.Indyuce.mmocore.comp.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.Indyuce.mmocore.MMOCore;

public class HolographicDisplaysPlugin extends HologramSupport {
	@Override
	public void displayIndicator(Location loc, String format, Player player) {
		Hologram hologram = HologramsAPI.createHologram(MMOCore.plugin, loc);
		hologram.appendTextLine(format);
		if (player != null)
			hologram.getVisibilityManager().hideTo(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOCore.plugin, hologram::delete, 20);
	}
}
