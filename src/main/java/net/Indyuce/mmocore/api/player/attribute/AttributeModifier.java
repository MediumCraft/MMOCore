package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;

import java.text.DecimalFormat;

public class AttributeModifier extends PlayerModifier {
    private final String attribute;
    private final double value;
    private final ModifierType type;

    private static final DecimalFormat oneDigit = MythicLib.plugin.getMMOConfig().newDecimalFormat("0.#");

    /**
     * Flat attribute modifier (simplest modifier you can think about)
     */
    public AttributeModifier(String key, String attribute, double value) {
        this(key, attribute, value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    /**
     * Attribute modifier given by an external mecanic, like a party buff, item set bonuses,
     * skills or abilities... Anything apart from items and armor.
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
        super(key, slot, source);

        this.attribute = attribute;
        this.value = value;
        this.type = type;
    }

    /**
     * Used to parse a StatModifier from a string in a configuration section.
     * Always returns a modifier with source OTHER. Can be used by MythicCore
     * to handle party buffs, or MMOItems for item set bonuses. Throws IAE
     *
     * @param str The string to be parsed
     */
    public AttributeModifier(String key, String attribute, String str) {
        super(key, EquipmentSlot.OTHER, ModifierSource.OTHER);

        Validate.notNull(str, "String cannot be null");
        Validate.notEmpty(str, "String cannot be empty");

        type = str.toCharArray()[str.length() - 1] == '%' ? ModifierType.RELATIVE : ModifierType.FLAT;
        value = Double.parseDouble(type == ModifierType.RELATIVE ? str.substring(0, str.length() - 1) : str);
        this.attribute = attribute;
    }

    public AttributeModifier(ConfigObject object) {
        super(object.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);

        this.attribute = object.getString("attribute");
        this.value = object.getDouble("value");
        type = object.getBoolean("multiplicative", false) ? ModifierType.RELATIVE : ModifierType.FLAT;
    }

    public String getAttribute() {
        return attribute;
    }

    public ModifierType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    /**
     * Used to multiply some existing stat modifier by a constant, usually an
     * integer, for instance when MMOCore party modifiers scale with the
     * number of the party member count
     *
     * @param coef The multiplicative constant
     * @return A new instance of StatModifier with modified value
     */
    public StatModifier multiply(double coef) {
        return new StatModifier(getKey(), attribute, value * coef, type, getSlot(), getSource());
    }

    @Override
    public void register(MMOPlayerData mmoPlayerData) {
        PlayerData playerData = PlayerData.get(mmoPlayerData.getUniqueId());
        playerData.getAttributes().getInstance(attribute).addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData mmoPlayerData) {
        PlayerData playerData = PlayerData.get(mmoPlayerData.getUniqueId());
        playerData.getAttributes().getInstance(attribute).removeModifier(getKey());
    }

    @Override
    public String toString() {
        return oneDigit.format(value) + (type == io.lumine.mythic.lib.player.modifier.ModifierType.RELATIVE ? "%" : "");
    }
}