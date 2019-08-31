package net.Indyuce.mmocore.api.player.stats;

import java.text.DecimalFormat;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.math.formula.LinearValue;
import net.Indyuce.mmocore.api.player.stats.stat.AttributeStat;
import net.Indyuce.mmocore.api.player.stats.stat.MovementSpeedStat;
import net.Indyuce.mmocore.api.player.stats.stat.PlayerStat;

public enum StatType {

	ATTACK_DAMAGE(Attribute.GENERIC_ATTACK_DAMAGE),
	ATTACK_SPEED(Attribute.GENERIC_ATTACK_SPEED),
	MAX_HEALTH(Attribute.GENERIC_MAX_HEALTH),
	HEALTH_REGENERATION,

	MOVEMENT_SPEED(new MovementSpeedStat()),
	SPEED_MALUS_REDUCTION(new MovementSpeedStat()),
	KNOCKBACK_RESISTANCE(Attribute.GENERIC_KNOCKBACK_RESISTANCE),

	MAX_MANA,
	MAX_STAMINA,
	MAX_STELLIUM,
	MANA_REGENERATION,
	STAMINA_REGENERATION,
	STELLIUM_REGENERATION,

	ARMOR(Attribute.GENERIC_ARMOR),
	ARMOR_TOUGHNESS(Attribute.GENERIC_ARMOR_TOUGHNESS),

	ADDITIONAL_EXPERIENCE,
	COOLDOWN_REDUCTION,

	MAGICAL_DAMAGE,
	PHYSICAL_DAMAGE,
	PROJECTILE_DAMAGE,
	WEAPON_DAMAGE,
	SKILL_DAMAGE,

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
	LUCK_OF_THE_FIELD,

	;

	private String profession;
	private PlayerStat handler;

	private LinearValue defaultInfo;
	private DecimalFormat format;

	private StatType() {
		// completely custom stat.
	}

	private StatType(PlayerStat handler) {
		this.handler = handler;
	}

	private StatType(Attribute attribute) {
		this.handler = new AttributeStat(attribute);
	}

	private StatType(String profession) {
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

	public boolean hasHandler() {
		return handler != null;
	}

	public PlayerStat getHandler() {
		return handler;
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
			stat.format = new DecimalFormat(config.contains("decimal-format." + stat.name()) ? config.getString("decimal-format." + stat.name()) : "0.#");
		}
	}
}
