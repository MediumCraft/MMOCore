package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.script.condition.Condition;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ClassSkill implements CooldownObject {
    private final RegisteredSkill skill;
    private final int unlockLevel, maxSkillLevel;
    private final boolean unlockedByDefault;
    private final Map<String, LinearValue> modifiers = new HashMap<>();

    @Deprecated
    private final Set<Condition> unlockConditions = new HashSet<>();

    /**
     * Class used to save information about skills IN A CLASS CONTEXT i.e at
     * which level the skill can be unlocked, etc.
     * <p>
     * This constructor can be used by other plugins to register class skills
     * directly without the use of class config files.
     * <p>
     * It is also used by the MMOCore API to force players to cast abilities.
     */
    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel) {
        this(skill, unlockLevel, maxSkillLevel, true);
    }

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault) {
        this.skill = skill;
        this.unlockLevel = unlockLevel;
        this.maxSkillLevel = maxSkillLevel;
        this.unlockedByDefault = unlockedByDefault;
        for (String mod : skill.getHandler().getModifiers())
            this.modifiers.put(mod, skill.getModifierInfo(mod));
    }

    public ClassSkill(RegisteredSkill skill, ConfigurationSection config) {
        this.skill = skill;
        unlockLevel = config.getInt("level");
        maxSkillLevel = config.getInt("max-level");
        unlockedByDefault = config.getBoolean("unlocked-by-default", true);
        for (String mod : skill.getHandler().getModifiers()) {
            LinearValue defaultValue = skill.getModifierInfo(mod);
            this.modifiers.put(mod, config.isConfigurationSection(mod) ? readLinearValue(defaultValue, config.getConfigurationSection(mod)) : defaultValue);
        }
    }


    public RegisteredSkill getSkill() {
        return skill;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public boolean hasMaxLevel() {
        return maxSkillLevel > 0;
    }

    public int getMaxLevel() {
        return maxSkillLevel;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    /**
     * This method can only override default modifiers and
     * will throw an error when trying to define non existing modifiers
     */
    public void addModifier(String modifier, LinearValue linear) {
        Validate.isTrue(modifiers.containsKey(modifier), "Could not find modifier '" + modifier + "'");
        modifiers.put(modifier, linear);
    }

    public double getModifier(String modifier, int level) {
        return Objects.requireNonNull(modifiers.get(modifier), "Could not find modifier '" + modifier + "'").calculate(level);
    }

    /**
     * Gives the delay to launch the skill
     *
     * @return
     */
    public int getDelay(PlayerData data) {
        return modifiers.containsKey("delay") ? (int) modifiers.get("delay").calculate(data.getSkillLevel(getSkill())) : 0;
    }

    public List<String> calculateLore(PlayerData data) {
        return calculateLore(data, data.getSkillLevel(skill));
    }

    public List<String> calculateLore(PlayerData data, int x) {

        // Calculate placeholders
        Placeholders placeholders = new Placeholders();
        modifiers.keySet().forEach(modifier -> placeholders.register(modifier, modifiers.get(modifier).getDisplay(x)));
        placeholders.register("mana_name", data.getProfess().getManaDisplay().getName());
        placeholders.register("mana_color", data.getProfess().getManaDisplay().getFull().toString());

        // Build string arraylist
        List<String> list = new ArrayList<>();
        skill.getLore().forEach(str -> list.add(placeholders.apply(data.getPlayer(), str)));

        return list;
    }

    private LinearValue readLinearValue(LinearValue current, ConfigurationSection config) {
        return current instanceof IntegerLinearValue ? new IntegerLinearValue(config) : new LinearValue(config);
    }

    public CastableSkill toCastable(PlayerData caster) {
        return new CastableSkill(this, caster.getSkillLevel(getSkill()));
    }


    /**
     * Be careful, this method creates a new UUID each time it
     * is called. It needs to be saved somewhere when trying to
     * unregister the passive skill from the skill map later on.
     */
    public PassiveSkill toPassive(PlayerData caster) {
        Validate.isTrue(skill.getTrigger().isPassive(), "Skill is active");
        return new PassiveSkill("MMOCorePassiveSkill", toCastable(caster), EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    @Override
    public String getCooldownPath() {
        return "skill_" + skill.getHandler().getId();
    }
}