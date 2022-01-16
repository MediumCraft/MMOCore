package net.Indyuce.mmocore.skill.cast;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class SkillCastingHandler extends BukkitRunnable implements Listener {
    private final PlayerData caster;

    private boolean open = true;

    public SkillCastingHandler(PlayerData caster, int runnablePeriod) {
        this.caster = caster;

        runTaskTimer(MMOCore.plugin, 0, runnablePeriod);
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    public PlayerData getCaster() {
        return caster;
    }

    public void close() {
        Validate.isTrue(open, "Skill casting already ended");

        open = false;

        // Unregister listeners
        HandlerList.unregisterAll(this);

        // Cancel runnable
        cancel();
    }

    @Override
    public void run() {
        if (!caster.isOnline() || caster.getPlayer().isDead())
            caster.leaveCastingMode();
        else
            onTick();
    }

    public abstract void onTick();
}
