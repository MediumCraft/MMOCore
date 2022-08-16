package net.Indyuce.mmocore;

import net.Indyuce.mmocore.listener.*;
import net.Indyuce.mmocore.listener.event.PlayerPressKeyListener;
import net.Indyuce.mmocore.listener.option.*;
import net.Indyuce.mmocore.listener.profession.FishingListener;
import net.Indyuce.mmocore.listener.profession.PlayerCollectStats;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class MMOCoreBukkit {

    /**
     * Called when MMOCore enables. This registers
     * all the listeners required for MMOCore to run
     */
    public MMOCoreBukkit(MMOCore plugin) {
        if (plugin.configManager.overrideVanillaExp = plugin.getConfig().getBoolean("override-vanilla-exp"))
            Bukkit.getPluginManager().registerEvents(new VanillaExperienceOverride(), plugin);

        if (plugin.getConfig().getBoolean("hotbar-swapping.enabled"))
            try {
                Bukkit.getPluginManager().registerEvents(new HotbarSwap(plugin.getConfig().getConfigurationSection("hotbar-swapping")), plugin);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Could not load hotbar swapping: " + exception.getMessage());
            }

        if (plugin.getConfig().getBoolean("prevent-spawner-xp"))
            Bukkit.getPluginManager().registerEvents(new NoSpawnerEXP(), plugin);

        if (plugin.getConfig().getBoolean("death-exp-loss.enabled"))
            Bukkit.getPluginManager().registerEvents(new DeathExperienceLoss(), plugin);

        if (plugin.getConfig().getBoolean("shift-click-player-profile-check"))
            Bukkit.getPluginManager().registerEvents(new PlayerProfileCheck(), plugin);

        if (plugin.getConfig().getBoolean("vanilla-exp-redirection.enabled"))
            Bukkit.getPluginManager().registerEvents(new RedirectVanillaExp(plugin.getConfig().getDouble("vanilla-exp-redirection.ratio")), plugin);

        Bukkit.getPluginManager().registerEvents(new WaypointsListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new GoldPouchesListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new LootableChestsListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new GuildListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new FishingListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerCollectStats(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerPressKeyListener(), plugin);
        // Bukkit.getPluginManager().registerEvents(new ClassTriggers(), plugin);
    }
}
