package net.Indyuce.mmocore.comp.mythicmobs.skill;

import io.lumine.mythic.lib.player.CooldownInfo;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerPostCastSkillEvent;
import net.Indyuce.mmocore.api.event.PlayerPreCastSkillEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.HashSet;

public abstract class PassiveSkillHandler implements Listener {
    protected final MythicSkill skill;

    /**
     * Core class for all passive types
     */
    protected PassiveSkillHandler(MythicSkill skill) {
        this.skill = skill;
    }

    /**
     * Forces the player to cast the skill without a target
     *
     * @param playerData Player casting the skill
     * @return Metadata of cast skill
     */
    public SkillMetadata castSkill(PlayerData playerData) {
        return castSkill(playerData, null);
    }

    /**
     * Forces the player to cast the skill with a target
     * <p>
     * DRY ALERT
     * This is a mere copy of the {@link PlayerData#cast(Skill.SkillInfo)} method
     * yet there isn't any other solution since MMOCore skills do not support targets.
     * The only way to make sure MythicMobs takes the event target into account is
     * to rewrite everything
     * <p>
     * This will change in the future when Mythic and MMOCore skills finally merge
     *
     * @param playerData Player casting the skill
     * @param target     Skill target
     * @return Metadata of cast skill
     */

    public SkillMetadata castSkill(PlayerData playerData, Entity target) {
        if (!playerData.getProfess().hasSkill(skill))
            return null;

        // Check for Bukkit pre cast event
        Skill.SkillInfo skill = playerData.getProfess().getSkill(this.skill);
        PlayerPreCastSkillEvent preEvent = new PlayerPreCastSkillEvent(playerData, skill);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled())
            return new SkillMetadata(playerData, skill, SkillMetadata.CancelReason.OTHER);

        // Gather MMOCore skill info
        CasterMetadata caster = new CasterMetadata(playerData);
        SkillMetadata cast = new SkillMetadata(caster, skill);
        if (!cast.isSuccessful())
            return cast;

        // Gather MythicMobs skill info
        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();

        // The only difference
        if (target != null)
            targetEntities.add(BukkitAdapter.adapt(target));

        AbstractEntity trigger = BukkitAdapter.adapt(caster.getPlayer());
        SkillCaster skillCaster = new GenericCaster(trigger);
        io.lumine.xikage.mythicmobs.skills.SkillMetadata skillMeta = new io.lumine.xikage.mythicmobs.skills.SkillMetadata(SkillTrigger.API, skillCaster, trigger, BukkitAdapter.adapt(caster.getPlayer().getEyeLocation()), targetEntities, targetLocations, 1);

        // Check if the MythicMobs skill can be cast
        if (!this.skill.getSkill().usable(skillMeta, SkillTrigger.CAST)) {
            cast.abort();
            return cast;
        }

        // Disable anticheat
        if (MMOCore.plugin.hasAntiCheat())
            MMOCore.plugin.antiCheatSupport.disableAntiCheat(caster.getPlayer(), this.skill.getAntiCheat());

        // Place cast skill info in a variable
        skillMeta.getVariables().putObject("MMOSkill", cast);
        skillMeta.getVariables().putObject("MMOStatMap", caster.getStats());

        // Apply cooldown, mana and stamina costs
        if (!playerData.noCooldown) {

            // Cooldown
            double flatCooldownReduction = Math.max(0, Math.min(1, playerData.getStats().getStat(StatType.COOLDOWN_REDUCTION) / 100));
            CooldownInfo cooldownHandler = playerData.getCooldownMap().applyCooldown(cast.getSkill(), cast.getCooldown());
            cooldownHandler.reduceInitialCooldown(flatCooldownReduction);

            // Mana and stamina cost
            playerData.giveMana(-cast.getManaCost(), PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
            playerData.giveStamina(-cast.getStaminaCost(), PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
        }

        // Execute the MythicMobs skill
        this.skill.getSkill().execute(skillMeta);

        Bukkit.getPluginManager().callEvent(new PlayerPostCastSkillEvent(playerData, skill, cast));
        return cast;
    }
}
