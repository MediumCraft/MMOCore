package net.Indyuce.mmocore.api.experience;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.PostLoadObject;

public class Profession extends PostLoadObject {
	private final String id, name;
	private final ExpCurve expCurve;
	private final int maxLevel;
	private final Map<ProfessionOption, Boolean> options = new HashMap<>();

	/*
	 * experience given to the main player level whenever he levels up this
	 * profession
	 */
	private final LinearValue experience;

	public Profession(String id, FileConfiguration config) {
		super(config);

		this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
		this.name = config.getString("name");
		Validate.notNull(name, "Could not load name");

		expCurve = config.contains("exp-curve")
				? MMOCore.plugin.experience.getOrThrow(config.get("exp-curve").toString().toLowerCase().replace("_", "-").replace(" ", "-"))
				: ExpCurve.DEFAULT;
		experience = new LinearValue(config.getConfigurationSection("experience"));

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

		if (config.contains("exp-sources"))
			for (String key : config.getStringList("exp-sources"))
				try {
					MMOCore.plugin.professionManager.registerExpSource(MMOCore.plugin.loadManager.loadExperienceSource(new MMOLineConfig(key), this));
				} catch (IllegalArgumentException exception) {
					MMOCore.plugin.getLogger().log(Level.WARNING,
							"Could not register exp source '" + key + "' from profession '" + id + "': " + exception.getMessage());
				}
	}

	/*
	 * drop tables must be loaded after professions are initialized
	 */
	@Override
	protected void whenPostLoaded(ConfigurationSection config) {

		if (config.contains("on-fish"))
			MMOCore.plugin.fishingManager.loadDropTables(config.getConfigurationSection("on-fish"));

		if (config.contains("on-mine"))
			MMOCore.plugin.mineManager.loadDropTables(config.getConfigurationSection("on-mine"));

		if (config.contains("alchemy-experience")) {

			MMOCore.plugin.alchemyManager.splash = 1 + config.getDouble("alchemy-experience.special.splash") / 100;
			MMOCore.plugin.alchemyManager.lingering = 1 + config.getDouble("alchemy-experience.special.lingering") / 100;
			MMOCore.plugin.alchemyManager.extend = 1 + config.getDouble("alchemy-experience.special.extend") / 100;
			MMOCore.plugin.alchemyManager.upgrade = 1 + config.getDouble("alchemy-experience.special.upgrade") / 100;

			for (String key : config.getConfigurationSection("alchemy-experience.effects").getKeys(false))
				try {
					PotionType type = PotionType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					MMOCore.plugin.alchemyManager.registerBaseExperience(type, config.getDouble("alchemy-experience.effects." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerProfessions:" + id + "] Could not read potion type from " + key);
				}
		}

		if (config.contains("base-enchant-exp"))
			for (String key : config.getConfigurationSection("base-enchant-exp").getKeys(false))
				try {
					Enchantment enchant = MythicLib.plugin.getVersion().getWrapper().getEnchantmentFromString(key.toLowerCase().replace("-", "_"));
					MMOCore.plugin.enchantManager.registerBaseExperience(enchant, config.getDouble("base-enchant-exp." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerProfessions:" + id + "] Could not read enchant from " + key);
				}

		if (config.contains("repair-exp"))
			for (String key : config.getConfigurationSection("repair-exp").getKeys(false))
				try {
					Material material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					MMOCore.plugin.smithingManager.registerBaseExperience(material, config.getDouble("repair-exp." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerProfessions:" + id + "] Could not read material from " + key);
				}

		// if (config.contains("effect-weight"))
		// for (String key :
		// config.getConfigurationSection("effect-weight").getKeys(false))
		// try {
		// MMOCore.plugin.alchemyManager.registerEffectWeight(PotionEffectType.getByName(key.toUpperCase().replace("-",
		// "_").replace(" ", "_")), config.getDouble("effect-weight." + key));
		// } catch (IllegalArgumentException exception) {
		// MMOCore.log(Level.WARNING, "[PlayerProfessions:" + id + "] Could not
		// read
		// potion effect type from " + key);
		// }
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

	public ExpCurve getExpCurve() {
		return expCurve;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public boolean hasMaxLevel() {
		return maxLevel > 0;
	}

	public int calculateExperience(int x) {
		return (int) experience.calculate(x);
	}

	public LinearValue getExperience() {
		return experience;
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
}
