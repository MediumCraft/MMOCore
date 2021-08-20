package net.Indyuce.mmocore.comp.mythicmobs.skill;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.HashSet;

public abstract class PassiveMythicMobSkillHandler implements Listener {
    protected final MythicMobSkill skill;

    /**
     * Core class for all passive types
     */
    protected PassiveMythicMobSkillHandler(MythicMobSkill skill) {
        this.skill = skill;
    }

    public SkillMetadata castSkill(PlayerData data) {
        return castSkill(data, null);
    }

    public SkillMetadata castSkill(PlayerData playerData, Entity target) {
        if (!playerData.getProfess().hasSkill(skill))
            return null;

        Skill.SkillInfo skill = playerData.getProfess().getSkill(this.skill);
        CasterMetadata caster = new CasterMetadata(playerData);
        SkillMetadata cast = new SkillMetadata(caster, skill);
        if (!cast.isSuccessful() || this.skill.isPassive())
            return cast;

        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();

        // The only difference
        if (target != null)
            targetEntities.add(BukkitAdapter.adapt(target));

        AbstractEntity trigger = BukkitAdapter.adapt(caster.getPlayer());
        SkillCaster skillCaster = new GenericCaster(trigger);
        io.lumine.xikage.mythicmobs.skills.SkillMetadata skillMeta = new io.lumine.xikage.mythicmobs.skills.SkillMetadata(SkillTrigger.API, skillCaster, trigger, BukkitAdapter.adapt(caster.getPlayer().getEyeLocation()), targetEntities, targetLocations, 1);

        // Disable anticheat
        if (MMOCore.plugin.hasAntiCheat())
            MMOCore.plugin.antiCheatSupport.disableAntiCheat(caster.getPlayer(), this.skill.getAntiCheat());

        if (this.skill.getSkill().usable(skillMeta, SkillTrigger.API))
            this.skill.getSkill().execute(skillMeta);
        else
            cast.abort();

        return cast;
    }
}
