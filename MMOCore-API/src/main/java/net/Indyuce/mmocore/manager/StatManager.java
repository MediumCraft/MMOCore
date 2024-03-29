package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.api.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StatManager implements MMOCoreManager {
    private final Map<String, StatInfo> loaded = new HashMap<>();
    private final Set<String> usedStats = new HashSet<>();

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            loaded.clear();
            usedStats.clear();
        }

        // Read default formulas
        FileConfiguration config = new ConfigFile("stats").getConfig();
        for (String key : config.getConfigurationSection("default").getKeys(false))
            registerDefaultFormula(key, new LinearValue(config.getConfigurationSection("default." + key)));
    }

    public Collection<StatInfo> getLoaded() {
        return loaded.values();
    }

    /**
     * Keeps track of all the item stats used so far in the plugin.
     * It is not a constant since users can freely add or even create
     * new stats. But this is a working approximation for MMOCore only.
     * <p>
     * These stats appear at least once:
     * - in a class definition
     * - in stats.yml which defines default stat formulas
     *
     * @return A list of stats that must be taken into account in MMOCore
     *         in the player stat calculation.
     */
    @NotNull
    public Set<String> getRegistered() {
        return usedStats;
    }

    @Nullable
    public StatInfo getInfo(String stat) {
        return loaded.get(stat);
    }

    public void registerProfession(String stat, Profession profession) {
        compute(stat).profession = profession;
    }

    public void registerDefaultFormula(String stat, LinearValue defaultFormula) {
        compute(stat).defaultInfo = defaultFormula;
        usedStats.add(stat);
    }

    /**
     * @return A stat info for the specified stat. If it doesn't exist
     *         when method is called, it is registered into the map
     */
    @NotNull
    private StatInfo compute(String stat) {
        return loaded.computeIfAbsent(stat, StatInfo::new);
    }
}
