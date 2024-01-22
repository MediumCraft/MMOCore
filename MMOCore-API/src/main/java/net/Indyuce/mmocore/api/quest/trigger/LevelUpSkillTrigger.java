package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import java.util.Objects;

public class LevelUpSkillTrigger extends Trigger implements Removable {
    private final RegisteredSkill skill;
    private final int amount;

    public LevelUpSkillTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("skill", "amount");
        amount = config.getInt("amount");
        skill = Objects.requireNonNull(MMOCore.plugin.skillManager.getSkill(config.getString("skill")));
    }

    @Override
    public void apply(PlayerData playerData) {
        playerData.setSkillLevel(skill, playerData.getSkillLevel(skill) + amount);
    }

    @Override
    public void remove(PlayerData playerData) {
        playerData.setSkillLevel(skill, Math.max(0, playerData.getSkillLevel(skill) - amount));
    }
}
