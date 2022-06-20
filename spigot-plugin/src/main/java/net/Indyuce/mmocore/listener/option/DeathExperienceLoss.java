package net.Indyuce.mmocore.listener.option;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathExperienceLoss implements Listener {
    private final double loss = MMOCore.plugin.getConfig().getDouble("death-exp-loss.percent") / 100;

    @EventHandler(priority = EventPriority.HIGH)
    public void a(PlayerDeathEvent event) {
        if (!PlayerData.has(event.getEntity()))
            return;

        PlayerData data = PlayerData.get(event.getEntity());
        int loss = (int) (data.getExperience() * this.loss);
        data.setExperience(data.getExperience() - loss);
        if (data.isOnline())
            new ConfigMessage("death-exp-loss").addPlaceholders("loss", "" + loss).send(data.getPlayer());
    }
}
