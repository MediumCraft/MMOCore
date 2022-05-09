package net.Indyuce.mmocore.api.condition;

import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.Indyuce.mmocore.MMOCore;

public class ConditionInstance {
	private final Entity entity;
	private final Location applied;
	private final List<String> regions;

	public ConditionInstance(Entity entity) {
		this(entity, entity.getLocation());
	}

	public ConditionInstance(Entity entity, Location applied) {
		this.entity = entity;
		this.regions = MMOCore.plugin.regionHandler.getRegions(this.applied = applied);
		
		regions.add("__global__");
	}

	public boolean isInRegion(String name) {
		return regions.contains(name);
	}

	public Location getAppliedLocation() {
		return applied;
	}

	public Entity getEntity() {
		return entity;
	}

	public Stream<String> getRegionStream() {
		return regions.stream();
	}
}
