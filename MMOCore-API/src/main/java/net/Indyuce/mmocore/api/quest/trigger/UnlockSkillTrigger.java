package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import java.util.Objects;

public class UnlockSkillTrigger extends Trigger implements Removable {
    private final RegisteredSkill skill;

    public UnlockSkillTrigger(MMOLineConfig config) {
        super(config);
        config.validateKeys("skill");
        skill = Objects.requireNonNull(MMOCore.plugin.skillManager.getSkill(config.getString("skill")));
    }

    @Override
    public void apply(PlayerData player) {
        player.unlock(skill);
    }

    @Override
    public void remove(PlayerData playerData) {
        playerData.lock(skill);
    }
}
