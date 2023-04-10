package net.Indyuce.mmocore.api.player.profess.skillbinding;

import net.Indyuce.mmocore.skill.ClassSkill;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.List;

public class SkillSlot {
    private final int slot, modelData;
    private final String formula;
    private final String name;
    private final List<String> lore;
    private final Material item;

    public SkillSlot(int slot, int modelData, String formula, String name, List<String> lore, Material item) {
        this.slot = slot;
        this.modelData = modelData;
        this.formula = formula;
        this.name = name;
        this.lore = lore;
        this.item = item;
    }

    public SkillSlot(ConfigurationSection section) {
        this.slot = Integer.parseInt(section.getName());
        this.formula = section.contains("expression") ? section.getString("expression") : "true";
        this.name = section.getString("name");
        this.lore = section.getStringList("lore");
        this.item = section.contains("item") ? Material.valueOf(section.getString("item")) : null;
        this.modelData = section.getInt("model-data", 0);
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

    public boolean acceptsSkill(ClassSkill classSkill) {
        return classSkill.getSkill().matchesFormula(formula);
    }
}
