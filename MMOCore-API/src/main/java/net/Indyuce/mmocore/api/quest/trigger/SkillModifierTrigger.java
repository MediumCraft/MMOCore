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
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SkillModifierTrigger extends Trigger implements Removable, Temporary {
    private SkillModifier mod;
    private boolean mutable = true;

    public SkillModifierTrigger(MMOLineConfig config) {
        super(config);

        config.validateKeys("modifier");
        config.validateKeys("amount");

        final double amount = config.getDouble("amount");
        final String parameter = config.getString("modifier");
        final String formula = config.getString("formula", "true");
        final ModifierType type = config.contains("type") ? ModifierType.valueOf(UtilityMethods.enumName(config.getString("type"))) : ModifierType.FLAT;
        final List<SkillHandler<?>> targetSkills = MMOCore.plugin.skillManager.getAll().stream().filter(skill -> skill.matchesFormula(formula)).map(RegisteredSkill::getHandler).collect(Collectors.toList());

        mod = new SkillModifier(Trigger.STAT_MODIFIER_KEY, parameter, targetSkills, amount, type);
    }

    public void updateKey(@NotNull String key) {
        Validate.isTrue(mutable, "No longer mutable");
        this.mod = new SkillModifier(key, mod.getParameter(), mod.getSkills(), mod.getValue(), mod.getType());
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
        mutable = false;
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
