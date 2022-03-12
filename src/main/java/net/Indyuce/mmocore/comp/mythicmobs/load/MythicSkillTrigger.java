package net.Indyuce.mmocore.comp.mythicmobs.load;

import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.mobs.GenericCaster;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMetadataImpl;
import io.lumine.mythic.core.skills.SkillTriggers;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class MythicSkillTrigger extends Trigger {
    private final Skill skill;

    public MythicSkillTrigger(MMOLineConfig config) {
        super(config);

        config.validate("id");
        String id = config.getString("id");
        Optional<io.lumine.mythic.api.skills.Skill> opt = MythicBukkit.inst().getSkillManager().getSkill(id);
        Validate.isTrue(opt.isPresent(), "Could not find MM skill " + id);
        skill = opt.get();
    }

    @Override
    public void apply(PlayerData player) {
        if (!player.isOnline()) return;

        AbstractPlayer trigger = BukkitAdapter.adapt(player.getPlayer());
        SkillCaster caster = new GenericCaster(trigger);
        SkillMetadata skillMeta = new SkillMetadataImpl(SkillTriggers.API, caster, trigger, BukkitAdapter.adapt(player.getPlayer().getLocation()), new HashSet<>(), null, 1);
        if (skill.isUsable(skillMeta))
            skill.execute(skillMeta);
    }
}
