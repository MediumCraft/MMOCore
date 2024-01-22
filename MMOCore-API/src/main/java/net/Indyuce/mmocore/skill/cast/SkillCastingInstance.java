package net.Indyuce.mmocore.skill.cast;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class SkillCastingInstance extends BukkitRunnable implements Listener {
    private final PlayerData caster;
    private final SkillCastingHandler handler;
    private final int runnablePeriod = 10; // Hard coded

    private boolean open = true;
    private int j, sinceLastActivity;

    public SkillCastingInstance(@NotNull SkillCastingHandler handler, @NotNull PlayerData caster) {
        this.handler = handler;
        this.caster = caster;

        runTaskTimer(MMOCore.plugin, 0, 1);
        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    public PlayerData getCaster() {
        return caster;
    }

    public void close() {
        Validate.isTrue(open, "Skill casting already closed");

        open = false;

        // Unregister listeners
        HandlerList.unregisterAll(this);

        // Cancel runnable
        cancel();
    }

    public void refreshTimeOut() {
        sinceLastActivity = 0;
    }

    private static final int PARTICLES_PER_TICK = 2;

    @Override
    public void run() {
        if (!caster.isOnline() || caster.getPlayer().isDead() || caster.getBoundSkills().isEmpty()) {
            caster.leaveSkillCasting(true);
            return;
        }

        // Check for timeout
        if (handler.doesTimeOut() && sinceLastActivity++ > handler.getTimeoutDelay()) {
            caster.leaveSkillCasting(true);
            return;
        }

        // Apply casting particles
        if (caster.getProfess().getCastParticle() != null) for (int k = 0; k < PARTICLES_PER_TICK; k++) {
            final double a = (double) (PARTICLES_PER_TICK * j + k) / 4;
            caster.getProfess().getCastParticle().display(caster.getPlayer().getLocation().add(Math.cos(a), 1 + Math.sin(a / 3) / 1.3, Math.sin(a)));
        }

        // Apply casting mode-specific effects
        if (j++ % runnablePeriod == 0) onTick();
    }

    public abstract void onTick();
}
