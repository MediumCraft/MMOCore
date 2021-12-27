package net.Indyuce.mmocore.manager.profession;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ProfessionManager implements MMOCoreManager {
	private final Map<String, Profession> professions = new HashMap<>();
	private final Set<SpecificProfessionManager> professionManagers = new HashSet<>();

	/**
	 * If it has been loaded at least once
	 */
	private boolean loadedOnce;

	public void register(Profession profession) {
		professions.put(profession.getId(), profession);
	}

	public void registerProfessionManager(@NotNull SpecificProfessionManager professionManager) {
		Validate.notNull(professionManager);

		professionManagers.add(professionManager);
	}

	/**
	 * @param profession Profession loading some configuration section
	 * @param config     Configuration section to load profession config from
	 */
	public void loadProfessionConfigurations(Profession profession, ConfigurationSection config) {
		for (SpecificProfessionManager manager : professionManagers)
			if (config.contains(manager.getStringKey()))
				try {
					manager.setLinkedProfession(profession);
					manager.loadProfessionConfiguration(config.getConfigurationSection(manager.getStringKey()));
				} catch (RuntimeException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load profession config '" + manager.getStringKey() + "': " + exception.getMessage());
				}
	}

	public Profession get(String id) {
		return professions.get(id);
	}

	public boolean has(String id) {
		return professions.containsKey(id);
	}

	public Collection<Profession> getAll() {
		return professions.values();
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			professions.clear();

		// Load default profession managers (can't be done on constructor because MMOCore.plugin is null)
		if (!loadedOnce) {
			registerProfessionManager(MMOCore.plugin.alchemyManager);
			registerProfessionManager(MMOCore.plugin.enchantManager);
			registerProfessionManager(MMOCore.plugin.fishingManager);
			registerProfessionManager(MMOCore.plugin.smithingManager);
			loadedOnce = true;
		}

		professionManagers.forEach(manager -> manager.initialize(clearBefore));

		for (File file : new File(MMOCore.plugin.getDataFolder() + "/professions").listFiles())
			try {
				String id = file.getName().substring(0, file.getName().length() - 4);
				register(new Profession(id, YamlConfiguration.loadConfiguration(file)));
			} catch (IllegalArgumentException exception) {
				MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load profession " + file.getName() + ": " + exception.getMessage());
			}
	}
}
