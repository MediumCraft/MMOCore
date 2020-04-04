package net.Indyuce.mmocore.api.quest.objective;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.quest.ObjectiveProgress;
import net.Indyuce.mmocore.api.quest.QuestProgress;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public abstract class Objective {
	private final String id, lore;

	private BarColor barColor;
	private final List<Trigger> triggers = new ArrayList<>();

	public Objective(ConfigurationSection config) {
		this.id = config.getName();
		this.lore = config.getString("lore");

		Validate.notNull(lore, "Could not find objective lore");
		Validate.notNull(config.getStringList("triggers"), "Could not load trigger list");

		try {
			String format = config.getString("bar-color");
			Validate.notNull(format);
			barColor = BarColor.valueOf(format.toUpperCase().replace("-", "_").replace(" ", "_"));
		} catch (IllegalArgumentException exeption) {
			barColor = BarColor.PURPLE;
		}

		for (String key : config.getStringList("triggers"))
			try {
				triggers.add(MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(key)));
			} catch (MMOLoadException exception) {
				exception.printConsole("Objectives:" + id, "trigger");
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
