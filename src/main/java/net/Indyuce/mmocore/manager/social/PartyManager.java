package net.Indyuce.mmocore.manager.social;

import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class PartyManager implements MMOCoreManager {
    private final Set<StatModifier> buffs = new HashSet<>();

    public Set<StatModifier> getBonuses() {
        return buffs;
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            buffs.clear();

        ConfigurationSection config = MMOCore.plugin.getConfig().getConfigurationSection("party.buff");
        if (config != null)
            for (String key : config.getKeys(false))
                try {
                    StatType stat = StatType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
                    buffs.add(new StatModifier("mmocoreParty", stat.name(), config.getString(key)));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load party buff '" + key + "': " + exception.getMessage());
                }
    }
}
