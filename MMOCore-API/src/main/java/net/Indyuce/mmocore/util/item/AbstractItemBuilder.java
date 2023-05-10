package net.Indyuce.mmocore.util.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public abstract class AbstractItemBuilder {
    private final ConfigItem configItem;
    private final Map<String, String> placeholders = new HashMap<>();

    public AbstractItemBuilder(@NotNull ConfigItem configItem) {
        this.configItem = Objects.requireNonNull(configItem, "Config item cannot be null");
    }

    public AbstractItemBuilder(String key) {
        this(MMOCore.plugin.configItems.get(key));
    }

    public ConfigItem getItem() {
        return configItem;
    }

    public AbstractItemBuilder addPlaceholders(String... placeholders) {
        for (int j = 0; j < placeholders.length - 1; j += 2)
            this.placeholders.put(placeholders[j], placeholders[j + 1]);
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(configItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial() && configItem.getTexture() != null)
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", configItem.getTexture()));
                profileField.set(meta, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
                MMOCore.log(Level.WARNING, "Could not load texture of config item called '" + configItem.getId() + "'");
            }

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(applyPlaceholders(configItem.getName()));
        if (configItem.isUnbreakable())
            meta.setUnbreakable(true);
        if (meta instanceof Damageable)
            ((Damageable) meta).setDamage(configItem.getDamage());
        meta.setCustomModelData(configItem.getModelData());

        List<String> lore = new ArrayList<>();
        configItem.getLore().forEach(line -> lore.add(applyPlaceholders(line)));
        meta.setLore(lore);

        whenBuildingMeta(item, meta);
        item.setItemMeta(meta);

        NBTItem nbtItem = NBTItem.get(item);
        nbtItem.addTag(new ItemTag("MMOCoreItemId", configItem.getId()));
        whenBuildingNBT(nbtItem);
        return nbtItem.toItem();
    }

    public abstract void whenBuildingMeta(ItemStack item, ItemMeta meta);

    public abstract void whenBuildingNBT(NBTItem nbtItem);

    public String applyPlaceholders(String string) {
        for (String placeholder : placeholders.keySet())
            if (string.contains("{" + placeholder + "}"))
                string = string.replace("{" + placeholder + "}", "" + placeholders.get(placeholder));
        return MythicLib.plugin.parseColors(string);
    }
}
