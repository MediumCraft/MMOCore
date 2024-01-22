package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.api.InstanceModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmocore.api.player.PlayerData;

import java.util.UUID;

public class AttributeModifier extends InstanceModifier {
    private final String attribute;

    /**
     * Flat attribute modifier (simplest modifier you can think about)
     */
    public AttributeModifier(String key, String attribute, double value) {
        super(key, value);

        this.attribute = attribute;
    }

    /**
     * Attribute modifier given by an external mechanic, like a party buff, item
     * set bonuses, skills or abilities... Anything apart from items and armor.
     */
    public AttributeModifier(String key, String attribute, double value, ModifierType type) {
        this(key, attribute, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    /**
     * Attribute modifier given by an item, either a weapon or an armor piece.
     *
     * @param key       Player modifier key
     * @param attribute Attribute being modified
     * @param value     Value of stat modifier
     * @param type      Is the modifier flat or multiplicative
     * @param slot      Slot of the item granting the stat modifier
     * @param source    Type of the item granting the stat modifier
     */
    public AttributeModifier(String key, String attribute, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        this(UUID.randomUUID(), key, attribute, value, type, slot, source);
    }

    /**
     * Attribute modifier given by an item, either a weapon or an armor piece.
     *
     * @param key       Player modifier key
     * @param attribute Attribute being modified
     * @param value     Value of stat modifier
     * @param type      Is the modifier flat or multiplicative
     * @param slot      Slot of the item granting the stat modifier
     * @param source    Type of the item granting the stat modifier
     */
    public AttributeModifier(UUID uniqueId, String key, String attribute, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(uniqueId, key, slot, source, value, type);

        this.attribute = attribute;
    }

    /**
     * Used to parse a StatModifier from a string in a configuration section.
     * Always returns a modifier with source OTHER. Can be used by MythicCore
     * to handle party buffs, or MMOItems for item set bonuses. Throws IAE
     *
     * @param str The string to be parsed
     */
    public AttributeModifier(String key, String attribute, String str) {
        super(key, EquipmentSlot.OTHER, ModifierSource.OTHER, str);

        this.attribute = attribute;
    }

    public AttributeModifier(ConfigObject object) {
        super(object);

        this.attribute = object.getString("attribute");
    }

    public String getAttribute() {
        return attribute;
    }

    /**
     * Used to multiply some existing stat modifier by a constant, usually an
     * integer, for instance when MMOCore party modifiers scale with the
     * number of the party member count
     *
     * @param coef The multiplicative constant
     * @return A new instance of StatModifier with modified value
     */
    public AttributeModifier multiply(double coef) {
        return new AttributeModifier(getUniqueId(), getKey(), attribute, value * coef, type, getSlot(), getSource());
    }

    @Override
    public void register(MMOPlayerData mmoPlayerData) {
        PlayerData playerData = PlayerData.get(mmoPlayerData);
        playerData.getAttributes().getInstance(attribute).addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData mmoPlayerData) {
        PlayerData playerData = PlayerData.get(mmoPlayerData);
        playerData.getAttributes().getInstance(attribute).removeModifier(getKey());
    }
}