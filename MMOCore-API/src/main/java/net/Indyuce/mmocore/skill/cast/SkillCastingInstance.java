package net.Indyuce.mmocore.skill.cast;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.binding.BoundSkillInfo;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SkillCastingInstance extends BukkitRunnable implements Listener {
    private final PlayerData caster;
    private final SkillCastingHandler handler;

    private static final int RUNNABLE_PERIOD = 10;

    /**
     * This variable temporarily stores the active skills that the player
     * can try to cast.
     */
    private List<BoundSkillInfo> activeSkills;
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

    @NotNull
    public List<BoundSkillInfo> getActiveSkills() {
        if (activeSkills == null)
            activeSkills = caster.getBoundSkills().values().stream().filter(bound -> !bound.isPassive()).collect(Collectors.toList());
        return activeSkills;
    }

    private static final int PARTICLES_PER_TICK = 2;

    @Override
    public void run() {
        if (UtilityMethods.isInvalidated(caster.getMMOPlayerData()) || !caster.hasActiveSkillBound()) {
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
        if (j++ % RUNNABLE_PERIOD == 0) {
            activeSkills = null;
            onTick();
        }
    }

    public abstract void onTick();
}
