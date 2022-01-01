package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.modifier.ModifierSource;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.skill.trigger.PassiveSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSkill implements CooldownObject {
    private final RegisteredSkill skill;
    private final int unlockLevel, maxSkillLevel;
    private final Map<String, LinearValue> modifiers = new HashMap<>();

    public ClassSkill(RegisteredSkill skill, int unlockLevel) {
        this(skill, unlockLevel, 0);
    }

    /**
     * Class used to save information about skills IN A CLASS CONTEXT i.e at
     * which level the skill can be unlocked, etc.
     */
    public ClassSkill(RegisteredSkill skill, int unlockLevel, int maxSkillLevel) {
        this.skill = skill;
        this.unlockLevel = unlockLevel;
        this.maxSkillLevel = maxSkillLevel;

        for (String mod : skill.getHandler().getModifiers())
            this.modifiers.put(mod, skill.getModifierInfo(mod));
    }

    public ClassSkill(RegisteredSkill skill, ConfigurationSection config) {
        this.skill = skill;
        unlockLevel = config.getInt("level");
        maxSkillLevel = config.getInt("max-level");

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

    /*
     * this method can only OVERRIDE default modifiers
     */
    public void addModifier(String modifier, LinearValue linear) {
        if (modifiers.containsKey(modifier))
            modifiers.put(modifier, linear);
    }

    public double getModifier(String modifier, int level) {
        return modifiers.get(modifier).calculate(level);
    }

    public List<String> calculateLore(PlayerData data) {
        return calculateLore(data, data.getSkillLevel(skill));
    }

    public List<String> calculateLore(PlayerData data, int x) {
        List<String> list = new ArrayList<>();

        Map<String, String> placeholders = calculateModifiers(x);
        placeholders.put("mana_name", data.getProfess().getManaDisplay().getName());
        skill.getLore().forEach(str -> list.add(applyPlaceholders(placeholders, str)));

        return list;
    }

    private String applyPlaceholders(Map<String, String> placeholders, String str) {
        while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
            String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
            str = str.replace("{" + holder + "}", placeholders.getOrDefault(holder, "PHE"));
        }
        return str;
    }

    private Map<String, String> calculateModifiers(int x) {
        Map<String, String> map = new HashMap<>();
        modifiers.keySet().forEach(modifier -> map.put(modifier, modifiers.get(modifier).getDisplay(x)));
        return map;
    }

    private LinearValue readLinearValue(LinearValue current, ConfigurationSection config) {
        return current instanceof IntegerLinearValue ? new IntegerLinearValue(config) : new LinearValue(config);
    }

    public CastableSkill toCastable(PlayerData caster) {
        return new CastableSkill(this, caster.getSkillLevel(getSkill()));
    }

    public PassiveSkill toPassive(PlayerData caster) {
        Validate.isTrue(skill.hasTrigger(), "Skill is active");
        return new PassiveSkill("MMOCorePassiveSkill", skill.getTrigger(), toCastable(caster), EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    public String getCooldownPath() {
        return "skill_" + skill.getHandler().getId();
    }
}