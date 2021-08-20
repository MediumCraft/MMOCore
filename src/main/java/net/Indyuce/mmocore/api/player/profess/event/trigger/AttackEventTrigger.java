package net.Indyuce.mmocore.api.player.profess.event.trigger;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.event.EventTriggerHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class AttackEventTrigger implements EventTriggerHandler {

    @Override
    public boolean handles(String event) {
        return event.endsWith("-damage");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void a(PlayerAttackEvent event) {
        // We don't want players dying by themselves when using an enderpearl.
        if (event.getPlayer().equals(event.getEntity())) return;

        PlayerData player = PlayerData.get(event.getData().getUniqueId());
        PlayerClass profess = player.getProfess();

        for (DamageType type : event.getAttack().getDamage().collectTypes()) {
            String path = type.getPath() + "-damage";
            if (profess.hasEventTriggers(path))
                profess.getEventTriggers(path).getTriggers().forEach(trigger -> trigger.apply(player));
        }
    }
}
