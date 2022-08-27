package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.script.condition.Condition;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ClassSkill implements CooldownObject {
    private final RegisteredSkill skill;
    private final int unlockLevel, maxSkillLevel;
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

    public List<String> calculateLore(PlayerData data) {
        return calculateLore(data, data.getSkillLevel(skill));
    }

    public List<String> calculateLore(PlayerData data, int x) {
        List<String> list = new ArrayList<>();

        Map<String, String> placeholders = calculateModifiers(x);
        placeholders.put("mana_name", data.getProfess().getManaDisplay().getName());
        placeholders.put("mana_color", data.getProfess().getManaDisplay().getFull().toString());
        skill.getLore().forEach(str -> list.add(applyPlaceholders(placeholders, str)));

        return list;
    }

    private String applyPlaceholders(Map<String, String> placeholders, String str) {
        String explored = str;
        while (explored.contains("{") && explored.substring(explored.indexOf("{")).contains("}")) {
            final int begin = explored.indexOf("{"), end = explored.indexOf("}");
            String holder = explored.substring(begin + 1, end);

            if (placeholders.containsKey(holder))
                str = str.replace("{" + holder + "}", placeholders.get(holder));

            // Increase counter
            explored = explored.substring(end + 1);
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
        Validate.isTrue(skill.getTrigger().isPassive(), "Skill is active");
        return new PassiveSkill("MMOCorePassiveSkill", toCastable(caster), EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    @Override
    public String getCooldownPath() {
        return "skill_" + skill.getHandler().getId();
    }
}