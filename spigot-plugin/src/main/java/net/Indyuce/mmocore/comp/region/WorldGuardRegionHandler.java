package net.Indyuce.mmocore.comp.region;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;

public class WorldGuardRegionHandler implements RegionHandler {

	@Override
	public List<String> getRegions(Location loc) {
		List<String> regions = new ArrayList<>();
		WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(loc)).getRegions().forEach(region -> regions.add(region.getId()));
		return regions;
	}
}
