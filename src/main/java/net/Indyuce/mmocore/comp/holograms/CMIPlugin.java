package net.Indyuce.mmocore.comp.holograms;

import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;

import net.Indyuce.mmocore.MMOCore;

public class CMIPlugin extends HologramSupport {
	@Override
	public void displayIndicator(Location loc, String format, Player player) {
		final CMIHologram hologram = new CMIHologram("MMOItems_" + UUID.randomUUID().toString(), loc);
		hologram.setLines(Collections.singletonList(format));
		// if (player != null)
//		hologram.hide(player.getUniqueId());
		CMI.getInstance().getHologramManager().addHologram(hologram);
		hologram.update();
		Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> CMI.getInstance().getHologramManager().removeHolo(hologram), 20);
	}
}
