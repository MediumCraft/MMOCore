package net.Indyuce.mmocore.skill.cast.handler;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerKeyPressEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SkillBar extends SkillCastingHandler {
    private final PlayerKey mainKey;
    private final boolean disableSneak, lowestKeybinds;

    public SkillBar(@NotNull ConfigurationSection config) {
        super(config);

        mainKey = PlayerKey.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("open"), "Could not find open key")));
        disableSneak = config.getBoolean("disable-sneak");
        lowestKeybinds = config.getBoolean("use-lowest-keybinds");
    }

    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        return new CustomSkillCastingInstance(player);
    }

    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.SKILL_BAR;
    }

    @Override
    public void onSkillBound(@NotNull PlayerData player) {

        // Lowest indices = start at slot 1 and increase
        if (lowestKeybinds) {

            int slot = 1;

            for (BoundSkillInfo bound : player.getBoundSkills().values())
                // Set cast slot and increment slot
                if (!bound.isPassive()) bound.skillBarCastSlot = slot++;
        }

        // Otherwise, direct correspondance
        else player.getBoundSkills().forEach((slot, bound) -> bound.skillBarCastSlot = slot);
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
        if (player.getGameMode() != GameMode.SPECTATOR
                && (MMOCore.plugin.configManager.canCreativeCast || player.getGameMode() != GameMode.CREATIVE)
                && !playerData.isCasting()
                && playerData.hasActiveSkillBound())
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
        public void onItemHeld(PlayerItemHeldEvent event) {
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

            // Look for skill with given slot
            ClassSkill classSkill = findSkillToCast(player.getInventory().getHeldItemSlot(), event.getNewSlot());
            if (classSkill != null) classSkill.toCastable(getCaster()).cast(getCaster().getMMOPlayerData());
        }

        @Nullable
        private ClassSkill findSkillToCast(int currentSlot, int clickedSlot) {
            for (BoundSkillInfo info : this.getActiveSkills())
                if (info.skillBarCastSlot + (currentSlot < info.skillBarCastSlot ? 1 : 0) == 1 + clickedSlot)
                    return info.getClassSkill();
            return null;
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

        @NotNull
        private String getFormat(PlayerData data) {
            if (!data.isOnline()) return "";

            final StringBuilder str = new StringBuilder();
            for (BoundSkillInfo active : getActiveSkills()) {
                final ClassSkill skill = active.getClassSkill();
                final int slot = active.skillBarCastSlot;

                str.append(str.isEmpty() ? "" : split).append(
                        (onCooldown(data, skill) ? onCooldown.replace("{cooldown}",
                                String.valueOf(data.getCooldownMap().getInfo(skill).getRemaining() / 1000)) :
                                noMana(data, skill) ? noMana : (noStamina(data, skill) ? noStamina : ready))
                                .replace("{index}", String.valueOf(slot + (data.getPlayer().getInventory().getHeldItemSlot() < slot ? 1 : 0)))
                                .replace("{skill}", skill.getSkill().getName()));
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
