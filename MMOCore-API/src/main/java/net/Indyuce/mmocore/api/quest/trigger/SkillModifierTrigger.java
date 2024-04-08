package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.skillmod.SkillModifier;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.api.quest.trigger.api.Temporary;
import net.Indyuce.mmocore.skill.RegisteredSkill;

import java.util.ArrayList;
import java.util.List;

public class SkillModifierTrigger extends Trigger implements Removable, Temporary {
    private final SkillModifier mod;
    private final double amount;

    public SkillModifierTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("modifier");
        config.validateKeys("amount");

        amount = config.getDouble("amount");
        final String skillModifier = config.getString("modifier");
        final String formula = config.getString("formula", "true");
        final ModifierType type = config.contains("type") ? ModifierType.valueOf(UtilityMethods.enumName(config.getString("type"))) : ModifierType.FLAT;
        List<SkillHandler<?>> targetSkills = new ArrayList<>();
        for (RegisteredSkill skill : MMOCore.plugin.skillManager.getAll())
            if (skill.matchesFormula(formula))
                targetSkills.add(skill.getHandler());

        mod = new SkillModifier(Trigger.STAT_MODIFIER_KEY, skillModifier, targetSkills, amount, type);
    }

    public List<SkillHandler<?>> getTargetSkills() {
        return mod.getSkills();
    }

    @Override
    public void apply(PlayerData player) {
        mod.register(player.getMMOPlayerData());
    }

    @Override
    public void remove(PlayerData playerData) {
        mod.unregister(playerData.getMMOPlayerData());
    }

    /**
     * Used by skill slots to apply a skill modifier.
     * to a dynamically chosen skill handler.
     */
    public void apply(PlayerData playerData, SkillHandler<?> skill) {
        mod.register(playerData.getMMOPlayerData(), skill);
    }

    /**
     * Used by skill slots to remove a skillBuff
     * from a dynamically chosen skill handler.
     */
    public void remove(PlayerData playerData, SkillHandler<?> skill) {
        mod.unregister(playerData.getMMOPlayerData(), skill);
    }
}
