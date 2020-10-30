package net.Indyuce.mmocore.comp.mythicmobs.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import io.lumine.xikage.mythicmobs.MythicMobs;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.SkillResult;

public class PlayerDamageSkillHandler implements Listener {
	private final MythicMobSkill skill;

	/**
	 * Used to handle passive skills which trigger when a player is attacked by
	 * another entity
	 * 
	 * @param skill
	 *            Passive skill info
	 */
	public PlayerDamageSkillHandler(MythicMobSkill skill) {
		this.skill = skill;
	}

	@EventHandler
	private void playerDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER)
			castSkill(PlayerData.get((Player) event.getEntity()));
	}

	public void castSkill(PlayerData data) {
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
		MythicMobs.inst().getAPIHelper().castSkill(data.getPlayer(), skill.getInternalName(), data.getPlayer(), data.getPlayer().getEyeLocation(),
				targets, null, 1);
	}
}
