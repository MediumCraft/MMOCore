package net.Indyuce.mmocore.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo.DamageType;

public class PlayerAttackEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final EntityDamageByEntityEvent event;
	private final DamageInfo info;

	public PlayerAttackEvent(PlayerData data, EntityDamageByEntityEvent event, DamageInfo info) {
		super(data);

		this.event = event;
		this.info = info;
	}

	@Override
	public boolean isCancelled() {
		return event.isCancelled();
	}

	@Override
	public void setCancelled(boolean value) {
		event.setCancelled(value);
	}

	public DamageInfo getDamageInfo() {
		return info;
	}

	// @Deprecated
	public boolean isWeapon() {
		return info.getTypes().contains(DamageType.WEAPON);
	}

	public Entity getEntity() {
		return event.getEntity();
	}

	public double getDamage() {
		return event.getDamage();
	}

	public void setDamage(double value) {
		event.setDamage(value);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
