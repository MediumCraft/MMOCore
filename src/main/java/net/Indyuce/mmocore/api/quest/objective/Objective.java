package net.Indyuce.mmocore.api.quest.objective;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import io.lumine.mythic.lib.api.MMOLineConfig;

public abstract class Objective {
	private final String id, lore;

	private final BarColor barColor;
	private final List<Trigger> triggers = new ArrayList<>();

	public Objective(ConfigurationSection config) {
		this.id = config.getName();
		this.lore = config.getString("lore");

		Validate.notNull(lore, "Could not find objective lore");
		Validate.notNull(config.getStringList("triggers"), "Could not load trigger list");

		String format = config.getString("bar-color", "PURPLE");
		barColor = BarColor.valueOf(format.toUpperCase().replace("-", "_").replace(" ", "_"));

		for (String key : config.getStringList("triggers"))
			try {
				triggers.add(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(key)));
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING,
						"Could not load trigger '" + key + "' from objective '" + id + "': " + exception.getMessage());
			}
	}

	public String getId() {
		return id;
	}

	public BarColor getBarColor() {
		return barColor;
	}

	public String getDefaultLore() {
		return lore;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public abstract ObjectiveProgress newProgress(QuestProgress questProgress);
}
