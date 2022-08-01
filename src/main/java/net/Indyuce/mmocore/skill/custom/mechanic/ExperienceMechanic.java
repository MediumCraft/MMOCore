package net.Indyuce.mmocore.skill.custom.mechanic;

import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.SimpleExperienceObject;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExperienceMechanic extends TargetMechanic {
    @NotNull
    private final DoubleFormula amount;
    @NotNull
    private final EXPSource source;
    @NotNull
    private final ExperienceDispenser dispenser;

    public ExperienceMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");
        amount = config.getDoubleFormula("amount");

        if (config.contains("profession")) {
            String id = config.getString("profession").toLowerCase().replace("_", "-");
            Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession");
            dispenser = MMOCore.plugin.professionManager.get(id);
        } else
            dispenser = new SimpleExperienceObject();
        source = config.contains("source") ? EXPSource.valueOf(config.getString("source").toUpperCase()) : EXPSource.QUEST;
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        PlayerData targetData = PlayerData.get(target.getUniqueId());
        dispenser.giveExperience(targetData, amount.evaluate(meta), null, source);
    }
}
