package net.Indyuce.mmocore.skill.cast.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.util.SoundObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import net.Indyuce.mmocore.skill.cast.SkillCastingInstance;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public class SkillScroller extends SkillCastingHandler {

    /**
     * Key players need to press to start casting
     */
    private final PlayerKey enterKey, castKey;

    @Nullable
    private final SoundObject enterSound, changeSound, leaveSound;

    private final String actionBarFormat;

    public SkillScroller(@NotNull ConfigurationSection config) {
        super(config);

        // Load sounds
        enterSound = config.contains("sound.enter") ? new SoundObject(config.getConfigurationSection("sound.enter")) : null;
        changeSound = config.contains("sound.change") ? new SoundObject(config.getConfigurationSection("sound.change")) : null;
        leaveSound = config.contains("sound.leave") ? new SoundObject(config.getConfigurationSection("sound.leave")) : null;

        actionBarFormat = config.getString("action-bar-format", "CLICK TO CAST: {selected}");

        // Find keybinds
        enterKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("enter-key"), "Could not find enter key")));
        castKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("cast-key"), "Could not find cast key")));
    }

    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        return new CustomSkillCastingInstance(player);
    }

    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.SKILL_SCROLLER;
    }

    @EventHandler
    public void whenPressingKey(PlayerKeyPressEvent event) {
        PlayerData playerData = event.getData();
        Player player = playerData.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !MMOCore.plugin.configManager.canCreativeCast)
            return;

        if (event.getPressed() == enterKey) {

            // Leave casting mode
            if (playerData.isCasting()) {

                // Cancel event if necessary
                if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

                if (!playerData.leaveSkillCasting()) return;

                if (leaveSound != null) leaveSound.playTo(player);
                return;
            }

            // Check if there are skills bound
            if (!playerData.hasActiveSkillBound()) return;

            // Cancel event if necessary
            if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

            // Enter casting mode
            if (!playerData.setSkillCasting()) return;

            if (enterSound != null) enterSound.playTo(player);
        }

        if (event.getPressed() == castKey && playerData.isCasting()) {

            // Cancel event if necessary
            if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

            CustomSkillCastingInstance casting = (CustomSkillCastingInstance) playerData.getSkillCasting();
            PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
            casting.getSelected().toCastable(playerData).cast(new TriggerMetadata(caster, null, null));
        }
    }

    public class CustomSkillCastingInstance extends SkillCastingInstance {
        private int index = 0;

        CustomSkillCastingInstance(PlayerData caster) {
            super(SkillScroller.this, caster);
        }

        @Override
        public void onTick() {
            final String skillName = getSelected().getSkill().getName();
            final String actionBarFormat = MythicLib.plugin.getPlaceholderParser().parse(getCaster().getPlayer(), SkillScroller.this.actionBarFormat.replace("{selected}", skillName));
            getCaster().displayActionBar(actionBarFormat);
        }

        public ClassSkill getSelected() {
            return getActiveSkills().get(index).getClassSkill();
        }

        @EventHandler
        public void onScroll(PlayerItemHeldEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return;

            PlayerData playerData = PlayerData.get(event.getPlayer());
            if (!playerData.hasActiveSkillBound()) {
                playerData.leaveSkillCasting(true);
                return;
            }

            event.setCancelled(true);

            final int previous = event.getPreviousSlot(), current = event.getNewSlot();
            final int dist1 = 9 + current - previous, dist2 = current - previous, dist3 = current - previous - 9;
            final int change = Math.abs(dist1) < Math.abs(dist2) ? (Math.abs(dist1) < Math.abs(dist3) ? dist1 : dist3) : (Math.abs(dist3) < Math.abs(dist2) ? dist3 : dist2);

            // Scroll through items
            final CustomSkillCastingInstance casting = (CustomSkillCastingInstance) playerData.getSkillCasting();
            casting.index = mod(casting.index + change, getActiveSkills().size());
            casting.onTick();
            casting.refreshTimeOut();

            if (changeSound != null) changeSound.playTo(event.getPlayer());
        }
    }

    private int mod(int x, int n) {
        while (x < 0) x += n;
        while (x >= n) x -= n;
        return x;
    }
}
