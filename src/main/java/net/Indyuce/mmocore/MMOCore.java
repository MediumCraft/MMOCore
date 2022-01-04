package net.Indyuce.mmocore;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.Metrics;
import io.lumine.mythic.lib.version.SpigotPlugin;
import io.lumine.mythic.utils.plugin.LuminePlugin;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.PlayerActionBar;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.debug.DebugMode;
import net.Indyuce.mmocore.command.*;
import net.Indyuce.mmocore.comp.MMOCoreTargetRestriction;
import net.Indyuce.mmocore.comp.citizens.CitizenInteractEventListener;
import net.Indyuce.mmocore.comp.citizens.CitizensMMOLoader;
import net.Indyuce.mmocore.comp.mythicmobs.MythicHook;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsMMOLoader;
import net.Indyuce.mmocore.comp.placeholder.DefaultParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderAPIParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderParser;
import net.Indyuce.mmocore.comp.region.DefaultRegionHandler;
import net.Indyuce.mmocore.comp.region.RegionHandler;
import net.Indyuce.mmocore.comp.region.WorldGuardMMOLoader;
import net.Indyuce.mmocore.comp.region.WorldGuardRegionHandler;
import net.Indyuce.mmocore.comp.vault.VaultEconomy;
import net.Indyuce.mmocore.comp.vault.VaultMMOLoader;
import net.Indyuce.mmocore.listener.*;
import net.Indyuce.mmocore.listener.option.*;
import net.Indyuce.mmocore.listener.profession.FishingListener;
import net.Indyuce.mmocore.listener.profession.PlayerCollectStats;
import net.Indyuce.mmocore.loot.chest.LootChest;
import net.Indyuce.mmocore.manager.*;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.mysql.MySQLDataProvider;
import net.Indyuce.mmocore.manager.data.yaml.YAMLDataProvider;
import net.Indyuce.mmocore.manager.profession.*;
import net.Indyuce.mmocore.manager.social.BoosterManager;
import net.Indyuce.mmocore.manager.social.PartyManager;
import net.Indyuce.mmocore.manager.social.RequestManager;
import net.Indyuce.mmocore.skill.list.Ambers;
import net.Indyuce.mmocore.skill.list.Neptune_Gift;
import net.Indyuce.mmocore.skill.list.Sneaky_Picky;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.logging.Level;

public class MMOCore extends LuminePlugin {
	public static MMOCore plugin;

	public ConfigManager configManager;
	public WaypointManager waypointManager;
	public SoundManager soundManager;
	public RequestManager requestManager;
	public ConfigItemManager configItems;
	public final PlayerActionBar actionBarManager = new PlayerActionBar();
	public final SkillManager skillManager = new SkillManager();
	public final ClassManager classManager = new ClassManager();
	public final DropTableManager dropTableManager = new DropTableManager();
	public final BoosterManager boosterManager = new BoosterManager();
	public final AttributeManager attributeManager = new AttributeManager();
	public final PartyManager partyManager = new PartyManager();
	public final QuestManager questManager = new QuestManager();
	public final ProfessionManager professionManager = new ProfessionManager();
	public final net.Indyuce.mmocore.manager.ExperienceManager experience = new net.Indyuce.mmocore.manager.ExperienceManager();
	public final LootChestManager lootChests = new LootChestManager();
	public final MMOLoadManager loadManager = new MMOLoadManager();
	public final RestrictionManager restrictionManager = new RestrictionManager();

	public VaultEconomy economy;
	public RegionHandler regionHandler = new DefaultRegionHandler();
	public PlaceholderParser placeholderParser = new DefaultParser();
	public DataProvider dataProvider = new YAMLDataProvider();

	// Profession managers
	public final net.Indyuce.mmocore.manager.profession.CustomBlockManager mineManager = new net.Indyuce.mmocore.manager.profession.CustomBlockManager();
	public final FishingManager fishingManager = new FishingManager();
	public final AlchemyManager alchemyManager = new AlchemyManager();
	public final EnchantManager enchantManager = new EnchantManager();
	public final SmithingManager smithingManager = new SmithingManager();

