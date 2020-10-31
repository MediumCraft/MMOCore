package net.Indyuce.mmocore.comp.mythicmobs.skill;

import java.util.Arrays;

import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.SkillResult;

public abstract class PassiveMythicMobSkillHandler implements Listener {
	protected final MythicMobSkill skill;
	
	/**
	 * Core class for all passive types
	 */
	protected PassiveMythicMobSkillHandler(MythicMobSkill skill) {
		this.skill = skill;
	}
	
	public void castSkill(PlayerData data) {
		castSkill(data, data.getPlayer());
	}

	public void castSkill(PlayerData data, Entity target) {
		if (!data.getProfess().hasSkill(skill.getId()))
			return;

		SkillResult cast = data.cast(data.getProfess().getSkill(skill.getId()));
		if (!cast.isSuccessful())
			return;

		data.getSkillData().cacheModifiers(skill.getInternalName(), cast);
		if (MMOCore.plugin.hasAntiCheat())
			MMOCore.plugin.antiCheatSupport.disableAntiCheat(data.getPlayer(), skill.getAntiCheat());
		MythicMobs.inst().getAPIHelper().castSkill(data.getPlayer(), skill.getInternalName(), target, data.getPlayer().getEyeLocation(),
				Arrays.asList(data.getPlayer()), null, 1);
	}
}
