package net.Indyuce.mmocore.util.item;

import io.lumine.mythic.lib.UtilityMethods;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ConfigItem {
    private final String name, id, texture;
    private final Material material;
    private final List<String> lore;
    private final int damage, modeldata;
    private final boolean unbreakable;

    public ConfigItem(ConfigurationSection config) {
        id = UtilityMethods.enumName(config.getName());
        name = config.getString("name");
        lore = config.getStringList("lore");
        String itemFormat = Objects.requireNonNull(config.getString("item"), "Could not find item material");
        Validate.isTrue(!itemFormat.contains(":"), "Invalid custom model data format, please use 'custom-model-data: X' instead");
        material = Material.valueOf(UtilityMethods.enumName(itemFormat));

        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(lore, "Lore can be empty but not null");

        // Extra options
        damage = config.getInt("damage");
        texture = config.getString("texture");
        modeldata = config.getInt("custom-model-data");
        unbreakable = config.getBoolean("unbreakable");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public int getDamage() {
        return damage;
    }

    @Nullable
    public String getTexture() {
        return texture;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getModelData() {
        return modeldata;
    }
}
