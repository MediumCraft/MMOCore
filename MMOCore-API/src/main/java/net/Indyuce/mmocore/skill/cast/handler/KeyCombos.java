package net.Indyuce.mmocore.skill.cast.handler;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.SoundObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.skill.cast.*;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class KeyCombos extends SkillCastingHandler {
    private final ComboMap comboMap;

    /**
     * Key players need to press to start a combo. If it's set to
     * null then the player can press any key which starts a combo.
     * These "starting keys" are saved in the combo map
     */
    @Nullable
    private final PlayerKey initializerKey;

    /**
     * Handles the display of the action bar when casting a skill.
     * Set to null if disabled
     */
    @Nullable
    private final ActionBarOptions actionBarOptions;

    @Nullable
    private final SoundObject beginComboSound, comboClickSound, failComboSound;

    public KeyCombos(@NotNull ConfigurationSection config) {
        super(config);

        comboMap = new ComboMap(config.getConfigurationSection("combos"));
        actionBarOptions = config.contains("action-bar") ? new ActionBarOptions(config.getConfigurationSection("action-bar")) : null;

        // Load sounds
        beginComboSound = config.contains("sound.begin-combo") ? new SoundObject(config.getConfigurationSection("sound.begin-combo")) : null;
        comboClickSound = config.contains("sound.combo-key") ? new SoundObject(config.getConfigurationSection("sound.combo-key")) : null;
        failComboSound = config.contains("sound.fail-combo") ? new SoundObject(config.getConfigurationSection("sound.fail-combo")) : null;

        // Find initializer key
        initializerKey = config.contains("initializer-key") ? PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("initializer-key"), "Could not find initializer key"))) : null;
    }

    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        return new CustomSkillCastingInstance(player);
    }

    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.KEY_COMBOS;
    }

    @EventHandler
    public void whenPressingKey(PlayerKeyPressEvent event) {
        PlayerData playerData = event.getData();
        Player player = playerData.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !MMOCore.plugin.configManager.canCreativeCast) return;

        // Don't start combos if no skills are bound
        if (playerData.getBoundSkills().isEmpty()) return;

        // Start combo when there is an initializer key
        if (!event.getData().isCasting() && initializerKey != null) {
            if (event.getPressed() == initializerKey) {

                // Cancel event if necessary
                if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

                // Start combo
                if (playerData.setSkillCasting() && beginComboSound != null) beginComboSound.playTo(player);
            }
            return;
        }

        // Make sure key is part of the combo map
        final ComboMap comboMap = Objects.requireNonNullElse(playerData.getProfess().getComboMap(), this.comboMap);
        if (!comboMap.isComboKey(event.getPressed())) return;

        // Player already casting
        final CustomSkillCastingInstance casting;
        if (event.getData().isCasting()) casting = (CustomSkillCastingInstance) playerData.getSkillCasting();

            // Start combo when there is NO initializer key
        else if (comboMap.isComboStart(event.getPressed()) && playerData.setSkillCasting()) {
            casting = (CustomSkillCastingInstance) playerData.getSkillCasting();
            if (beginComboSound != null) beginComboSound.playTo(player);
        }

        // Just return
        else return;

        // Adding pressed key
        casting.refreshTimeOut();
        casting.current.registerKey(event.getPressed());
        casting.onTick();
        if (comboClickSound != null) comboClickSound.playTo(player);

        // Cancel event if necessary
        if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

        // Hash current combo and check
        if (casting.combos.getCombos().containsKey(casting.current)) {
            final int spellSlot = casting.combos.getCombos().get(casting.current);
            playerData.leaveSkillCasting(true);

            // Cast spell
            if (playerData.hasSkillBound(spellSlot)) {
                PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
                playerData.getBoundSkill(spellSlot).toCastable(playerData).cast(new TriggerMetadata(caster, null, null));
            }
            return;
        }

        // Check if current combo is too large
        if (casting.current.countKeys() >= casting.combos.getLongest()) {
            playerData.leaveSkillCasting(true);
            if (failComboSound != null) failComboSound.playTo(player);
        }
    }

    /**
     * Loads the player current combos & the combos applicable to the player
     * (combos defined in its class or the default combos of the config.yml)
     */
    public class CustomSkillCastingInstance extends SkillCastingInstance {
        private final KeyCombo current = new KeyCombo();
        private final ComboMap combos;

        CustomSkillCastingInstance(PlayerData caster) {
            super(KeyCombos.this, caster);

            combos = Objects.requireNonNullElse(caster.getProfess().getComboMap(), comboMap);
        }

        @Override
        public void onTick() {
            if (actionBarOptions != null) if (actionBarOptions.isSubtitle)
                getCaster().getPlayer().sendTitle(" ", actionBarOptions.format(this), 0, 20, 0);
            else getCaster().displayActionBar(actionBarOptions.format(this));
        }

        private static final List<TriggerType> IGNORED_WHEN_CASTING = Arrays.asList(TriggerType.RIGHT_CLICK, TriggerType.LEFT_CLICK, TriggerType.SHIFT_RIGHT_CLICK, TriggerType.SHIFT_LEFT_CLICK, TriggerType.SNEAK);

        /**
         * This makes sure NO skills are cast when in casting mode so that
         * item abilities from MMOItems don't interfere with that.
         * <p>
         * Any trigger type that has a PlayerKey associated to it will
         * be ignored if the player is currently in casting mode.
         */
        @EventHandler
        public void ignoreOtherSkills(PlayerCastSkillEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return;
            if (IGNORED_WHEN_CASTING.contains(event.getCast().getTrigger())) event.setCancelled(true);
        }
    }

    private class ActionBarOptions {
        private final String separator, noKey, prefix, suffix;

        /**
         * Saves the names for all the players keys. Used when displaying
         * the current player's key combo on the action bar
         */
        private final Map<PlayerKey, String> keyNames = new HashMap<>();
        private final boolean isSubtitle;

        ActionBarOptions(ConfigurationSection config) {
            this.prefix = config.contains("prefix") ? config.getString("prefix") : "";
            this.suffix = config.contains("suffix") ? config.getString("suffix") : "";
            this.separator = Objects.requireNonNull(config.getString("separator"), "Could not find action bar option 'separator'");
            this.noKey = Objects.requireNonNull(config.getString("no-key"), "Could not find action bar option 'no-key'");
            this.isSubtitle = config.getBoolean("is-subtitle", false);
            for (PlayerKey key : PlayerKey.values())
                keyNames.put(key, Objects.requireNonNull(config.getString("key-name." + key.name()), "Could not find translation for key " + key.name()));
        }

        public String format(CustomSkillCastingInstance casting) {
            StringBuilder builder = new StringBuilder();
            Placeholders holders = MMOCore.plugin.actionBarManager.getActionBarPlaceholders(casting.getCaster());

            builder.append(prefix);
            // Join all keys with separator
            builder.append(casting.current.countKeys() == 0 ? noKey : keyNames.get(casting.current.getAt(0)));
            int j = 1;
            for (; j < casting.current.countKeys(); j++)
                builder.append(separator + keyNames.get(casting.current.getAt(j)));

            // All remaining
            for (; j < casting.combos.getLongest(); j++)
                builder.append(separator + noKey);

            builder.append(suffix);
            return holders.apply(casting.getCaster().getPlayer(), builder.toString());
        }
    }
}
