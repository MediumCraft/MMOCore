package net.Indyuce.mmocore.api.player.stats;

import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.stats.StatInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Deprecated
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
    FISHING_STRENGTH,

    /**
     * Chance of instant success when fishing
     */
    CRITICAL_FISHING_CHANCE,

    /**
     * Chance of crit fishing failure
     */
    CRITICAL_FISHING_FAILURE_CHANCE,

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

    @Deprecated
    public String getProfession() {
        return findProfession().getId();
    }

    @Deprecated
    @Nullable
    public Profession findProfession() {
        return StatInfo.valueOf(name()).profession;
    }

    @Deprecated
    public boolean hasProfession() {
        return findProfession() != null;
    }

    @Deprecated
    @NotNull
    public LinearValue getDefault() {
        return StatInfo.valueOf(name()).getDefaultFormula();
    }

    @Deprecated
    public boolean matches(Profession profession) {
        return Objects.equals(findProfession(), profession);
    }

    @Deprecated
    public String format(double value) {
        return StatInfo.valueOf(name()).format(value);
    }
}
