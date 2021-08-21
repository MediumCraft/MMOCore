package net.Indyuce.mmocore.comp.mythicmobs.load;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import org.apache.commons.lang.Validate;

import java.util.HashSet;
import java.util.Optional;

public class MythicSkillTrigger extends Trigger {
    private final Skill skill;

    public MythicSkillTrigger(MMOLineConfig config) {
        super(config);

        config.validate("id");
        String id = config.getString("id");
        Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(id);
        Validate.isTrue(opt.isPresent(), "Could not find MM skill " + id);
        skill = opt.get();
    }

    @Override
    public void apply(PlayerData player) {
        if (!player.isOnline()) return;

        AbstractPlayer trigger = BukkitAdapter.adapt(player.getPlayer());
        SkillCaster caster = new GenericCaster(trigger);
        SkillMetadata skillMeta = new SkillMetadata(SkillTrigger.API, caster, trigger, BukkitAdapter.adapt(player.getPlayer().getLocation()), new HashSet<>(), null, 1);
        if (skill.usable(skillMeta, SkillTrigger.API))
            skill.execute(skillMeta);
    }
}
