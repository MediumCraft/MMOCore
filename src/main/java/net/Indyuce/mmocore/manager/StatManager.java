package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatManager implements MMOCoreManager {
    private final Map<String, StatInfo> loaded = new HashMap<>();

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            loaded.clear();

        FileConfiguration config = new ConfigFile("stats").getConfig();

        // Read decimal formats
        for (String key : config.getConfigurationSection("decimal-format").getKeys(false))
            registerDecimalFormat(key, MythicLib.plugin.getMMOConfig().newDecimalFormat(config.getString("decimal-format." + key)));

        // Read default formulas
        for (String key : config.getConfigurationSection("default").getKeys(false))
            registerDefaultFormula(key, new LinearValue(config.getConfigurationSection("default." + key)));
    }

    public Collection<StatInfo> getLoaded() {
        return loaded.values();
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
    }

    public void registerDecimalFormat(String stat, DecimalFormat format) {
        compute(stat).format = format;
    }

    /**
     * @return A stat info for the specified stat. If it doesn't
     *         exist when method is called, it is registered into the map
     */
    @NotNull
    private StatInfo compute(String stat) {
        StatInfo found = loaded.get(stat);
        if (found != null)
            return found;

        StatInfo newInfo = new StatInfo(stat);
        loaded.put(stat, newInfo);
        return newInfo;
    }
}
