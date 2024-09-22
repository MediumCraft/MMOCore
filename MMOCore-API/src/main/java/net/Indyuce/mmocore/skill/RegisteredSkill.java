package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.formula.BooleanExpression;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.util.Icon;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;

public class RegisteredSkill {
    private final SkillHandler<?> handler;
    private final String name;
    private final Map<String, LinearValue> defaultParameters = new HashMap<>();

    private final Map<String, DecimalFormat> parameterDecimalFormats = new HashMap<>();

    private final ItemStack icon;
    private final List<String> lore;
    private final List<String> categories;
    private final TriggerType triggerType;

    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;

        name = Objects.requireNonNull(config.getString("name"), "Could not find skill name");
        icon = Icon.from(config.get("material")).toItem();
        lore = Objects.requireNonNull(config.getStringList("lore"), "Could not find skill lore");

        // Trigger type
        triggerType = getHandler().isTriggerable() ? (config.contains("passive-type") ? TriggerType.valueOf(UtilityMethods.enumName(config.getString("passive-type"))) : TriggerType.CAST) : TriggerType.API;

        // Categories
        categories = config.getStringList("categories");
        categories.add(getHandler().getId());
        if (triggerType.isPassive())
            categories.add("PASSIVE");
        else
            categories.add("ACTIVE");


        // Load default modifier formulas
        for (String param : handler.getParameters()) {
            if (config.contains(param + ".decimal-format"))
                parameterDecimalFormats.put(param, new DecimalFormat(config.getString(param + ".decimal-format")));
            defaultParameters.put(param, config.contains(param) ? new LinearValue(config.getConfigurationSection(param)) : LinearValue.ZERO);

        }

        /*
         * This is so that SkillAPI skill level matches the MMOCore skill level
         * https://gitlab.com/phoenix-dvpmt/mmocore/-/issues/531
         */
        defaultParameters.put("level", new IntegerLinearValue(0, 1));
    }

    public RegisteredSkill(SkillHandler<?> handler, String name, ItemStack icon, List<String> lore, @Nullable TriggerType triggerType) {
        this.handler = handler;
        this.name = name;
        this.icon = icon;
        this.lore = lore;
        this.triggerType = triggerType;
        this.categories = new ArrayList<>();
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

    public List<String> getCategories() {
        return categories;
    }

    public ItemStack getIcon() {
        return icon.clone();
    }

    public boolean hasParameter(String parameter) {
        return defaultParameters.containsKey(parameter);
    }

    /**
     * Skills modifiers are now called parameters.
     */
    @Deprecated
    public boolean hasModifier(String modifier) {
        return defaultParameters.containsKey(modifier);
    }

    @NotNull
    public TriggerType getTrigger() {
        return Objects.requireNonNull(triggerType, "Skill has no trigger");
    }

    /**
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public void addModifier(String modifier, LinearValue linear) {
        defaultParameters.put(modifier, linear);
    }

    public void addParameter(String parameter, LinearValue linear) {
        defaultParameters.put(parameter, linear);
    }

    public DecimalFormat getDecimalFormat(String parameter) {
        return parameterDecimalFormats.getOrDefault(parameter, MythicLib.plugin.getMMOConfig().decimal);
    }


    @Deprecated
    public void addModifierIfNone(String mod, LinearValue defaultValue) {
        if (!hasParameter(mod))
            addParameter(mod, defaultValue);
    }


    /**
     * Skill modifiers are now called parameters.
     */
    @Deprecated
    public LinearValue getModifierInfo(String modifier) {
        return defaultParameters.get(modifier);
    }

    /**
     * @return Modifier formula.
     *         Not null as long as the modifier is well defined
     */
    @NotNull
    public LinearValue getParameterInfo(String parameter) {
        return defaultParameters.get(parameter);
    }

    public double getModifier(String modifier, int level) {
        return defaultParameters.get(modifier).calculate(level);
    }

    public boolean matchesFormula(String formula) {
        String parsedExpression = formula;
        for (String category : categories)
            parsedExpression = parsedExpression.replace("<" + category + ">", "true");
        parsedExpression = parsedExpression.replaceAll("<.*?>", "false");
        return BooleanExpression.eval(parsedExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredSkill that = (RegisteredSkill) o;
        return handler.equals(that.handler) && triggerType.equals(that.triggerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, triggerType);
    }
}
