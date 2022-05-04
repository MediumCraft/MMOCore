package net.Indyuce.mmocore.api.player.stats;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;

public enum StatType {

    // Vanilla stats
    ATTACK_DAMAGE,
    ATTACK_SPEED,
    MAX_HEALTH,
    HEALTH_REGENERATION,
    MAX_HEALTH_REGENERATION,

    // Misc
    MOVEMENT_SPEED,
    SPEED_MALUS_REDUCTION,
    KNOCKBACK_RESISTANCE,

    // Mana
    MAX_MANA,
    MANA_REGENERATION,
    MAX_MANA_REGENERATION,

    // Stamina
    MAX_STAMINA,
    STAMINA_REGENERATION,
    MAX_STAMINA_REGENERATION,

    // Stellium
    MAX_STELLIUM,
    STELLIUM_REGENERATION,
    MAX_STELLIUM_REGENERATION,

    // Vanilla armor stats
    ARMOR,
    ARMOR_TOUGHNESS,

    // Critical strikes
    CRITICAL_STRIKE_CHANCE,
    CRITICAL_STRIKE_POWER,
    SKILL_CRITICAL_STRIKE_CHANCE,
    SKILL_CRITICAL_STRIKE_POWER,

    // Mitigation
    DEFENSE,
    BLOCK_POWER,
    BLOCK_RATING,
    BLOCK_COOLDOWN_REDUCTION,
    DODGE_RATING,
    DODGE_COOLDOWN_REDUCTION,
    PARRY_RATING,
    PARRY_COOLDOWN_REDUCTION,

    // Utility
    ADDITIONAL_EXPERIENCE,
    COOLDOWN_REDUCTION,
    CHANCE,

    // Damage-type based stats
    MAGIC_DAMAGE,
    PHYSICAL_DAMAGE,
    PROJECTILE_DAMAGE,
    WEAPON_DAMAGE,
    SKILL_DAMAGE,
    UNARMED_DAMAGE,
    UNDEAD_DAMAGE,

    // Misc damage stats
    PVP_DAMAGE,
    PVE_DAMAGE,

    // Damage reduction stats
    DAMAGE_REDUCTION,
    MAGIC_DAMAGE_REDUCTION,
    PHYSICAL_DAMAGE_REDUCTION,
    PROJECTILE_DAMAGE_REDUCTION,
    WEAPON_DAMAGE_REDUCTION,
    SKILL_DAMAGE_REDUCTION,

    /**
     * Reduces amount of tugs needed to fish
     */
    FISHING_STRENGTH("fishing"),

    /**
     * Chance of instant success when fishing
     */
    CRITICAL_FISHING_CHANCE("fishing"),

    /**
     * Chance of crit fishing failure
     */
    CRITICAL_FISHING_FAILURE_CHANCE("fishing"),

    /**
     * Chance of dropping more minerals when mining.
     */
    FORTUNE,

    /**
     * Get haste when mining blocks.
     */
    GATHERING_HASTE,

    /**
     * Chance of getting more crops when farming
     */
    LUCK_OF_THE_FIELD;

    private String profession;

    private LinearValue defaultInfo;
    private DecimalFormat format;

    StatType() {
        // Completely custom stat
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
            stat.format = MythicLib.plugin.getMMOConfig().newDecimalFormat(config.contains("decimal-format." + stat.name()) ? config.getString("decimal-format." + stat.name()) : "0.#");
        }
    }
}
