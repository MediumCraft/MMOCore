package net.Indyuce.mmocore.comp.mythicmobs.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;

public class PlayerAttackSkillHandler implements Listener {
	private final MythicMobSkill skill;

	/**
	 * Used to handle passive skills which trigger when a player attacks another
	 * entity
	 * 
	 * @param skill
	 *            Passive skill info
	 */
	public PlayerAttackSkillHandler(MythicMobSkill skill) {
		this.skill = skill;
	}

	@EventHandler
	private void playerAttack(PlayerAttackEvent event) {
		castSkill(event.getData().getMMOCore(), event.getEntity());
	}

	/**
	 * Has an extra parameter compared to PlayerDamageSkillHandler because the
	 * hit entity is added to the MythicMobs target list
	 * 
	 * @param data
	 *            Player casting the skill
	 * @param target
	 *            Entity hit
	 */
	public void castSkill(PlayerData data, LivingEntity target) {
		if (!data.getProfess().hasSkill(skill.getId()))
			return;

		SkillResult cast = data.cast(data.getProfess().getSkill(skill.getId()));
		if (!cast.isSuccessful())
			return;

		data.getSkillData().cacheModifiers(skill.getInternalName(), cast);
		if (MMOCore.plugin.hasAntiCheat())
			MMOCore.plugin.antiCheatSupport.disableAntiCheat(data.getPlayer(), skill.getAntiCheat());
		List<Entity> targets = new ArrayList<>();
		targets.add(data.getPlayer());
		targets.add(target);
		MythicMobs.inst().getAPIHelper().castSkill(data.getPlayer(), skill.getInternalName(), data.getPlayer(), data.getPlayer().getEyeLocation(),
				targets, null, 1);
	}
}