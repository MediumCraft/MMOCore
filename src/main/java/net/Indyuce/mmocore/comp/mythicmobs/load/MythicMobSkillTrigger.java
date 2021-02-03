package net.Indyuce.mmocore.comp.mythicmobs.load;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.skills.Skill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class MythicMobSkillTrigger extends Trigger {
	private final Skill skill;

	public MythicMobSkillTrigger(MMOLineConfig config) {
		super(config);

		config.validate("id");
		String id = config.getString("id");
		Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(id);
		Validate.isTrue(opt.isPresent(), "Could not find MM skill " + id);
		skill = opt.get();
	}

	@Override
	public void apply(PlayerData player) {
		if(!player.isOnline()) return;
		List<Entity> targets = new ArrayList<>();
		targets.add(player.getPlayer());
		MythicMobs.inst().getAPIHelper().castSkill(player.getPlayer(), this.skill.getInternalName(), player.getPlayer(), player.getPlayer().getEyeLocation(), targets, null, 1);
	}
}
