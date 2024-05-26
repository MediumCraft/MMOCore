package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import javax.annotation.Nullable;

public class UnlockSkillTrigger extends Trigger implements Removable {
    private final RegisteredSkill skill;

    public UnlockSkillTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("skill");
        skill = MMOCore.plugin.skillManager.getSkillOrThrow(config.getString("skill"));
    }

    @Override
    public void apply(PlayerData playerData) {
        final @Nullable ClassSkill found = playerData.getProfess().getSkill(skill);
        if (found != null) playerData.unlock(found);
    }

    @Override
    public void remove(PlayerData playerData) {
        final @Nullable ClassSkill found = playerData.getProfess().getSkill(skill);
        if (found != null) playerData.lock(found);
    }
}
