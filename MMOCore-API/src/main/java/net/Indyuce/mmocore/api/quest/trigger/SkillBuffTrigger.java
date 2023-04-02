package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.skill.SkillBuff;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillBuffTrigger extends Trigger implements Removable {
    private final SkillBuff skillBuff;
    private final String buffKey = TRIGGER_PREFIX + "." + UUID.randomUUID();
    private final double amount;


    public SkillBuffTrigger(MMOLineConfig config) {
        super(config);
        config.validateKeys("modifier");
        config.validateKeys("amount");
        config.validateKeys("type");
        amount = config.getDouble("amount");
        String skillModifier = config.getString("modifier");
        String formula = config.getString("formula", "true");
        List<String> targetSkills = new ArrayList<>();
        for (RegisteredSkill skill : MMOCore.plugin.skillManager.getAll()) {
            if (skill.matchesFormula(formula))
                targetSkills.add(skill.getHandler().getId());
        }
        skillBuff = new SkillBuff(buffKey, skillModifier, targetSkills, amount);
    }

    @Override
    public void apply(PlayerData player) {
        if (player.getMMOPlayerData().getSkillBuffMap().hasSkillBuff(buffKey)) {
            player.getMMOPlayerData().getSkillBuffMap().getSkillBuff(buffKey).add(amount).register(player.getMMOPlayerData());
        } else {
            skillBuff.register(player.getMMOPlayerData());
        }
    }

    @Override
    public void remove(PlayerData playerData) {
        skillBuff.unregister(playerData.getMMOPlayerData());
    }


    /**
     * Used by skill slots to apply a skillBuff to a specific skill dynamically chosen .
     */
    public void apply(PlayerData playerData, String skill) {
        skillBuff.register(playerData.getMMOPlayerData(), skill);
    }
    /**
     * Used by skill slots to remove a skillBuff from a specific skill dynamically chosen.
     */
    public void remove(PlayerData playerData, String skill) {
        skillBuff.unregister(playerData.getMMOPlayerData(), skill);
    }
}
