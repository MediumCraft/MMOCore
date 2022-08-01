package net.Indyuce.mmocore.skill.custom.mechanic;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class StelliumMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final Operation operation;
    private final PlayerResourceUpdateEvent.UpdateReason reason;

    public StelliumMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        reason = PlayerResourceUpdateEvent.UpdateReason.valueOf(UtilityMethods.enumName(config.getString("reason", "CUSTOM")));
        operation = config.contains("operation") ? Operation.valueOf(config.getString("operation").toUpperCase()) : Operation.GIVE;
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        PlayerData targetData = PlayerData.get(target.getUniqueId());
        if (operation == Operation.GIVE)
            targetData.giveStellium(amount.evaluate(meta), reason);
        else if (operation == Operation.SET)
            targetData.setStellium(amount.evaluate(meta));
        else if (operation == Operation.TAKE)
            targetData.giveStellium(-amount.evaluate(meta), reason);
    }
}
