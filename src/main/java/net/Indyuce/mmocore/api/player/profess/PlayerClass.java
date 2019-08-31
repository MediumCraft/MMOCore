package net.Indyuce.mmocore.api.player.profess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.MMOCoreUtils;
import net.Indyuce.mmocore.api.AltChar;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoadException;
import net.Indyuce.mmocore.api.math.formula.LinearValue;
import net.Indyuce.mmocore.api.math.particle.CastingParticle;
import net.Indyuce.mmocore.api.player.profess.event.EventTrigger;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.profess.resource.ResourceHandler;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.skill.Skill;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.Indyuce.mmocore.manager.ClassManager;

public class PlayerClass {
	private final String name, id, fileName;
	private final List<String> description = new ArrayList<>(), attrDescription = new ArrayList<>();
	private final ItemStack icon;
	private final Map<ClassOption, Boolean> options = new HashMap<>();
	private final ManaDisplayOptions manaDisplay;
	private final int maxLevel;

	private final Map<StatType, LinearValue> stats = new HashMap<>();
	private final Map<String, SkillInfo> skills = new LinkedHashMap<>();
	private final List<Subclass> subclasses = new ArrayList<>();

	private Map<String, EventTrigger> eventTriggers = new HashMap<>();

	private final Map<PlayerResource, ResourceHandler> resources = new HashMap<>();
	private CastingParticle castParticle = new CastingParticle(Particle.SPELL_INSTANT);

	/*
	 * easy load
	 */
	private FileConfiguration loaded;

