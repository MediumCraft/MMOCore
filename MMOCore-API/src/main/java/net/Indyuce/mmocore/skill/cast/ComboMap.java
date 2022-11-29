package net.Indyuce.mmocore.skill.cast;

import io.lumine.mythic.lib.UtilityMethods;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComboMap {

    /**
     * Using instances of KeyCombo as keys work because {@link KeyCombo}
     * has a working implementation for the hash code method
     */
    private final Map<KeyCombo, Integer> combos = new HashMap<>();

    /**
     * All the keys that are at the start of a combo.
     */
    private final Set<PlayerKey> firstKeys = new HashSet<>();

    private final int longestCombo;

    public ComboMap(ConfigurationSection config) {

        int currentLongestCombo = 0;
        for (String key : config.getKeys(false))
            try {
                final int spellSlot = Integer.valueOf(key);
                Validate.isTrue(spellSlot >= 0, "Spell slot must be at least 0");
                Validate.isTrue(!combos.values().contains(spellSlot), "There is already a key combo with the same skill slot");
                KeyCombo combo = new KeyCombo();
                for (String str : config.getStringList(key))
                    combo.registerKey(PlayerKey.valueOf(UtilityMethods.enumName(str)));

                combos.put(combo, spellSlot);
                firstKeys.add(combo.getAt(0));
                currentLongestCombo = Math.max(currentLongestCombo, combo.countKeys());
            } catch (RuntimeException exception) {
                throw new RuntimeException("Could not loading key combo '" + key + "': " + exception.getMessage());
            }

        this.longestCombo = currentLongestCombo;
    }

    public Map<KeyCombo, Integer> getCombos() {
        return combos;
    }

    public int getLongest() {
        return longestCombo;
    }

    public boolean isComboStart(PlayerKey key) {
        return firstKeys.contains(key);
    }
}
