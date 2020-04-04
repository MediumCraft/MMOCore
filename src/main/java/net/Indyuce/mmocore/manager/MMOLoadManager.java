package net.Indyuce.mmocore.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.JsonParseException;

import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.DefaultMMOLoader;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class MMOLoadManager {
	private final List<MMOLoader> loaders = new ArrayList<>();

	public MMOLoadManager() {
		loaders.add(new DefaultMMOLoader());
	}

	public void registerLoader(MMOLoader loader) {
		Validate.notNull(loader, "Loader must not be null");

		loaders.add(loader);
	}

	public Condition loadCondition(MMOLineConfig config) {
		return load(Condition.class, config, loader -> loader.loadCondition(config));
	}

	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {
		return load(Objective.class, config, loader -> loader.loadObjective(config, section));
	}

	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {
		return load(ExperienceSource.class, config, loader -> loader.loadExperienceSource(config, profession));
	}

	public Trigger loadTrigger(MMOLineConfig config) {
		return load(Trigger.class, config, loader -> loader.loadTrigger(config));
	}

	public DropItem loadDropItem(MMOLineConfig config) {
		return load(DropItem.class, config, loader -> loader.loadDropItem(config));
	}

	public BlockType loadBlockType(MMOLineConfig config) {
		return load(BlockType.class, config, loader -> loader.loadBlockType(config));
	}

	private <T> T load(Class<T> c, MMOLineConfig config, Function<MMOLoader, T> func) {

		for (MMOLoader loader : loaders) {

			try {
				T found = func.apply(loader);
				if (found != null)
					return found;
			} catch (IllegalArgumentException | JsonParseException | IndexOutOfBoundsException exception) {
				throw new MMOLoadException("Could not load '" + config.toString() + "': " + exception.getMessage());
			}
		}

		throw new MMOLoadException("Could not load '" + config.toString() + "': Could not find corresponding " + c.getSimpleName() + " in database");
	}
}
