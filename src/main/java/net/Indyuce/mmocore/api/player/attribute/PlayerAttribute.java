package net.Indyuce.mmocore.api.player.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.mmogroup.mmolib.api.stat.modifier.StatModifier;

public class PlayerAttribute {
	private final String id, name;
	private final int max;
	private final Map<StatType, StatModifier> buffs = new HashMap<>();

	public PlayerAttribute(ConfigurationSection config) {
		Validate.notNull(config, "Could not load config");
		id = config.getName().toLowerCase().replace("_", "-").replace(" ", "-");

		name = config.getString("name");
		Validate.isTrue(name != null && !name.isEmpty(), "Could not read name");

		max = config.contains("max-points") ? Math.max(1, config.getInt("max-points")) : 0;

		if (config.contains("buff"))
			for (String key : config.getConfigurationSection("buff").getKeys(false))
				try {
					StatType stat = StatType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					buffs.put(stat, new StatModifier(config.getString("buff." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerAttributes:" + id + "] Could not load buff '" + key + "': " + exception.getMessage());
				}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return ChatColor.translateAlternateColorCodes('&', name);
	}

	public boolean hasMax() {
		return max > 0;
	}

	public int getMax() {
		return max;
	}

	public Set<StatType> getStats() {
		return buffs.keySet();
	}

	public StatModifier getBuff(StatType stat) {
		return buffs.get(stat);
	}
}
