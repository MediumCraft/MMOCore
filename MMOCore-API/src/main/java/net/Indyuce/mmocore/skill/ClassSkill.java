package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClassSkill implements CooldownObject, Unlockable {
    private final RegisteredSkill skill;
    private final int unlockLevel, maxSkillLevel;
    private final boolean unlockedByDefault, permanent, upgradable;
    private final Map<String, LinearValue> parameters = new HashMap<>();

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel) {
        this(skill, unlockLevel, maxSkillLevel, true);
    }

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault) {
        this(skill, unlockLevel, maxSkillLevel, unlockedByDefault, MMOCore.plugin.configManager.passiveSkillsNeedBinding);
    }

    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault, boolean needsBinding) {
        this(skill, unlockLevel, maxSkillLevel, unlockedByDefault, needsBinding, true);
    }

    /**
     * Class used to save information about skills IN A CLASS CONTEXT
     * i.e at which level the skill can be unlocked, etc.
     * <p>
     * This constructor can be used by other plugins to register class
     * skills directly without the use of class config files.
     * <p>
     * It is also used by the MMOCore API to force players to cast abilities.
     */
    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel, boolean unlockedByDefault, boolean needsBinding, boolean upgradable) {
        this.skill = skill;
        this.unlockLevel = unlockLevel;
        this.maxSkillLevel = maxSkillLevel;
        this.unlockedByDefault = unlockedByDefault;
        this.permanent = !needsBinding && skill.getTrigger().isPassive();
        this.upgradable = upgradable;
        for (String param : skill.getHandler().getParameters())
            this.parameters.put(param, skill.getParameterInfo(param));
    }

    public ClassSkill(RegisteredSkill skill, ConfigurationSection config) {
        this.skill = skill;
        unlockLevel = config.getInt("level");
        maxSkillLevel = config.getInt("max-level");
        unlockedByDefault = config.getBoolean("unlocked-by-default", true);
        permanent = !config.getBoolean("needs-bound", MMOCore.plugin.configManager.passiveSkillsNeedBinding) && skill.getTrigger().isPassive();
        upgradable = config.getBoolean("upgradable", true);
        for (String param : skill.getHandler().getParameters()) {
            LinearValue defaultValue = skill.getParameterInfo(param);
            this.parameters.put(param, config.isConfigurationSection(param) ? readLinearValue(defaultValue, config.getConfigurationSection(param)) : defaultValue);
        }
    }

    @NotNull
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

    public boolean isUpgradable() {
        return upgradable;
    }

    @Override
    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    @Deprecated
    public boolean needsBound() {
        return getSkill().getTrigger().isPassive() && !isPermanent();
    }

    /**
     * @return Permanent skills are passive skills which do
     * not have to be bound in order to apply their effects.
     * Permanent skills can only be passive skills.
     */
    public boolean isPermanent() {
        return permanent;
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "skill:" + skill.getHandler().getId().toLowerCase();
    }

    @Override
    public void whenLocked(PlayerData playerData) {

        // Unbind the skill if necessary
        new HashMap<>(playerData.getBoundSkills()).forEach((slot, bound) -> {
            if (this.equals(bound.getClassSkill()))
                playerData.unbindSkill(slot);
        });

        // Update stats to flush permanent skill
        if (isPermanent()) playerData.getStats().updateStats();
    }

    @Override
    public void whenUnlocked(PlayerData playerData) {

        // Update stats to register permanent skill
        if (isPermanent()) playerData.getStats().updateStats();
    }


    /**
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public void addModifier(String modifier, LinearValue linear) {
        addParameter(modifier, linear);
    }

    /**
     * This method can only override default parameters and
     * will throw an error when trying to define non existing modifiers
     */
    public void addParameter(String parameter, LinearValue linear) {
        Validate.isTrue(parameters.containsKey(parameter), "Could not find parameter '" + parameter + "'");
        parameters.put(parameter, linear);
    }

    /**
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public double getModifier(String modifier, int level) {
        return getParameter(modifier, level);
    }

    public double getParameter(String parameter, int level) {
        return Objects.requireNonNull(parameters.get(parameter), "Could not find parameter '" + parameter + "'").calculate(level);
    }

    public List<String> calculateLore(PlayerData data) {
        return calculateLore(data, data.getSkillLevel(skill));
    }

    public List<String> calculateLore(PlayerData data, int x) {

        // Calculate placeholders
        Placeholders placeholders = new Placeholders();
        parameters.keySet()
                .forEach(param -> placeholders.register(param, skill.getDecimalFormat(param).format(data
                        .getMMOPlayerData()
                        .getSkillModifierMap()
                        .calculateValue(skill.getHandler(), parameters.get(param).calculate(x), param)))
                );
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
        return new CastableSkill(this, caster);
    }

    /**
     * Be careful, this method creates a new UUID each time it
     * is called. It needs to be saved somewhere when trying to
     * unregister the passive skill from the skill map later on.
     */
    @NotNull
    public PassiveSkill toPassive(PlayerData caster) {
        Validate.isTrue(skill.getTrigger().isPassive(), "Skill is active");
        return new PassiveSkill("MMOCore" + (permanent ? "Permanent" : "Passive") + "Skill", toCastable(caster), EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    @Override
    public String getCooldownPath() {
        return "skill_" + skill.getHandler().getId();
    }
}