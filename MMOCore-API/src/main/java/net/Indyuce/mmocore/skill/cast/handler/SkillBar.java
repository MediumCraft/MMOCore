package net.Indyuce.mmocore.skill.cast.handler;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
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

import java.util.Objects;

public class SkillBar extends SkillCastingHandler {
    private final PlayerKey mainKey;
    private final boolean disableSneak;

    public SkillBar(@NotNull ConfigurationSection config) {
        super(config);

        mainKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("open"), "Could not find open key")));
        disableSneak = config.getBoolean("disable-sneak");
    }

    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        return new CustomSkillCastingInstance(player);
    }

    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.SKILL_BAR;
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
        if (player.getGameMode() != GameMode.SPECTATOR && (MMOCore.plugin.configManager.canCreativeCast || player.getGameMode() != GameMode.CREATIVE) && !playerData.isCasting() && !playerData.getBoundSkills().isEmpty())
            if (playerData.setSkillCasting())
                MMOCore.plugin.soundManager.getSound(SoundEvent.SPELL_CAST_BEGIN).playTo(player);
    }

    public class CustomSkillCastingInstance extends SkillCastingInstance {
        private final String ready = ConfigMessage.fromKey("casting.action-bar.ready").asLine();
        private final String onCooldown = ConfigMessage.fromKey("casting.action-bar.on-cooldown").asLine();
        private final String noMana = ConfigMessage.fromKey("casting.action-bar.no-mana").asLine();
        private final String noStamina = ConfigMessage.fromKey("casting.action-bar.no-stamina").asLine();
        private final String split = ConfigMessage.fromKey("casting.split").asLine();

        private int j;

        CustomSkillCastingInstance(PlayerData playerData) {
            super(SkillBar.this, playerData);
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
            refreshTimeOut();
            final int slot = event.getNewSlot() + 1 + (event.getNewSlot() >= player.getInventory().getHeldItemSlot() ? -1 : 0);

            /*
             * The event is called again soon after the first since when
             * cancelling the first one, the player held item slot must go back
             * to the previous one.
             */
            if (slot >= 1 && getCaster().hasSkillBound(slot)) {
                final PlayerMetadata caster = getCaster().getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
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

            if (getCaster().leaveSkillCasting()) {
                MMOCore.plugin.soundManager.getSound(SoundEvent.SPELL_CAST_END).playTo(player);
                ConfigMessage.fromKey("casting.no-longer").send(getCaster().getPlayer());
            }
        }

        private String getFormat(PlayerData data) {
            final StringBuilder str = new StringBuilder();
            if (!data.isOnline()) return str.toString();
            for (int slot : data.mapBoundSkills().keySet()) {
                final ClassSkill skill = data.getBoundSkill(slot);
                if (skill.getSkill().getTrigger().isPassive()) continue;

                str.append(str.isEmpty() ? "" : split).append((onCooldown(data, skill) ? onCooldown.replace("{cooldown}",
                        String.valueOf(data.getCooldownMap().getInfo(skill).getRemaining() / 1000)) : noMana(data, skill) ? noMana : (noStamina(
                        data, skill) ? noStamina : ready)).replace("{index}",
                                String.valueOf(slot + (data.getPlayer().getInventory().getHeldItemSlot() < slot ? 1 : 0)))
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
            return skill.getSkill().hasParameter("mana") && skill.getParameter("mana", data.getSkillLevel(skill.getSkill())) > data.getMana();
        }

        private boolean noStamina(PlayerData data, ClassSkill skill) {
            return skill.getSkill().hasParameter("stamina") && skill.getParameter("stamina", data.getSkillLevel(skill.getSkill())) > data.getStamina();
        }

        @Override
        public void onTick() {
            if (j++ % 2 == 0) getCaster().displayActionBar(getFormat(getCaster()));
        }
    }
}
