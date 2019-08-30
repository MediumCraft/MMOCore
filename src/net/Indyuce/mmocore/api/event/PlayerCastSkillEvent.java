package net.Indyuce.mmocore.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;

public class PlayerCastSkillEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final SkillInfo skill;

	private boolean cancelled;

	public PlayerCastSkillEvent(PlayerData playerData, SkillInfo skill) {
		super(playerData);
		
		this.skill = skill;
	}

	public SkillInfo getCast() {
		return skill;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
