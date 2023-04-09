package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.skillmod.SkillModifier;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillModifierTrigger extends Trigger implements Removable {
    private final SkillModifier mod;
    private final String modifierKey = TRIGGER_PREFIX + "." + UUID.randomUUID();
    private final double amount;

    public SkillModifierTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("modifier");
        config.validateKeys("amount");
        config.validateKeys("formula");
        config.validateKeys("type");

        amount = config.getDouble("amount");
        String skillModifier = config.getString("modifier");
        String formula = config.getString("formula");
        final List<SkillHandler<?>> targetSkills = new ArrayList<>();
        for (RegisteredSkill skill : MMOCore.plugin.skillManager.getAll())
            if (skill.matchesFormula(formula))
                targetSkills.add(skill.getHandler());

        mod = new SkillModifier(modifierKey, skillModifier, targetSkills, amount);
    }

    @Override
    public void apply(PlayerData player) {
        mod.register(player.getMMOPlayerData());
    }

    @Override
    public void remove(PlayerData playerData) {
        mod.unregister(playerData.getMMOPlayerData());
    }
}
