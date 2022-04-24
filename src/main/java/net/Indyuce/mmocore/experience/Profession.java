package net.Indyuce.mmocore.experience;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.PostLoadObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class Profession extends PostLoadObject implements ExperienceObject {
    private final String id, name;
    private final int maxLevel;
    private final Map<ProfessionOption, Boolean> options = new HashMap<>();
    private final ExpCurve expCurve;
    private final ExperienceTable expTable;

    /**
     * Experience given to the main player level
     * whenever he levels up this profession
     */
    private final LinearValue experience;

    public Profession(String id, FileConfiguration config) {
        super(config);

        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = config.getString("name");
        Validate.notNull(name, "Could not load name");

        expCurve = config.contains("exp-curve")
                ? MMOCore.plugin.experience.getCurveOrThrow(config.get("exp-curve").toString().toLowerCase().replace("_", "-").replace(" ", "-"))
                : ExpCurve.DEFAULT;
        experience = new LinearValue(config.getConfigurationSection("experience"));

        ExperienceTable expTable = null;
        if (config.contains("exp-table"))
            try {
                expTable = MMOCore.plugin.experience.loadExperienceTable(config.get("exp-table"));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load exp table from profession '" + id + "': " + exception.getMessage());
            }
        this.expTable = expTable;

        if (config.contains("options"))
            for (String key : config.getConfigurationSection("options").getKeys(false))
                try {
                    ProfessionOption option = ProfessionOption.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
                    options.put(option, config.getBoolean("options." + key));
                } catch (IllegalArgumentException exception) {
                    MMOCore.plugin.getLogger().log(Level.WARNING,
                            "Could not load option '" + key + "' from profession '" + id + "': " + exception.getMessage());
                }

        maxLevel = config.getInt("max-level");

        if (config.contains("exp-sources")) {
            for (String key : config.getStringList("exp-sources"))
                try {
                    MMOCore.plugin.experience.registerSource(MMOCore.plugin.loadManager.loadExperienceSource(new MMOLineConfig(key), this));
                } catch (IllegalArgumentException exception) {
                    MMOCore.plugin.getLogger().log(Level.WARNING,
                            "Could not register exp source '" + key + "' from profession '" + id + "': " + exception.getMessage());
                }
        }
    }

    @Override
    protected void whenPostLoaded(ConfigurationSection configurationSection) {
        MMOCore.plugin.professionManager.loadProfessionConfigurations(this, configurationSection);
    }

    public boolean getOption(ProfessionOption option) {
        return options.getOrDefault(option, option.getDefault());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return "profession." + getId();
    }

    @Override
    public ExpCurve getExpCurve() {
        return expCurve;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean hasMaxLevel() {
        return maxLevel > 0;
    }

    public LinearValue getExperience() {
        return experience;
    }

    @Override
    public boolean hasExperienceTable() {
        return expTable != null;
    }

    @NotNull
    public ExperienceTable getExperienceTable() {
        return Objects.requireNonNull(expTable, "Profession has no exp table");
    }

    @Override
    public void giveExperience(PlayerData playerData, double experience, @Nullable Location hologramLocation, EXPSource source) {
        hologramLocation = !getOption(Profession.ProfessionOption.EXP_HOLOGRAMS) ? null
                : hologramLocation == null ? getPlayerLocation(playerData) : hologramLocation;
        playerData.getCollectionSkills().giveExperience(this, experience, EXPSource.SOURCE, hologramLocation);
    }

    @Override
    public boolean shouldHandle(PlayerData playerData) {
        return true;
    }

    public static enum ProfessionOption {

        /**
         * When disabled, removes exp holograms when mined
         */
        EXP_HOLOGRAMS(true);

        private final boolean def;

        private ProfessionOption(boolean def) {
            this.def = def;
        }

        public boolean getDefault() {
            return def;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profession that = (Profession) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
