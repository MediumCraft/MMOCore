package net.Indyuce.mmocore.util;

import io.lumine.mythic.lib.UtilityMethods;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The material and custom model-data of a node
 */
public class Icon {
    private final Material material;
    private final int modelData;

    public Material getMaterial() {
        return material;
    }

    public int getModelData() {
        return modelData;
    }

    public Icon(@NotNull Material material, int modelData) {
        this.material = material;
        this.modelData = modelData;
    }

    public ItemStack toItem() {
        final ItemStack stack = new ItemStack(material);
        if (modelData > 0) {
            final ItemMeta meta = stack.getItemMeta();
            meta.setCustomModelData(modelData);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Icon icon = (Icon) o;
        return modelData == icon.modelData && material == icon.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, modelData);
    }

    @Override
    public String toString() {
        return "Icon{" + "material=" + material + ", customModelData=" + modelData + '}';
    }

    @NotNull
    public static Icon from(@Nullable Object object) {
        Validate.notNull(object, "Could not read icon");

        if (object instanceof ConfigurationSection) {
            final ConfigurationSection config = (ConfigurationSection) object;
            final Material material = Material.valueOf(UtilityMethods.enumName(((ConfigurationSection) object).getString("item")));
            final int modelData = config.getInt("model-data", config.getInt("custom-model-data")); // Backwards compatibility
            return new Icon(material, modelData);
        }

        if (object instanceof String) {
            final String[] split = ((String) object).split("[:.,]");
            final Material mat = Material.valueOf(UtilityMethods.enumName(split[0]));
            final int modelData = split.length == 1 ? 0 : Integer.parseInt(split[1]);
            return new Icon(mat, modelData);
        }

        throw new RuntimeException("Needs either a string or configuration section");
    }
}