	public PlayerClass(String id, FileConfiguration config) {
		this.id = (fileName = id).toUpperCase().replace("-", "_").replace(" ", "_");
		this.loaded = config;

		name = ChatColor.translateAlternateColorCodes('&', config.getString("display.name"));
		icon = MMOCoreUtils.readIcon(config.getString("display.item"));
		for (String string : config.getStringList("display.lore"))
			description.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', string));
		for (String string : config.getStringList("display.attribute-lore"))
			attrDescription.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', string));
		manaDisplay = new ManaDisplayOptions(config.getConfigurationSection("mana"));
		maxLevel = config.getInt("max-level");

		if (config.contains("attributes"))
			for (String key : config.getConfigurationSection("attributes").getKeys(false))
				try {
					stats.put(StatType.valueOf(key.toUpperCase().replace("-", "_")), new LinearValue(config.getConfigurationSection("attributes." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not load stat info '" + key + "': " + exception.getMessage());
				}

		if (config.contains("skills"))
			for (String key : config.getConfigurationSection("skills").getKeys(false))
				try {
					Validate.isTrue(MMOCore.plugin.skillManager.has(key), "Could not find skill " + key);
					skills.put(key, MMOCore.plugin.skillManager.get(key).newSkillInfo(config.getConfigurationSection("skills." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not load skill info '" + key + "': " + exception.getMessage());
				}

		if (config.contains("cast-particle"))
			try {
				castParticle = new CastingParticle(config.getConfigurationSection("cast-particle"));
			} catch (IllegalArgumentException exception) {
				MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not read casting particle, using default: " + exception.getMessage());
			}

		if (config.contains("options"))
			for (String key : config.getConfigurationSection("options").getKeys(false))
				try {
					setOption(ClassOption.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_")), config.getBoolean("options." + key));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] Could not read class option from '" + key + "'");
				}

		if (config.contains("main-exp-sources"))
			for (String key : config.getStringList("main-exp-sources"))
				try {
					ExperienceSource<?> source = MMOCore.plugin.loadManager.loadExperienceSource(new MMOLineConfig(key), null);
					source.setClass(this);
					MMOCore.plugin.professionManager.registerExpSource(source);
				} catch (MMOLoadException exception) {
					exception.printConsole("PlayerClasses:" + id, "exp source");
				}

		if (config.contains("triggers"))
			for (String key : config.getConfigurationSection("triggers").getKeys(false)) {
				try {
					String format = key.toLowerCase().replace("_", "-").replace(" ", "-");
					// Validate.isTrue(MMOCore.plugin.classManager.isEventRegistered(format),
					// "Could not find trigger event called '" + format + "'");
					eventTriggers.put(format, new EventTrigger(format, config.getStringList("triggers." + key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "[PlayerClasses:" + id + "] " + exception.getMessage());
					continue;
				}
			}

		for (PlayerResource resource : PlayerResource.values())
			resources.put(resource, new ResourceHandler(this, resource));
	}

	/*
	 * used to generate display class
	 */
	public PlayerClass(String id, String name, Material material) {
		this.id = id;
		this.name = name;
		this.fileName = id;
		manaDisplay = new ManaDisplayOptions(ChatColor.BLUE, "Mana", AltChar.listSquare.charAt(0));
		maxLevel = 0;

		this.icon = new ItemStack(material);
		setOption(ClassOption.DISPLAY, false);
		setOption(ClassOption.DEFAULT, false);

		for (PlayerResource resource : PlayerResource.values())
			resources.put(resource, new ResourceHandler(this, resource));
	}

	public void loadSubclasses(ClassManager manager) {
		if (loaded.contains("subclasses"))
			for (String key : loaded.getConfigurationSection("subclasses").getKeys(false))
				subclasses.add(new Subclass(manager.get(key.toUpperCase().replace("-", "_").replace(" ", "_")), loaded.getInt("subclasses." + key)));
		loaded = null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ManaDisplayOptions getManaDisplay() {
		return manaDisplay;
	}

	public ResourceHandler getHandler(PlayerResource resource) {
		return resources.get(resource);
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public String getFileName() {
		return fileName;
	}

	public ItemStack getIcon() {
		return icon.clone();
	}

	public CastingParticle getCastParticle() {
		return castParticle;
	}

	public List<String> getDescription() {
		return description;
	}

	public List<String> getAttributeDescription() {
		return attrDescription;
	}

	public void setOption(ClassOption option, boolean value) {
		options.put(option, value);
	}

	public boolean hasOption(ClassOption option) {
		return options.containsKey(option) ? options.get(option) : option.getDefault();
	}

	public void setStat(StatType type, double base, double perLevel) {
		stats.put(type, new LinearValue(base, perLevel));
	}

	public double calculateStat(StatType stat, int level) {
		return getStatInfo(stat).calculate(level);
	}

	public List<Subclass> getSubclasses() {
		return subclasses;
	}

	public boolean hasSkill(Skill skill) {
		return skills.containsKey(skill.getId());
	}

	public SkillInfo getSkill(Skill skill) {
		return getSkill(skill.getId());
	}

	public SkillInfo getSkill(String id) {
		return skills.get(id);
	}

	public Set<String> getEventTriggers() {
		return eventTriggers.keySet();
	}

	public boolean hasEventTriggers(String name) {
		return eventTriggers.containsKey(name);
	}

	public EventTrigger getEventTriggers(String name) {
		return eventTriggers.get(name);
	}

	public Collection<SkillInfo> getSkills() {
		return skills.values();
	}

	private LinearValue getStatInfo(StatType type) {
		return stats.containsKey(type) ? stats.get(type) : type.getDefault();
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof PlayerClass && ((PlayerClass) obj).id.equals(id);
	}

	public class Subclass {
		private PlayerClass profess;
		private int level;

		public Subclass(PlayerClass profess, int level) {
			this.profess = profess;
			this.level = level;
		}

		public PlayerClass getProfess() {
			return profess;
		}

		public int getLevel() {
			return level;
		}
	}

	public enum ClassOption {

		/*
		 * is class by default
		 */
		DEFAULT,

		/*
		 * displays in the /class GUI
		 */
		DISPLAY(true),

		/*
		 * resource regeneration depends on max resource
		 */
		MISSING_HEALTH_REGEN,
		MISSING_MANA_REGEN,
		MISSING_STAMINA_REGEN,

		/*
		 * resource regeneration depends on missing resource
		 */
		MAX_HEALTH_REGEN,
		MAX_MANA_REGEN,
		MAX_STAMINA_REGEN,

		/*
		 * only regen resource when out of combat
		 */
		OFF_COMBAT_HEALTH_REGEN,
		OFF_COMBAT_MANA_REGEN(true),
		OFF_COMBAT_STAMINA_REGEN;

		private final boolean def;

		private ClassOption() {
			this(false);
		}

		private ClassOption(boolean def) {
			this.def = def;
		}

		public boolean getDefault() {
			return def;
		}

		public String getPath() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
