package net.Indyuce.mmocore.skill.binding;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Unlockable;
import net.Indyuce.mmocore.api.quest.trigger.SkillModifierTrigger;
import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SkillSlot implements Unlockable {
    private final int slot, modelData;
    private final String formula;
    private final String name;
    private final List<String> lore;

    private final boolean isUnlockedByDefault;
    private final boolean canManuallyBind;

    private final List<SkillModifierTrigger> skillModifierTriggers = new ArrayList<>();
    private final Material item;

    public SkillSlot(int slot, int modelData, String formula, String name, List<String> lore, boolean isUnlockedByDefault, boolean canManuallyBind, List<SkillModifierTrigger> skillModifierTriggers) {
        this.slot = slot;
        this.modelData = modelData;
        this.formula = formula;
        this.name = name;
        this.lore = lore;
        this.item = null;
        this.canManuallyBind = canManuallyBind;
        this.isUnlockedByDefault = isUnlockedByDefault;
        this.skillModifierTriggers.addAll(skillModifierTriggers);
    }

    public SkillSlot(ConfigurationSection section) {
        this.slot = Integer.parseInt(section.getName());
        this.formula = section.contains("formula") ? section.getString("formula") : "true";
        this.name = section.getString("name");
        this.lore = section.getStringList("lore");
        this.item = section.contains("item") ? Material.valueOf(section.getString("item")) : null;
        this.modelData = section.getInt("model-data", 0);
        this.isUnlockedByDefault = section.getBoolean("unlocked-by-default", true);
        this.canManuallyBind = section.getBoolean("can-manually-bind", true);
        if (section.contains("skill-buffs"))
            for (String skillBuff : section.getStringList("skill-buffs"))
                if (skillBuff.startsWith("skill_buff"))
                    skillModifierTriggers.add((SkillModifierTrigger) MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(skillBuff)));
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    @Nullable
    public Material getItem() {
        return item;
    }

    public boolean hasItem() {
        return item != null;
    }

    public int getModelData() {
        return modelData;
    }

    public boolean isUnlockedByDefault() {
        return isUnlockedByDefault;
    }

    public List<SkillModifierTrigger> getSkillModifierTriggers() {
        return skillModifierTriggers;
    }

    public boolean canManuallyBind() {
        return canManuallyBind;
    }

    public boolean acceptsSkill(ClassSkill classSkill) {
        return classSkill.getSkill().matchesFormula(formula);
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "slot:" + slot;
    }

    /**
     * If we lock a slot that had a skill bound
     * to it we first unbind the attached skill.
     */
    @Override
    public void whenLocked(PlayerData playerData) {
        if (playerData.hasSkillBound(slot))
            playerData.unbindSkill(slot);
    }

    @Override
    public void whenUnlocked(PlayerData playerData) {

    }
}
