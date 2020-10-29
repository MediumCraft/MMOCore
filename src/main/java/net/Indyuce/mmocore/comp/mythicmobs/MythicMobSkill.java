package net.Indyuce.mmocore.comp.mythicmobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import io.lumine.xikage.mythicmobs.MythicMobs;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.SkillResult;
import net.Indyuce.mmocore.api.skill.SkillResult.CancelReason;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.comp.anticheat.CheatType;
import net.mmogroup.mmolib.api.event.PlayerAttackEvent;

public class MythicMobSkill extends Skill {
	private final io.lumine.xikage.mythicmobs.skills.Skill skill;
	private final Map<CheatType, Integer> antiCheat = new HashMap<>();

	// private final BiFunction<PlayerDataManager, SkillInfo, SkillResult> cast;

	public MythicMobSkill(String id, FileConfiguration config) {
		super(id);

		String mmId = config.getString("mythicmobs-skill-id");
		Validate.notNull(mmId, "Could not find MM skill ID");

		java.util.Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(mmId);
		Validate.isTrue(opt.isPresent(), "Could not find MM skill " + mmId);
		skill = opt.get();

		String format = config.getString("material");
		Validate.notNull(format, "Could not load skill material");
		setIcon(MMOCoreUtils.readIcon(format));

		setName(config.getString("name"));
		setLore(config.getStringList("lore"));
		
		for (String key : config.getKeys(false)) {
			Object mod = config.get(key);
			if (mod instanceof ConfigurationSection)
				addModifier(key, readLinearValue((ConfigurationSection) mod));
		}

		if (config.isConfigurationSection("disable-anti-cheat"))
			for(String key : config.getConfigurationSection("").getKeys(false)) {
				Optional<CheatType> cheatType = Enums.getIfPresent(CheatType.class, key.toUpperCase());
				if(cheatType.isPresent() && config.isInt("disable-anti-cheat." + key))
					antiCheat.put(cheatType.get(), config.getInt("disable-anti-cheat." + key));
				else MMOCore.log(Level.WARNING, "Invalid Anti-Cheat configuration for '" + id + "'!");
			}


		if(config.isString("passive-type")) {
			Optional<PassiveSkillType> passiveType = Enums.getIfPresent(PassiveSkillType.class, config.getString("passive-type").toUpperCase());
			Validate.isTrue(passiveType.isPresent(), "Invalid 'passive-type' for MM skill: " + id);
			setPassive();
			Bukkit.getPluginManager().registerEvents(getListener(passiveType.get()), MMOCore.plugin);
		}

		// cast = config.getBoolean("target") ? (data, info) -> new
		// TargetSkillResult(data, info, def(config.getDouble("range"), 50)) :
		// (data, info) -> new SkillResult(data, info);
	}

	// private double def(double d, double def) {
	// return d == 0 ? def : d;
	// }

	private MythicMobSkillPassive getListener(PassiveSkillType type) {
		switch(type) {
			case PLAYER_ATTACK:
				return new PlayerAttackPassive();
			case PLAYER_DAMAGE:
				return new PlayerDamagePassive();
		}
		MMOCore.log(Level.SEVERE, "Something went wrong adding a passive skill! (" + getInternalName() + ")");
		return null;
	}
	
	public String getInternalName() {
		return skill.getInternalName();
	}

	@Override
	public SkillResult whenCast(PlayerData data, SkillInfo skill) {
		SkillResult cast = new SkillResult(data, skill);
		if (!cast.isSuccessful() || !data.isOnline())
			return cast;
		if(isPassive()) return cast;

		List<Entity> targets = new ArrayList<>();
		// targets.add(cast instanceof TargetSkillResult ? ((TargetSkillResult)
		// cast).getTarget() : stats.getPlayer());
		targets.add(data.getPlayer());

		/*
		 * cache placeholders so they can be retrieved later by MythicMobs math formulas
		 */
		data.getSkillData().cacheModifiers(this, cast);
		if(MMOCore.plugin.hasAntiCheat()) MMOCore.plugin.antiCheatSupport.disableAntiCheat(data.getPlayer(), antiCheat);
		if (!MythicMobs.inst().getAPIHelper().castSkill(data.getPlayer(), this.skill.getInternalName(),
				data.getPlayer(), data.getPlayer().getEyeLocation(), targets, null, 1))
			cast.abort(CancelReason.OTHER);

		return cast;
	}
	
	/*
	 * used to load double modifiers from the config with a specific type, since
	 * modifiers have initially a type for mmocore default skills
	 */
	private LinearValue readLinearValue(ConfigurationSection section) {
		return section.getBoolean("int") ? new IntegerLinearValue(section) : new LinearValue(section);
	}
	
	protected abstract class MythicMobSkillPassive implements Listener {		
		protected void castSkill(PlayerData data) {
			if (!data.getProfess().hasSkill(getId()))
				return;

			SkillResult cast = data.cast(data.getProfess().getSkill(getId()));
			if (!cast.isSuccessful())
				return;
			
			data.getSkillData().cacheModifiers(getInternalName(), cast);
			if(MMOCore.plugin.hasAntiCheat()) MMOCore.plugin.antiCheatSupport.disableAntiCheat(data.getPlayer(), antiCheat);
			List<Entity> targets = new ArrayList<>();
			targets.add(data.getPlayer());
			MythicMobs.inst().getAPIHelper().castSkill(data.getPlayer(), getInternalName(), data.getPlayer(), data.getPlayer().getEyeLocation(), targets, null, 1);
		}
	}
	
	protected class PlayerAttackPassive extends MythicMobSkillPassive {
		@EventHandler
		private void playerAttack(PlayerAttackEvent event) {
			castSkill(event.getData().getMMOCore());
		}
	}
	
	protected class PlayerDamagePassive extends MythicMobSkillPassive {
		@EventHandler
		private void playerDamage(EntityDamageEvent event) {
			if(event.getEntityType() != EntityType.PLAYER)
				return;
			castSkill(PlayerData.get((Player) event.getEntity()));
		}
	}
}
