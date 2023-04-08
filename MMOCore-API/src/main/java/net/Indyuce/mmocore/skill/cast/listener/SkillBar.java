package net.Indyuce.mmocore.skill.cast.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.cast.PlayerKey;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class SkillBar implements Listener {
    private final PlayerKey mainKey;
    private final boolean disableSneak;

    public SkillBar(ConfigurationSection config) {
        mainKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("open"), "Could not find open key")));
        disableSneak = config.getBoolean("disable-sneak");
    }

    @EventHandler
    public void enterSkillCasting(PlayerKeyPressEvent event) {
        if (event.getPressed() != mainKey) return;

        // Extra option to improve support with other plugins
        final Player player = event.getPlayer();
        if (disableSneak && player.isSneaking()) return;

        // Always cancel event
        if (event.getPressed().shouldCancelEvent()) event.setCancelled(true);

        // Enter spell casting
        final PlayerData playerData = event.getData();
        if (player.getGameMode() != GameMode.SPECTATOR && (MMOCore.plugin.configManager.canCreativeCast || player.getGameMode() != GameMode.CREATIVE) && !playerData.isCasting() && !playerData.getBoundSkills()
                .isEmpty()) {
            playerData.setSkillCasting(new CustomSkillCastingHandler(playerData));
            MMOCore.plugin.soundManager.getSound(SoundEvent.SPELL_CAST_BEGIN).playTo(player);
        }
    }

    private class CustomSkillCastingHandler extends SkillCastingHandler {
        private final String ready = MMOCore.plugin.configManager.getSimpleMessage("casting.action-bar.ready").message();
        private final String onCooldown = MMOCore.plugin.configManager.getSimpleMessage("casting.action-bar.on-cooldown").message();
        private final String noMana = MMOCore.plugin.configManager.getSimpleMessage("casting.action-bar.no-mana").message();
        private final String noStamina = MMOCore.plugin.configManager.getSimpleMessage("casting.action-bar.no-stamina").message();
        private final String split = MMOCore.plugin.configManager.getSimpleMessage("casting.split").message();

        private int j;

        CustomSkillCastingHandler(PlayerData playerData) {
            super(playerData, 1);
        }

        @EventHandler
        public void onSkillCast(PlayerItemHeldEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return;

            // Extra option to improve support with other plugins
            final Player player = event.getPlayer();
            if (disableSneak && player.isSneaking()) return;

            /*
             * When the event is cancelled, another playerItemHeldEvent is
             * called and previous and next slots are equal. the event must not
             * listen to that non-player called event.
             */
            if (event.getPreviousSlot() == event.getNewSlot()) return;

            event.setCancelled(true);
            int slot = event.getNewSlot() + (event.getNewSlot() >= player.getInventory().getHeldItemSlot() ? -1 : 0);

            /*
             * The event is called again soon after the first since when
             * cancelling the first one, the player held item slot must go back
             * to the previous one.
             */
            if (slot >= 0 && getCaster().hasSkillBound(slot)) {
                PlayerMetadata caster = getCaster().getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
                getCaster().getBoundSkill(slot).toCastable(getCaster()).cast(new TriggerMetadata(caster, null, null));
            }
        }

        @EventHandler
        public void stopCasting(PlayerKeyPressEvent event) {
            if (!event.getPlayer().equals(getCaster().getPlayer())) return;

            if (event.getPressed() != mainKey) return;

            // Extra option to improve support with other plugins
            final Player player = event.getPlayer();
            if (disableSneak && player.isSneaking()) return;

            MMOCore.plugin.soundManager.getSound(SoundEvent.SPELL_CAST_END).playTo(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    MMOCore.plugin.configManager.getSimpleMessage("casting.no-longer").send(getCaster().getPlayer());
                }
            }.runTask(MMOCore.plugin);
            getCaster().leaveSkillCasting();
        }

        private String getFormat(PlayerData data) {
            StringBuilder str = new StringBuilder();
            if (!data.isOnline()) return str.toString();
            for (int slot : data.mapBoundSkills().keySet()) {
                ClassSkill skill = data.getBoundSkill(slot);
                str.append((str.length() == 0) ? "" : split).append((onCooldown(data, skill) ? onCooldown.replace("{cooldown}",
                        String.valueOf(data.getCooldownMap().getInfo(skill).getRemaining() / 1000)) : noMana(data, skill) ? noMana : (noStamina(
                        data, skill) ? noStamina : ready)).replace("{index}",
                                "" + (slot + 1 + (data.getPlayer().getInventory().getHeldItemSlot() <= slot ? 1 : 0)))
                        .replace("{skill}", data.getBoundSkill(slot).getSkill().getName()));
            }
            return MMOCore.plugin.placeholderParser.parse(data.getPlayer(), str.toString());
        }

        /**
         * We don't even need to check if the skill has the 'cooldown'
         * modifier. We just look for an entry in the cooldown map which
         * won't be here if the skill has no cooldown.
         */
        private boolean onCooldown(PlayerData data, ClassSkill skill) {
            return data.getCooldownMap().isOnCooldown(skill);
        }

        private boolean noMana(PlayerData data, ClassSkill skill) {
            return skill.getSkill().hasModifier("mana") && skill.getModifier("mana", data.getSkillLevel(skill.getSkill())) > data.getMana();
        }

        private boolean noStamina(PlayerData data, ClassSkill skill) {
            return skill.getSkill().hasModifier("stamina") && skill.getModifier("stamina",
                    data.getSkillLevel(skill.getSkill())) > data.getStamina();
        }

        @Override
        public void onTick() {
            if (j++ % 20 == 0) getCaster().displayActionBar(getFormat(getCaster()));
        }
    }
}
