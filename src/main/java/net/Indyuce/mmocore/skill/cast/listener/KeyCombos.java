package net.Indyuce.mmocore.skill.cast.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.SoundObject;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.cast.KeyCombo;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public class KeyCombos implements Listener {

    /**
     * Using instances of KeyCombo as keys work because
     * {@link KeyCombo} has a working implementation for the
     * hash code method
     */
    private final Map<KeyCombo, Integer> combos = new HashMap<>();

    /**
     * Key players need to press to start a combo
     */
    private final PlayerKey initializerKey;
    private final int longestCombo;

    /**
     * Handles the display of the action bar when casting a skill.
     * Set to null if disabled
     */
    @Nullable
    private final ActionBarOptions actionBarOptions;

    @Nullable
    private final SoundObject beginComboSound, comboClickSound, failComboSound;

    /**
     * Essentially the inverse of the {@link #combos} map. This maps
     * the skill slot to the corresponding key combo. There's no problem
     * because the maps are 100% bijective
     */
    private static final Map<Integer, KeyCombo> PUBLIC_COMBOS = new HashMap<>();

    public KeyCombos(ConfigurationSection config) {

        int longestCombo = 0;

        // Load different combos
        for (String key : config.getConfigurationSection("combos").getKeys(false))
            try {
                int spellSlot = Integer.valueOf(key);
                Validate.isTrue(spellSlot >= 0, "Spell slot must be at least 0");
                Validate.isTrue(!combos.values().contains(spellSlot), "There is already a key combo with the same skill slot");
                KeyCombo combo = new KeyCombo();
                for (String str : config.getStringList("combos." + key))
                    combo.registerKey(PlayerKey.valueOf(UtilityMethods.enumName(str)));

                combos.put(combo, spellSlot);
                longestCombo = Math.max(longestCombo, combo.countKeys());

                PUBLIC_COMBOS.put(spellSlot, combo);
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load key combo '" + key + "': " + exception.getMessage());
            }

        this.longestCombo = longestCombo;

        // Load player key names
        actionBarOptions = config.contains("action-bar") ? new ActionBarOptions(config.getConfigurationSection("action-bar")) : null;

        // Load sounds
        beginComboSound = config.contains("sound.begin-combo") ? new SoundObject(config.getConfigurationSection("sound.begin-combo")) : null;
        comboClickSound = config.contains("sound.combo-key") ? new SoundObject(config.getConfigurationSection("sound.combo-key")) : null;
        failComboSound = config.contains("sound.fail-combo") ? new SoundObject(config.getConfigurationSection("sound.fail-combo")) : null;

        // Find initializer key
        initializerKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("initializer-key"), "Could not find initializer key")));
    }

    @EventHandler
    public void whenPressingKey(PlayerKeyPressEvent event) {
        PlayerData playerData = event.getData();
        Player player = playerData.getPlayer();

        if (!event.getData().isCasting()) {
            if (event.getPressed() == initializerKey) {

                // Always cancel event
                event.setCancelled(true);

                // Start combo
                playerData.setSkillCasting(new CustomSkillCastingHandler(playerData));
                if (beginComboSound != null)
                    beginComboSound.playTo(player);
            }
            return;
        }


        // Adding pressed key
        CustomSkillCastingHandler casting = (CustomSkillCastingHandler) playerData.getSkillCasting();
        casting.current.registerKey(event.getPressed());
        casting.onTick();
        if (comboClickSound != null)
            comboClickSound.playTo(player);

        // Always cancel event
        event.setCancelled(true);

        // Hash current combo and check
        if (combos.containsKey(casting.current)) {
            int spellSlot = combos.get(casting.current) - 1;
            playerData.leaveCastingMode();

            // Cast spell
            if (playerData.hasSkillBound(spellSlot)) {
                PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
                playerData.getBoundSkill(spellSlot).toCastable(playerData).cast(new TriggerMetadata(caster, null, null));
            }
            return;
        }

        // Check if current combo is too large
        if (casting.current.countKeys() >= longestCombo) {
            playerData.leaveCastingMode();
            if (failComboSound != null)
                failComboSound.playTo(player);
        }
    }

    private static final Set<TriggerType> IGNORED_WHEN_CASTING = new HashSet<>();

    static {
        IGNORED_WHEN_CASTING.add(TriggerType.RIGHT_CLICK);
        IGNORED_WHEN_CASTING.add(TriggerType.LEFT_CLICK);
        IGNORED_WHEN_CASTING.add(TriggerType.SHIFT_RIGHT_CLICK);
        IGNORED_WHEN_CASTING.add(TriggerType.SHIFT_LEFT_CLICK);
        IGNORED_WHEN_CASTING.add(TriggerType.SNEAK);
    }

    /**
     * This makes sure NO skills are cast when in casting mode so that
     * item abilities from MMOItems don't interfere with that.
     * <p>
     * Any trigger type that has a PlayerKey associated to it will
     * be ignored if the player is currently in casting mode.
     */
    @EventHandler
    public void ignoreOtherSkills(PlayerCastSkillEvent event) {
        TriggerType triggerType = event.getCast().getTrigger();
        if (IGNORED_WHEN_CASTING.contains(triggerType) && PlayerData.get(event.getData().getUniqueId()).isCasting())
            event.setCancelled(true);
    }

    private class CustomSkillCastingHandler extends SkillCastingHandler {
        private final KeyCombo current = new KeyCombo();

        CustomSkillCastingHandler(PlayerData caster) {
            super(caster, 10);
        }

        @Override
        public void onTick() {
            if (actionBarOptions != null)
                getCaster().displayActionBar(actionBarOptions.format(current));
        }
    }

    private class ActionBarOptions {
        private final String separator, noKey;

        /**
         * Saves the names for all the players keys. Used when displaying
         * the current player's key combo on the action bar
         */
        private final Map<PlayerKey, String> keyNames = new HashMap<>();

        ActionBarOptions(ConfigurationSection config) {
            this.separator = Objects.requireNonNull(config.getString("separator"), "Could not find action bar option 'separator'");
            this.noKey = Objects.requireNonNull(config.getString("no-key"), "Could not find action bar option 'no-key'");

            for (PlayerKey key : PlayerKey.values())
                keyNames.put(key, Objects.requireNonNull(config.getString("key-name." + key.name()), "Could not find translation for key " + key.name()));
        }

        public String format(KeyCombo currentCombo) {

            // Join all keys with separator
            String builder = currentCombo.countKeys() == 0 ? noKey : keyNames.get(currentCombo.getAt(0));
            int j = 1;
            for (; j < currentCombo.countKeys(); j++)
                builder += separator + keyNames.get(currentCombo.getAt(j));

            // All remaining
            for (; j < longestCombo; j++)
                builder += separator + noKey;

            return MythicLib.plugin.parseColors(builder);
        }
    }
}
