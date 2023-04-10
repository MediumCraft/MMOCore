package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.skillmod.SkillModifier;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkillModifierTrigger extends Trigger implements Removable {
    private final SkillModifier mod;
    private final String buffKey = TRIGGER_PREFIX + "." + UUID.randomUUID();
    private final double amount;

    public SkillModifierTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("modifier");
        config.validateKeys("amount");
        config.validateKeys("type");
        amount = config.getDouble("amount");
        String skillModifier = config.getString("modifier");
        String formula = config.getString("formula", "true");
        String type = config.getString("type").toUpperCase();
        Validate.isTrue(type.equals("FLAT") || type.equals("RELATIVE"));
        List<SkillHandler<?>> targetSkills = new ArrayList<>();
        for (RegisteredSkill skill : MMOCore.plugin.skillManager.getAll()) {
            if (skill.matchesFormula(formula))
                targetSkills.add(skill.getHandler());
        }
        mod = new SkillModifier(buffKey, skillModifier, targetSkills, amount, ModifierType.valueOf(type));
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
     * Used by skill slots to apply a skillBuff to a specific skill dynamically chosen .
     */
    public void apply(PlayerData playerData, SkillHandler<?> skill) {
        mod.register(playerData.getMMOPlayerData(), skill);
    }

    /**
     * Used by skill slots to remove a skillBuff from a specific skill dynamically chosen.
     */
    public void remove(PlayerData playerData, SkillHandler<?> skill) {
        mod.unregister(playerData.getMMOPlayerData(), skill);
    }
}