	public boolean shouldDebugSQL = false;

	private static final int MYTHICLIB_COMPATIBILITY_INDEX = 2;

	public MMOCore() {
		plugin = this;
	}

	public void load() {

		// Check if the ML build matches
		if (MYTHICLIB_COMPATIBILITY_INDEX != MythicLib.MMOCORE_COMPATIBILITY_INDEX) {
			getLogger().log(Level.WARNING, "Your versions of MythicLib and MMOCore do not match. Make sure you are using the latest builds of both plugins");
			disable();
			return;
		}

		// Register target restrictions due to MMOCore in MythicLib
		MythicLib.plugin.getEntities().registerRestriction(new MMOCoreTargetRestriction());

		// Register extra objective, drop items...
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
			loadManager.registerLoader(new WorldGuardMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("Citizens") != null)
			loadManager.registerLoader(new CitizensMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("Vault") != null) loadManager.registerLoader(new VaultMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
			loadManager.registerLoader(new MythicMobsMMOLoader());

		// Register MMOCore specific skills
		MythicLib.plugin.getSkills().registerSkillHandler(new Ambers());
		MythicLib.plugin.getSkills().registerSkillHandler(new Neptune_Gift());
		MythicLib.plugin.getSkills().registerSkillHandler(new Sneaky_Picky());
	}

	public void enable() {
		new SpigotPlugin(70575, this).checkForUpdate();
		new Metrics(this);
		saveDefaultConfig();

		final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
		final int defConfigVersion = getConfig().getDefaults().getInt("config-version");
		if (configVersion != defConfigVersion) {
			getLogger().warning("You may be using an outdated config.yml!");
			getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '" + defConfigVersion + "')");
		}

		if (getConfig().isConfigurationSection("mysql") && getConfig().getBoolean("mysql.enabled"))
			dataProvider = new MySQLDataProvider(getConfig());
		shouldDebugSQL = getConfig().getBoolean("mysql.debug");

		if (getConfig().isConfigurationSection("default-playerdata"))
			dataProvider.getDataManager().loadDefaultData(getConfig().getConfigurationSection("default-playerdata"));

		if (Bukkit.getPluginManager().getPlugin("Vault") != null) economy = new VaultEconomy();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			placeholderParser = new PlaceholderAPIParser();
			getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
		}

