package net.Indyuce.mmocore.comp.worldguard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class DefaultRegionHandler implements RegionHandler {

	@Override
	public List<String> getRegions(Location loc) {
		return new ArrayList<>();
	}
}
