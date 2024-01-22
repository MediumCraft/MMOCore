package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.util.Closeable;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.PvpModeCommand;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatHandler implements Closeable {
    private final PlayerData player;
    private long lastEntry = System.currentTimeMillis(), lastHit = System.currentTimeMillis(), invulnerableTill;

    private boolean pvpMode;

    @Nullable
    private BukkitTask task;

    public CombatHandler(PlayerData player) {
        this.player = player;
    }

    public void update() {
        lastHit = System.currentTimeMillis();
        invulnerableTill = 0;
        player.getMMOPlayerData().getCooldownMap().applyCooldown(PvpModeCommand.COOLDOWN_KEY, MMOCore.plugin.configManager.pvpModeCombatCooldown);

        // Simply refreshing
        if (isInCombat()) {
            task.cancel();
            task = newTask();

            // Entering combat
        } else {
            lastEntry = System.currentTimeMillis();
            ConfigMessage.fromKey("now-in-combat").send(player.getPlayer());
            Bukkit.getPluginManager().callEvent(new PlayerCombatEvent(player, true));
            task = newTask();
        }
    }

    @NotNull
    private BukkitTask newTask() {
        return Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> quit(false), MMOCore.plugin.configManager.combatLogTimer / 50);
    }

    public boolean isInPvpMode() {
        return pvpMode;
    }

    public void setPvpMode(boolean pvpMode) {
        this.pvpMode = pvpMode;
    }

    public long getLastHit() {
        return lastHit;
    }

    public long getLastEntry() {
        return lastEntry;
    }

    public long getInvulnerableTill() {
        return invulnerableTill;
    }

    /**
     * Simply checks if there is a scheduled task.
     *
     * @return If the player is in combat
     */
    public boolean isInCombat() {
        return task != null;
    }

    /**
     * This is used for PvP mode inactivity when a player
     * joins a region with PvP mode toggled on, OR toggles on
     * PvP mode using the command when standing in a PvP region.
     *
     * @return If the player is inert i.e if he CAN hit/take damage
     */
    public boolean isInvulnerable() {
        return System.currentTimeMillis() < invulnerableTill;
    }

    public void setInvulnerable(double time) {
        invulnerableTill = System.currentTimeMillis() + (long) (time * 1000);
    }

    public boolean canQuitPvpMode() {
        return System.currentTimeMillis() > lastHit + MMOCore.plugin.configManager.pvpModeCombatTimeout * 1000;
    }

    /**
     * Quits combat. Throws an exception if player is not in combat.
     * Can be called anytime when the player is in combat.
     *
     * @param cancelTask Should the running task be canceled.
     */
    private void quit(boolean cancelTask) {
        Validate.isTrue(isInCombat(), "Player not in combat");
        if (cancelTask) task.cancel();
        task = null;

        if (player.isOnline()) {
            Bukkit.getPluginManager().callEvent(new PlayerCombatEvent(player, false));
            ConfigMessage.fromKey("leave-combat").send(player.getPlayer());
        }
    }

    @Override
    public void close() {
        if (isInCombat()) quit(true);

        // Necessary steps when entering a town.
        lastHit = 0;
        invulnerableTill = 0;
        player.getMMOPlayerData().getCooldownMap().resetCooldown(PvpModeCommand.COOLDOWN_KEY);
    }
}
