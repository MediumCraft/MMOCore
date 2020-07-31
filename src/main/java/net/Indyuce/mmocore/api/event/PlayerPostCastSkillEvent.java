package net.Indyuce.mmocore.api.event;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.api.skill.SkillResult;

public class PlayerPostCastSkillEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	private final SkillInfo skill;
	private final SkillResult result;
	private final boolean successful;

	public PlayerPostCastSkillEvent(PlayerData playerData, SkillInfo skill, SkillResult result, boolean successful) {
		super(playerData);
		
		this.skill = skill;
		this.result = result;
		this.successful = successful;
	}

	public SkillInfo getCast() {
		return skill;
	}

	public SkillResult getResult() {
		return result;
	}
	
	public boolean wasSuccessful() {
		return successful;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
