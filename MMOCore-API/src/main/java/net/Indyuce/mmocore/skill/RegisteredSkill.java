package net.Indyuce.mmocore.skill;

import bsh.EvalError;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.Unlockable;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.api.util.math.formula.IntegerLinearValue;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegisteredSkill implements Unlockable {
    private final SkillHandler<?> handler;
    private final String name;
    private final Map<String, LinearValue> defaultModifiers = new HashMap<>();
    private final ItemStack icon;
    private final List<String> lore;
    private final List<String> categories;
    @NotNull
    private final TriggerType triggerType;

    public RegisteredSkill(SkillHandler<?> handler, ConfigurationSection config) {
        this.handler = handler;

        name = Objects.requireNonNull(config.getString("name"), "Could not find skill name");
        icon = MMOCoreUtils.readIcon(Objects.requireNonNull(config.getString("material"), "Could not find skill icon"));
        lore = Objects.requireNonNull(config.getStringList("lore"), "Could not find skill lore");
        categories = config.getStringList("categories");
        // Trigger type
        triggerType = getHandler().isTriggerable() ? (config.contains("passive-type") ? TriggerType.valueOf(UtilityMethods.enumName(config.getString("passive-type"))) : TriggerType.CAST) : TriggerType.API;
        categories.add(getHandler().getId());
        if (triggerType.isPassive())
            categories.add("PASSIVE");
        else
            categories.add("ACTIVE");

        // Load default modifier formulas
        for (String mod : handler.getModifiers())
            defaultModifiers.put(mod, config.contains(mod) ? new LinearValue(config.getConfigurationSection(mod)) : LinearValue.ZERO);

        /*
         * This is so that SkillAPI skill level matches the MMOCore skill level
         * https://gitlab.com/phoenix-dvpmt/mmocore/-/issues/531
         */
        defaultModifiers.put("level", new IntegerLinearValue(0, 1));
    }

    public RegisteredSkill(SkillHandler<?> handler, String name, ItemStack icon, List<String> lore, @Nullable TriggerType triggerType) {
        this.handler = handler;
        this.name = name;
        this.icon = icon;
        this.lore = lore;
        this.triggerType = triggerType;
        this.categories = new ArrayList<>();
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "skill:" + handler.getId().toLowerCase();
    }

    @Override
    public void whenLocked(PlayerData playerData) {
        playerData.mapBoundSkills()
                .forEach((slot, skill) ->
                {
                    if (skill.equals(getUnlockNamespacedKey().split(":")[1]))
                        playerData.unbindSkill(slot);
                });
    }

    @Override
    public void whenUnlocked(PlayerData playerData) {

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

    public boolean hasModifier(String modifier) {
        return defaultModifiers.containsKey(modifier);
    }

    @NotNull
    public TriggerType getTrigger() {
        return Objects.requireNonNull(triggerType, "Skill has no trigger");
    }

    public void addModifier(String modifier, LinearValue linear) {
        defaultModifiers.put(modifier, linear);
    }

    @Deprecated
    public void addModifierIfNone(String mod, LinearValue defaultValue) {
        if (!hasModifier(mod))
            addModifier(mod, defaultValue);
    }

    /**
     * @return Modifier formula.
     * Not null as long as the modifier is well defined
     */
    @NotNull
    public LinearValue getModifierInfo(String modifier) {
        return defaultModifiers.get(modifier);
    }

    public double getModifier(String modifier, int level) {
        return defaultModifiers.get(modifier).calculate(level);
    }

    public boolean matchesFormula(String formula) {
        String parsedExpression = formula;
        for (String category : categories)
            parsedExpression = parsedExpression.replace("<" + category + ">", "true");
        parsedExpression = parsedExpression.replaceAll("<.*?>", "false");
        try {
            boolean res = (boolean) MythicLib.plugin.getInterpreter().eval(parsedExpression);
            return res;
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
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
