package net.Indyuce.mmocore.skill.cast.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import net.Indyuce.mmocore.api.SoundObject;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import javax.annotation.Nullable;
import java.util.Objects;

public class SkillScroller implements Listener {

    /**
     * Key players need to press to start casting
     */
    private final PlayerKey enterKey, castKey;

    @Nullable
    private final SoundObject enterSound, changeSound, leaveSound;

    public SkillScroller(ConfigurationSection config) {

        // Load sounds
        enterSound = config.contains("sound.enter") ? new SoundObject(config.getConfigurationSection("sound.enter")) : null;
        changeSound = config.contains("sound.change") ? new SoundObject(config.getConfigurationSection("sound.change")) : null;
        leaveSound = config.contains("sound.leave") ? new SoundObject(config.getConfigurationSection("sound.leave")) : null;

        // Find keybinds
        enterKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("enter-key"), "Could not find enter key")));
        castKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("cast-key"), "Could not find cast key")));
    }

    @EventHandler
    public void whenPressingKey(PlayerKeyPressEvent event) {
        PlayerData playerData = event.getData();
        Player player = playerData.getPlayer();

        if (event.getPressed() == enterKey) {

            // Leave casting mode
            if (playerData.isCasting()) {

                // Cancel event if necessary
                if (event.getPressed().shouldCancelEvent())
                    event.setCancelled(true);

                playerData.leaveSkillCasting();
                if (leaveSound != null)
                    leaveSound.playTo(player);
                return;
            }

            // Check if there are skills bound
            if (playerData.getBoundSkills().isEmpty())
                return;

            // Cancel event if necessary
            if (event.getPressed().shouldCancelEvent())
                event.setCancelled(true);

            // Enter casting mode
            playerData.setSkillCasting(new CustomSkillCastingHandler(playerData));
            if (enterSound != null)
                enterSound.playTo(player);
        }

        if (event.getPressed() == castKey && playerData.isCasting()) {

            // Cancel event if necessary
            if (event.getPressed().shouldCancelEvent())
                event.setCancelled(true);

            CustomSkillCastingHandler casting = (CustomSkillCastingHandler) playerData.getSkillCasting();
            PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
            playerData.getBoundSkill(casting.index).toCastable(playerData).cast(new TriggerMetadata(caster, null, null)
                    , playerData.getBoundSkill(casting.index).getDelay(playerData));
        }
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        if (!playerData.isCasting())
            return;

        if (playerData.getBoundSkills().isEmpty()) {
            playerData.leaveSkillCasting();
            return;
        }

        event.setCancelled(true);

        int previous = event.getPreviousSlot(), current = event.getNewSlot();
        int dist1 = 9 + current - previous, dist2 = current - previous, dist3 = current - previous - 9;
        int change = Math.abs(dist1) < Math.abs(dist2) ? (Math.abs(dist1) < Math.abs(dist3) ? dist1 : dist3) : (Math.abs(dist3) < Math.abs(dist2) ? dist3 : dist2);

        // Scroll trough items
        CustomSkillCastingHandler casting = (CustomSkillCastingHandler) playerData.getSkillCasting();
        casting.index = mod(casting.index + change, playerData.getBoundSkills().size());
        casting.onTick();

        if (changeSound != null)
            changeSound.playTo(event.getPlayer());
    }

    private int mod(int x, int n) {

        while (x < 0)
            x += n;

        while (x >= n)
            x -= n;

        return x;
    }

    private class CustomSkillCastingHandler extends SkillCastingHandler {
        private int index = 0;

        CustomSkillCastingHandler(PlayerData caster) {
            super(caster, 10);
        }

        @Override
        public void onTick() {
            getCaster().displayActionBar("CLICK: " + getCaster().getBoundSkill(index).getSkill().getName());
        }
    }
}
