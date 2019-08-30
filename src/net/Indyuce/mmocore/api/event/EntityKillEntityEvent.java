package net.Indyuce.mmocore.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class EntityKillEntityEvent extends EntityEvent {
	private static final HandlerList handlers = new HandlerList();

	private final Entity target;

	public EntityKillEntityEvent(Entity what, Entity target) {
		super(what);

		this.target = target;
	}

	public Entity getTarget() {
		return target;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
