package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;

import javax.inject.Provider;

public class CastableSkill extends Skill {
    private final ClassSkill skill;

    /**
     * Now uses a provider since 1.12 because the skill level can CHANGE
     * with time, and such instance of CastableSkill is
     */
    private final Provider<Integer> skillLevel;

    @Deprecated
    public CastableSkill(ClassSkill skill, int fixedLevel) {
        super(skill.getSkill().getTrigger());

        this.skill = skill;
        this.skillLevel = () -> fixedLevel;
    }

    public CastableSkill(ClassSkill skill, PlayerData playerData) {
        super(skill.getSkill().getTrigger());

        this.skill = skill;
        this.skillLevel = () -> playerData.getSkillLevel(skill.getSkill());
    }

    public ClassSkill getSkill() {
        return skill;
    }

    @Override
    public boolean getResult(SkillMetadata skillMeta) {
        PlayerData playerData = PlayerData.get(skillMeta.getCaster().getData());
        boolean loud = !getTrigger().isSilent();

        // Skill is not usable yet
        if (!playerData.hasUnlockedLevel(skill)) {
            if (loud) ConfigMessage.fromKey("skill-level-not-met").send(playerData.getPlayer());
            return false;
        }

        // Global cooldown check
        if (!getTrigger().isPassive() && playerData.getActivityTimeOut(PlayerActivity.CAST_SKILL) > 0)
            return false;

        // Cooldown check
        if (skillMeta.getCaster().getData().getCooldownMap().isOnCooldown(this)) {
            if (loud) ConfigMessage.fromKey("casting.on-cooldown",
                    "cooldown", MythicLib.plugin.getMMOConfig().decimal.format(skillMeta.getCaster().getData().getCooldownMap().getCooldown(this))).send(playerData.getPlayer());
            return false;
        }

        // Mana cost
        if (playerData.getMana() < skillMeta.getParameter("mana")) {
            if (loud) ConfigMessage.fromKey("casting.no-mana",
                    "mana-required", MythicLib.plugin.getMMOConfig().decimal.format((skillMeta.getParameter("mana") - playerData.getMana())),
                    "mana", playerData.getProfess().getManaDisplay().getName()).send(playerData.getPlayer());
            return false;
        }

        // Stamina cost
        if (playerData.getStamina() < skillMeta.getParameter("stamina")) {

            if (loud) ConfigMessage.fromKey("casting.no-stamina").send(playerData.getPlayer());
            return false;
        }

        // Ability flag
        if (!MythicLib.plugin.getFlags().isFlagAllowed(skillMeta.getCaster().getPlayer(), CustomFlag.MMO_ABILITIES))
            return false;

        return true;
    }

    @Override
    public void whenCast(SkillMetadata skillMeta) {
        PlayerData casterData = PlayerData.get(skillMeta.getCaster().getData());

        // Apply cooldown, mana and stamina costs
        if (!casterData.noCooldown) {

            // Cooldown
            double flatCooldownReduction = Math.max(0, Math.min(1, skillMeta.getCaster().getStat("COOLDOWN_REDUCTION") / 100));
            CooldownInfo cooldownHandler = skillMeta.getCaster().getData().getCooldownMap().applyCooldown(this, skillMeta.getParameter("cooldown"));
            cooldownHandler.reduceInitialCooldown(flatCooldownReduction);

            casterData.giveMana(-skillMeta.getParameter("mana"), PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
            casterData.giveStamina(-skillMeta.getParameter("stamina"), PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
        }

        if (!getTrigger().isPassive())
            casterData.setLastActivity(PlayerActivity.CAST_SKILL);
    }

    @Override
    public SkillHandler<?> getHandler() {
        return skill.getSkill().getHandler();
    }

    @Override
    public double getParameter(String mod) {
        return skill.getParameter(mod, skillLevel.get());
    }
}
