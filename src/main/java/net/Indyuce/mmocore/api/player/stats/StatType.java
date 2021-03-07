package net.Indyuce.mmocore.api.player.stats;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;

public enum StatType {

	ATTACK_DAMAGE,
	ATTACK_SPEED,
	MAX_HEALTH,
	HEALTH_REGENERATION,

	MOVEMENT_SPEED,
	SPEED_MALUS_REDUCTION,
	KNOCKBACK_RESISTANCE,

	MAX_MANA,
	MAX_STAMINA,
	MAX_STELLIUM,
	MANA_REGENERATION,
	STAMINA_REGENERATION,
	STELLIUM_REGENERATION,

	ARMOR,
	ARMOR_TOUGHNESS,

	CRITICAL_STRIKE_CHANCE,
	CRITICAL_STRIKE_POWER,
	SKILL_CRITICAL_STRIKE_CHANCE,
	SKILL_CRITICAL_STRIKE_POWER,

	BLOCK_POWER,
	BLOCK_RATING,
	BLOCK_COOLDOWN_REDUCTION,
	DODGE_RATING,
	DODGE_COOLDOWN_REDUCTION,
	PARRY_RATING,
	PARRY_COOLDOWN_REDUCTION,

	ADDITIONAL_EXPERIENCE,
	COOLDOWN_REDUCTION,

	MAGIC_DAMAGE,
	PHYSICAL_DAMAGE,
	PROJECTILE_DAMAGE,
	WEAPON_DAMAGE,
	SKILL_DAMAGE,
	UNDEAD_DAMAGE,

	PVP_DAMAGE,
	PVE_DAMAGE,

	DEFENSE,
	DAMAGE_REDUCTION,
	MAGIC_DAMAGE_REDUCTION,
	PHYSICAL_DAMAGE_REDUCTION,
	PROJECTILE_DAMAGE_REDUCTION,
	WEAPON_DAMAGE_REDUCTION,
	SKILL_DAMAGE_REDUCTION,

	// reduces amount of tugs needed to fish
	FISHING_STRENGTH("fishing"),

	// chance of instant success when fishing
	CRITICAL_FISHING_CHANCE("fishing"),

	// chance of crit fishing failure
	CRITICAL_FISHING_FAILURE_CHANCE("fishing"),

	// chance of dropping more minerals when mining.
	FORTUNE,

	// get haste when mining blocks.
	GATHERING_HASTE,

	// chance of getting more crops when farming
	LUCK_OF_THE_FIELD;

	private String profession;

	private LinearValue defaultInfo;
	private DecimalFormat format;

	StatType() {
		// completely custom stat.
	}

	@SuppressWarnings("SameParameterValue")
	StatType(String profession) {
		this.profession = profession;
	}

	public String getProfession() {
		return profession;
	}

	public Profession findProfession() {
		return MMOCore.plugin.professionManager.get(profession);
	}

	public boolean hasProfession() {
		return profession != null;
	}

	public LinearValue getDefault() {
		return defaultInfo;
	}

	public boolean matches(Profession profession) {
		return this.profession != null && this.profession.equals(profession.getId());
	}

	public String format(double value) {
		return format.format(value);
	}

	public static void load() {
		FileConfiguration config = new ConfigFile("stats").getConfig();
		for (StatType stat : values()) {
			stat.defaultInfo = config.contains("default." + stat.name()) ? new LinearValue(config.getConfigurationSection("default." + stat.name())) : new LinearValue(0, 0);
			stat.format = MythicLib.plugin.getMMOConfig().newFormat(config.contains("decimal-format." + stat.name()) ? config.getString("decimal-format." + stat.name()) : "0.#");
		}
	}
}
