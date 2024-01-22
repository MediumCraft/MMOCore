package net.Indyuce.mmocore.skill.cast.handler;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.cast.SkillCastingHandler;
import net.Indyuce.mmocore.skill.cast.SkillCastingInstance;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class SkillCastingDisabled extends SkillCastingHandler {
    public SkillCastingDisabled(@NotNull ConfigurationSection config) {
        super(config);
    }

    @Override
    public SkillCastingInstance newInstance(@NotNull PlayerData player) {
        throw new RuntimeException("Skill casting is disabled");
    }

    @Override
    public SkillCastingMode getCastingMode() {
        return SkillCastingMode.NONE;
    }
}