		if (Bukkit.getPluginManager().getPlugin("Citizens") != null) {
			Bukkit.getPluginManager().registerEvents(new CitizenInteractEventListener(), this);
			getLogger().log(Level.INFO, "Hooked onto Citizens");
		}

		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
			regionHandler = new WorldGuardRegionHandler();
			getLogger().log(Level.INFO, "Hooked onto WorldGuard");
		}

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			Bukkit.getServer().getPluginManager().registerEvents(new MythicHook(), this);
			MMOCore.plugin.getLogger().log(Level.INFO, "Hooked onto MythicMobs");
		}

		/*
		 * Resource regeneration. Must check if entity is dead otherwise regen will make
		 * the 'respawn' button glitched plus HURT entity effect bug
		 */
		new BukkitRunnable() {
			public void run() {
				for (PlayerData player : PlayerData.getAll())
					if (player.isOnline() && !player.getPlayer().isDead())
						for (PlayerResource resource : PlayerResource.values()) {
							double regenAmount = player.getProfess().getHandler(resource).getRegen(player);
							if (regenAmount != 0)
								resource.regen(player, regenAmount);
						}
			}
		}.runTaskTimer(MMOCore.plugin, 100, 20);

		/*
		 * Clean active loot chests every 5 minutes. Cannot register this runnable in
		 * the loot chest manager because it is instanced when the plugin loads
		 */
		new BukkitRunnable() {
			public void run() {
				for (LootChest chest : new HashSet<>(lootChests.getActive()))
					if (chest.shouldExpire()) chest.unregister(false);
			}
		}.runTaskTimer(this, 5 * 60 * 20, 5 * 60 * 20);

		/*
		 * For the sake of the lord, make sure they aren't using MMOItems Mana and
		 * Stamina Addon...This should prevent a couple error reports produced by people
		 * not reading the installation guide...
		 */
		if (Bukkit.getPluginManager().getPlugin("MMOMana") != null) {
			getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "MMOCore is not meant to be used with MMOItems ManaAndStamina");
			getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Please read the installation guide!");
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "[MMOCore] MMOCore is not meant to be used with MMOItems ManaAndStamina");
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "[MMOCore] Please read the installation guide!");
			return;
		}

		initializePlugin(false);

		if (getConfig().getBoolean("vanilla-exp-redirection.enabled"))
			Bukkit.getPluginManager().registerEvents(new RedirectVanillaExp(getConfig().getDouble("vanilla-exp-redirection.ratio")), this);

		/*
		 * enable debug mode for extra debug tools.
		 */
		if (getConfig().contains("debug")) {
			DebugMode.setLevel(getConfig().getInt("debug", 0));
			DebugMode.enableActionBar();
		}

		if (configManager.overrideVanillaExp = getConfig().getBoolean("override-vanilla-exp"))
			Bukkit.getPluginManager().registerEvents(new VanillaExperienceOverride(), this);

		if (getConfig().getBoolean("prevent-spawner-xp"))
			Bukkit.getPluginManager().registerEvents(new NoSpawnerEXP(), this);

		if (getConfig().getBoolean("death-exp-loss.enabled"))
			Bukkit.getPluginManager().registerEvents(new DeathExperienceLoss(), this);

		if (getConfig().getBoolean("shift-click-player-profile-check"))
			Bukkit.getPluginManager().registerEvents(new PlayerProfileCheck(), this);

		Bukkit.getPluginManager().registerEvents(new WaypointsListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new GoldPouchesListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new LootableChestsListener(), this);
		Bukkit.getPluginManager().registerEvents(new SpellCast(), this);
		Bukkit.getPluginManager().registerEvents(new PartyListener(), this);
		Bukkit.getPluginManager().registerEvents(new GuildListener(), this);
		Bukkit.getPluginManager().registerEvents(new FishingListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerCollectStats(), this);

		/*
		 * Initialize player data from all online players. This is very important to do
		 * that after registering all the professses otherwise the player datas can't
		 * recognize what profess the player has and professes will be lost
		 */
		Bukkit.getOnlinePlayers().forEach(player -> dataProvider.getDataManager().setup(player.getUniqueId()));

		// load guild data after loading player data
		dataProvider.getGuildManager().load();

		// Command
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			FileConfiguration config = new ConfigFile("commands").getConfig();

			if (config.contains("player"))
				commandMap.register("mmocore", new PlayerStatsCommand(config.getConfigurationSection("player")));
			if (config.contains("attributes"))
				commandMap.register("mmocore", new AttributesCommand(config.getConfigurationSection("attributes")));
			if (config.contains("class"))
				commandMap.register("mmocore", new ClassCommand(config.getConfigurationSection("class")));
			if (config.contains("waypoints"))
				commandMap.register("mmocore", new WaypointsCommand(config.getConfigurationSection("waypoints")));
			if (config.contains("quests"))
				commandMap.register("mmocore", new QuestsCommand(config.getConfigurationSection("quests")));
			if (config.contains("skills"))
				commandMap.register("mmocore", new SkillsCommand(config.getConfigurationSection("skills")));
			if (config.contains("friends"))
				commandMap.register("mmocore", new FriendsCommand(config.getConfigurationSection("friends")));
			if (config.contains("party"))
				commandMap.register("mmocore", new PartyCommand(config.getConfigurationSection("party")));
			if (config.contains("guild"))
				commandMap.register("mmocore", new GuildCommand(config.getConfigurationSection("guild")));

			if (hasEconomy() && economy.isValid()) {
				if (config.contains("withdraw"))
					commandMap.register("mmocore", new WithdrawCommand(config.getConfigurationSection("withdraw")));
				if (config.contains("deposit"))
					commandMap.register("mmocore", new DepositCommand(config.getConfigurationSection("deposit")));
			}
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
			ex.printStackTrace();
		}

		MMOCoreCommandTreeRoot mmoCoreCommand = new MMOCoreCommandTreeRoot();
		getCommand("mmocore").setExecutor(mmoCoreCommand);
		getCommand("mmocore").setTabCompleter(mmoCoreCommand);

		if (getConfig().getBoolean("auto-save.enabled")) {
			int autosave = getConfig().getInt("auto-save.interval") * 20;
			new BukkitRunnable() {
				public void run() {

					// Save player data
					for (PlayerData data : PlayerData.getAll())
						if (data.isFullyLoaded())
							dataProvider.getDataManager().saveData(data);

					// Save guild info
					for (Guild guild : dataProvider.getGuildManager().getAll())
						dataProvider.getGuildManager().save(guild);
				}
			}.runTaskTimerAsynchronously(MMOCore.plugin, autosave, autosave);
		}
	}

	public void disable() {

		// Save player data
		for (PlayerData data : PlayerData.getAll())
			if (data.isFullyLoaded()) {
				data.close();
				dataProvider.getDataManager().saveData(data);
			}

		// Save guild info
		for (Guild guild : dataProvider.getGuildManager().getAll())
			dataProvider.getGuildManager().save(guild);

		// Close MySQL data provider (memory leaks)
		if (dataProvider instanceof MySQLDataProvider)
			((MySQLDataProvider) dataProvider).close();

		// Reset active blocks
		mineManager.resetRemainingBlocks();

		// Clear spawned loot chests
		lootChests.getActive().forEach(chest -> chest.unregister(false));
	}

	/**
	 * Called either when the server starts when initializing the manager for
	 * the first time, or when issuing a plugin reload; in that case, stuff
	 * like listeners must all be cleared before.
	 *
	 * Also see {@link MMOCoreManager}
	 *
	 * @param clearBefore True when issuing a plugin reload
	 */
	public void initializePlugin(boolean clearBefore) {
		reloadConfig();

		configManager = new ConfigManager();

		skillManager.initialize(clearBefore);
		mineManager.initialize(clearBefore);
		partyManager.initialize(clearBefore);
		attributeManager.initialize(clearBefore);

		// Experience must be loaded before professions and classes
		experience.initialize(clearBefore);

		// Drop tables must be loaded before professions
		dropTableManager.initialize(clearBefore);

		professionManager.initialize(clearBefore);
		classManager.initialize(clearBefore);

		InventoryManager.load();

		questManager.initialize(clearBefore);
		lootChests.initialize(clearBefore);
		restrictionManager.initialize(clearBefore);

		waypointManager = new WaypointManager(new ConfigFile("waypoints").getConfig());
		requestManager = new RequestManager();
		soundManager = new SoundManager(new ConfigFile("sounds").getConfig());
		configItems = new ConfigItemManager(new ConfigFile("items").getConfig());

		if (getConfig().isConfigurationSection("action-bar"))
			actionBarManager.reload(getConfig().getConfigurationSection("action-bar"));

		StatType.load();
	}

	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void debug(int value, String message) {
		debug(value, Level.INFO, message);
	}

	public static void log(Level level, String message) {
		plugin.getLogger().log(level, message);
	}

	public static void debug(int value, Level level, String message) {
		if (DebugMode.level > (value - 1)) plugin.getLogger().log(level, message);
	}

	public File getJarFile() {
		return getFile();
	}

	public boolean hasEconomy() {
		return economy != null && economy.isValid();
	}

	public static void sqlDebug(String s) {
		if(!MMOCore.plugin.shouldDebugSQL) return;
		MMOCore.plugin.getLogger().warning("- [SQL Debug] " + s);
	}
}
