package net.Indyuce.mmocore.skilltree.tree.display;

import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * The material and custom model-data of a node
 */
public class Icon {
    private final Material material;
    private final int customModelData;

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public Icon(ConfigurationSection config) {
        this(Material.valueOf(Objects.requireNonNull(UtilityMethods.enumName(
                config.getString("item")))), config.contains("model-data") ? config.getInt("model-data") : 0);
    }

    public Icon(Material material, int customModelData) {
        this.material = material;
        this.customModelData = customModelData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Icon icon = (Icon) o;
        return customModelData == icon.customModelData && material == icon.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, customModelData);
    }

    @Override
    public String toString() {
        return "Icon{" +
                "material=" + material +
                ", customModelData=" + customModelData +
                '}';
    }
}
