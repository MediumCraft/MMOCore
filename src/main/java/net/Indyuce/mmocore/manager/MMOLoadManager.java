package net.Indyuce.mmocore.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.JsonParseException;

import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.DefaultMMOLoader;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import io.lumine.mythic.lib.api.MMOLineConfig;

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

	/**
	 Returns a List of Experience Source as one experience source can be linked to others.
	 Loading one exp source can in fact oad multiples if they are linked
	 */
	@Deprecated
	public List<ExperienceSource<?>> loadExperienceSource(MMOLineConfig config, ExperienceDispenser dispenser) {
		return load(List.class, config, loader -> loader.loadExperienceSource(config, dispenser));
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

		for (MMOLoader loader : loaders)
			try {
				T found = func.apply(loader);
				if (found != null)
					return found;
			} catch (IllegalArgumentException | JsonParseException | IndexOutOfBoundsException exception) {
				throw new IllegalArgumentException(exception.getMessage());
			}

		throw new IllegalArgumentException("Could not match any " + c.getSimpleName() + " to '" + config.getKey() + "' in database");
	}
}
