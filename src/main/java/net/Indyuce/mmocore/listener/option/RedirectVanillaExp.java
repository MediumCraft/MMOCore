package net.Indyuce.mmocore.listener.option;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class RedirectVanillaExp implements Listener {
    private final double ratio;

    public RedirectVanillaExp(double ratio) {
        this.ratio = ratio;
    }

    @EventHandler
    public void a(PlayerExpChangeEvent event) {
        PlayerData.get(event.getPlayer()).giveExperience(event.getAmount() * ratio, EXPSource.VANILLA);
    }
}
