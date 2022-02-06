package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegisteredSkill {
    private final SkillHandler<?> handler;

    private final String name;
    private final Map<String, LinearValue> defaultModifiers = new HashMap<>();
    private final ItemStack icon;
    private final List<String> lore;
    private final TriggerType triggerType;

    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;

        name = Objects.requireNonNull(config.getString("name"), "Could not find skill name");
        icon = MMOCoreUtils.readIcon(Objects.requireNonNull(config.getString("material"), "Could not find skill icon"));
        lore = Objects.requireNonNull(config.getStringList("lore"), "Could not find skill lore");

        // Trigger type
        Validate.isTrue(!getHandler().isTriggerable() || config.contains("passive-type"), "Please provide a passive type");
        Validate.isTrue(getHandler().isTriggerable() || !config.contains("passive-type"), "Cannot change passive type of a default passive skill");
        triggerType = config.contains("passive-type") ? TriggerType.valueOf(config.getString("passive-type").toUpperCase().replace(" ", "_").replace("-", "_")) : null;

        for (String mod : handler.getModifiers())
            defaultModifiers.put(mod, config.contains(mod) ? new LinearValue(config.getConfigurationSection(mod)) : LinearValue.ZERO);
    }

    public RegisteredSkill(SkillHandler<?> handler, String name, ItemStack icon, List<String> lore, @Nullable TriggerType triggerType) {
        this.handler = handler;
        this.name = name;
        this.icon = icon;
        this.lore = lore;
        this.triggerType = triggerType;
    }

    public SkillHandler<?> getHandler() {
        return handler;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemStack getIcon() {
        return icon.clone();
    }

    public boolean hasModifier(String modifier) {
        return defaultModifiers.containsKey(modifier);
    }

    /**
     * There are three types of MMOCore skills:
     * - skills with no trigger type (therefore active)
     * - default passive skills with no trigger type
     * - custom skills with a trigger type (therefore passive)
     * <p>
     * Illegal:
     * - default passive skills with a trigger type
     *
     * @return If the skill should
     */
    public boolean hasTrigger() {
        return triggerType != null;
    }

    /**
     * Two types of passive skills:
     * - custom passive skills, with a trigger type
     * - default passive skills which are untriggerable
     * <p>
     * This option dictates whether or not it
     * can be cast when in casting mode.
     *
     * @return If the given skill is passive
     */
    public boolean isPassive() {
        return triggerType != null || !getHandler().isTriggerable();
    }

    @NotNull
    public TriggerType getTrigger() {
        return Objects.requireNonNull(triggerType, "Skill has no trigger");
    }

    @Nullable
    public TriggerType getTriggerOrNull() {
        return triggerType;
    }

    public void addModifier(String modifier, LinearValue linear) {
        defaultModifiers.put(modifier, linear);
    }

    @Deprecated
    public void addModifierIfNone(String mod, LinearValue defaultValue) {
        if (!hasModifier(mod))
            addModifier(mod, defaultValue);
    }

    public LinearValue getModifierInfo(String modifier) {
        return defaultModifiers.get(modifier);
    }

    public double getModifier(String modifier, int level) {
        return defaultModifiers.get(modifier).calculate(level);
    }
}
