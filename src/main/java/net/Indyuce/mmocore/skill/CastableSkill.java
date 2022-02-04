package net.Indyuce.mmocore.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;

public class CastableSkill extends Skill {
    private final ClassSkill skill;
    private final int skillLevel;

    public CastableSkill(ClassSkill skill, int skillLevel) {
        this.skill = skill;
        this.skillLevel = skillLevel;
    }

    public ClassSkill getSkill() {
        return skill;
    }

    @Override
    public boolean getResult(SkillMetadata skillMeta) {
        PlayerData playerData = PlayerData.get(skillMeta.getCaster().getData().getUniqueId());
        boolean loud = !skill.getSkill().hasTrigger() || !skill.getSkill().getTrigger().isSilent();

        // If the caster has unlocked that skill
        if (!playerData.hasSkillUnlocked(skill)) {
            if (loud) MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-skill").send(playerData.getPlayer());
            return false;
        }

        // Global cooldown check
        if (!skill.getSkill().isPassive() && playerData.getActivityTimeOut(PlayerActivity.CAST_SKILL) > 0)
            return false;

        // Cooldown check
        if (skillMeta.getCaster().getData().getCooldownMap().isOnCooldown(this)) {
            if (loud) MMOCore.plugin.configManager.getSimpleMessage("casting.on-cooldown").send(playerData.getPlayer());
            return false;
        }

        // Mana cost
        if (playerData.getMana() < getModifier("mana")) {
            if (loud) MMOCore.plugin.configManager.getSimpleMessage("casting.no-mana", "mana", playerData.getProfess().getManaDisplay().getName()).send(playerData.getPlayer());
            return false;
        }

        // Stamina cost
        if (playerData.getStamina() < getModifier("stamina")) {
            if (loud) MMOCore.plugin.configManager.getSimpleMessage("casting.no-stamina").send(playerData.getPlayer());
            return false;
        }

        // Ability flag
        if (!MythicLib.plugin.getFlags().isFlagAllowed(skillMeta.getCaster().getPlayer(), CustomFlag.MMO_ABILITIES))
            return false;

        return true;
    }

    @Override
    public void whenCast(SkillMetadata skillMeta) {
        PlayerData casterData = PlayerData.get(skillMeta.getCaster().getData().getUniqueId());

        // Apply cooldown, mana and stamina costs
        if (!casterData.noCooldown) {

            // Cooldown
            double flatCooldownReduction = Math.max(0, Math.min(1, skillMeta.getCaster().getStat("COOLDOWN_REDUCTION") / 100));
            CooldownInfo cooldownHandler = skillMeta.getCaster().getData().getCooldownMap().applyCooldown(this, getModifier("cooldown"));
            cooldownHandler.reduceInitialCooldown(flatCooldownReduction);

            casterData.giveMana(-getModifier("mana"), PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
            casterData.giveStamina(-getModifier("stamina"), PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
        }

        casterData.setLastActivity(PlayerActivity.CAST_SKILL);
    }

    @Override
    public SkillHandler<?> getHandler() {
        return skill.getSkill().getHandler();
    }

    @Override
    public double getModifier(String mod) {
        return skill.getModifier(mod, skillLevel);
    }
}
