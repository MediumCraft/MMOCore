package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.experience.ExpCurve;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ExperienceManager implements MMOCoreManager {
    private final Map<String, ExpCurve> expCurves = new HashMap<>();
    private final Map<String, ExperienceTable> expTables = new HashMap<>();

    /**
     * Experience sources from the exp-sources.yml config file where you can
     * input any exp source which can later be used along with the 'from'
     * exp source anywhere in the plugin.
     * <p>
     * TODO First needs to edit the exp-source current structure. This is going to break a lot of things
     *
     * @deprecated See TODO
     */
    @Deprecated
    private final Map<String, List<ExperienceSource<?>>> publicExpSources = new HashMap<>();

    /**
     * Saves different experience sources based on experience source type.
     */
    private final Map<Class<?>, ExperienceSourceManager<?>> managers = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends ExperienceSource> ExperienceSourceManager<T> getManager(Class<T> t) {
        return (ExperienceSourceManager<T>) managers.get(t);
    }

    @SuppressWarnings("unchecked")
    public <T extends ExperienceSource> void registerSource(T source) {
        final Class<T> path = (Class<T>) source.getClass();
        managers.computeIfAbsent(path, unused -> source.newManager());
        getManager(path).registerSource(source);
    }

    public boolean hasCurve(String id) {
        return expCurves.containsKey(id);
    }

    public ExpCurve getCurveOrThrow(String id) {
        Validate.isTrue(hasCurve(id), "Could not find exp curve with ID '" + id + "'");
        return expCurves.get(id);
    }

    @Deprecated
    @Nullable
    public List<ExperienceSource<?>> getExperienceSourceList(String key) {
        return publicExpSources.get(key);
    }

    public boolean hasTable(String id) {
        return expTables.containsKey(id);
    }

    public ExperienceTable getTableOrThrow(String id) {
        return Objects.requireNonNull(expTables.get(id), "Could not find exp table with ID '" + id + "'");
    }

    public ExperienceTable loadExperienceTable(Object obj) {

        if (obj instanceof ConfigurationSection)
            return new ExperienceTable((ConfigurationSection) obj);

        if (obj instanceof String)
            return MMOCore.plugin.experience.getTableOrThrow(obj.toString());

        throw new IllegalArgumentException("Please provide either a string (exp table name) or a config section (locally define an exp table)");
    }

    public Collection<ExpCurve> getCurves() {
        return expCurves.values();
    }

    public Collection<ExperienceTable> getTables() {
        return expTables.values();
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            expCurves.clear();
            expTables.clear();

            managers.values().forEach(HandlerList::unregisterAll);
            managers.clear();
        }

        // Exp curves
        for (File file : new File(MMOCore.plugin.getDataFolder() + "/expcurves").listFiles())
            try {
                ExpCurve curve = new ExpCurve(file);
                expCurves.put(curve.getId(), curve);
            } catch (IllegalArgumentException | IOException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load exp curve '" + file.getName() + "': " + exception.getMessage());
            }

        // Exp tables
        FileConfiguration expTablesConfig = new ConfigFile("exp-tables").getConfig();
        for (String key : expTablesConfig.getKeys(false))
            try {
                ExperienceTable table = new ExperienceTable(expTablesConfig.getConfigurationSection(key));
                expTables.put(table.getId(), table);
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load exp table '" + key + "': " + exception.getMessage());
            }
    }
}
