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
    private int j;

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
        if (!caster.isOnline() || caster.getPlayer().isDead()) {
            caster.leaveSkillCasting();
            return;
        }

        // Apply casting particles
        if (caster.getProfess().getCastParticle() != null)
            for (int k = 0; k < 2; k++) {
                double a = (double) j++ / 5;
                caster.getProfess().getCastParticle()
                        .display(caster.getPlayer().getLocation().add(Math.cos(a), 1 + Math.sin(a / 3) / 1.3, Math.sin(a)));
            }

        // Apply casting mode-specific effects
        onTick();
    }

    public abstract void onTick();
}
