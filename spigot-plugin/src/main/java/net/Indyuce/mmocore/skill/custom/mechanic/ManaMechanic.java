package net.Indyuce.mmocore.skill.custom.mechanic;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.custom.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ManaMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final PlayerResourceUpdateEvent.UpdateReason reason;

    public ManaMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        reason = PlayerResourceUpdateEvent.UpdateReason.valueOf(UtilityMethods.enumName(config.getString("reason", "CUSTOM")));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        PlayerData targetData = PlayerData.get(target.getUniqueId());
        targetData.giveMana(amount.evaluate(meta), reason);
    }
}
