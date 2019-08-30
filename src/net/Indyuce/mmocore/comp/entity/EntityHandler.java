package net.Indyuce.mmocore.comp.entity;

import org.bukkit.entity.Entity;

public interface EntityHandler {

	/*
	 * this method lets you check if a specific entity was created using a plugin
	 * and therefore which should not be a skill target.
	 */
	boolean isCustomEntity(Entity entity);
}
