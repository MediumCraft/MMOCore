package net.Indyuce.mmocore.command;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PvpModeCommand extends RegisteredCommand {
    public PvpModeCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.PVP_MODE);
    }

    public static final String COOLDOWN_KEY = "PvpMode";

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return false;
        }

        if (!sender.hasPermission("mmocore.pvpmode")) {
            MMOCore.plugin.configManager.getSimpleMessage("not-enough-perms").send((Player) sender);
            return false;
        }

        final PlayerData playerData = PlayerData.get(((Player) sender).getUniqueId());

        // Command cooldown
        if (playerData.getCooldownMap().isOnCooldown(COOLDOWN_KEY)) {
            MMOCore.plugin.configManager.getSimpleMessage("pvp-mode.cooldown", "remaining", MythicLib.plugin.getMMOConfig().decimal.format(playerData.getCooldownMap().getCooldown(COOLDOWN_KEY))).send((Player) sender);
            return true;
        }

        playerData.getCombat().setPvpMode(!playerData.getCombat().isInPvpMode());
        playerData.getCooldownMap().applyCooldown(COOLDOWN_KEY, playerData.getCombat().isInPvpMode() ? MMOCore.plugin.configManager.pvpModeToggleOnCooldown : MMOCore.plugin.configManager.pvpModeToggleOffCooldown);

        // Toggling on when in PVP region
        if (playerData.getCombat().isInPvpMode() &&
                MythicLib.plugin.getFlags().isFlagAllowed(playerData.getPlayer(), CustomFlag.PVP_MODE)) {
            playerData.getCombat().setInvulnerable(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeCommand);
            MMOCore.plugin.configManager.getSimpleMessage("pvp-mode.toggle.on-invulnerable", "time",
                    MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.configManager.pvpModeInvulnerabilityTimeCommand)).send(playerData.getPlayer());

            // Just send message otherwise
        } else
            MMOCore.plugin.configManager.getSimpleMessage("pvp-mode.toggle." + (playerData.getCombat().isInPvpMode() ? "on" : "off") + "-safe").send((Player) sender);
        return true;
    }
}
