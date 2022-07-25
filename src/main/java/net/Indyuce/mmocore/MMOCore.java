package net.Indyuce.mmocore;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.metrics.bukkit.Metrics;
import io.lumine.mythic.lib.version.SpigotPlugin;
import net.Indyuce.mmocore.comp.citizens.CitizenInteractEventListener;
import net.Indyuce.mmocore.comp.citizens.CitizensMMOLoader;
import net.Indyuce.mmocore.comp.mythicmobs.MythicHook;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsMMOLoader;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.PlayerActionBar;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.AttributeModifier;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.util.debug.DebugMode;
import net.Indyuce.mmocore.command.*;
import net.Indyuce.mmocore.comp.placeholder.DefaultParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderAPIParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderParser;
import net.Indyuce.mmocore.comp.region.DefaultRegionHandler;
import net.Indyuce.mmocore.comp.region.RegionHandler;
import net.Indyuce.mmocore.comp.region.WorldGuardMMOLoader;
import net.Indyuce.mmocore.comp.region.WorldGuardRegionHandler;
import net.Indyuce.mmocore.comp.vault.VaultEconomy;
import net.Indyuce.mmocore.comp.vault.VaultMMOLoader;
import net.Indyuce.mmocore.guild.GuildModule;
import net.Indyuce.mmocore.guild.GuildModuleType;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.guild.provided.MMOCoreGuildModule;
import net.Indyuce.mmocore.listener.*;
import net.Indyuce.mmocore.listener.bungee.GetMMOCorePlayerListener;
import net.Indyuce.mmocore.listener.event.PlayerPressKeyListener;
import net.Indyuce.mmocore.listener.option.*;
import net.Indyuce.mmocore.listener.profession.FishingListener;
import net.Indyuce.mmocore.listener.profession.PlayerCollectStats;
import net.Indyuce.mmocore.manager.*;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.mysql.MySQLDataProvider;
import net.Indyuce.mmocore.manager.data.yaml.YAMLDataProvider;
import net.Indyuce.mmocore.manager.profession.*;
import net.Indyuce.mmocore.manager.social.BoosterManager;
import net.Indyuce.mmocore.manager.social.PartyManager;
import net.Indyuce.mmocore.manager.social.RequestManager;
import net.Indyuce.mmocore.party.MMOCoreTargetRestriction;
import net.Indyuce.mmocore.party.PartyModule;
import net.Indyuce.mmocore.party.PartyModuleType;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import net.Indyuce.mmocore.skill.custom.mechanic.ExperienceMechanic;
import net.Indyuce.mmocore.skill.custom.mechanic.ManaMechanic;
import net.Indyuce.mmocore.skill.custom.mechanic.StaminaMechanic;
import net.Indyuce.mmocore.skill.custom.mechanic.StelliumMechanic;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

public class MMOCore extends JavaPlugin {
	public static MMOCore plugin;

	public final WaypointManager waypointManager = new WaypointManager();
	public final SoundManager soundManager = new SoundManager();
	public final RequestManager requestManager = new RequestManager();
	public final ConfigItemManager configItems = new ConfigItemManager();
	public final PlayerActionBar actionBarManager = new PlayerActionBar();
	public final SkillManager skillManager = new SkillManager();
	public final ClassManager classManager = new ClassManager();
	public final DropTableManager dropTableManager = new DropTableManager();
	public final BoosterManager boosterManager = new BoosterManager();
	public final AttributeManager attributeManager = new AttributeManager();
	public final PartyManager partyManager = new PartyManager();
	public final QuestManager questManager = new QuestManager();
	public final ProfessionManager professionManager = new ProfessionManager();
	public final ExperienceManager experience = new ExperienceManager();
	public final LootChestManager lootChests = new LootChestManager();
	public final MMOLoadManager loadManager = new MMOLoadManager();
	public final RestrictionManager restrictionManager = new RestrictionManager();
	public final StatManager statManager = new StatManager();
	@Deprecated
	public final SkillTreeManager skillTreeManager = new SkillTreeManager();

	// Profession managers
	public final CustomBlockManager mineManager = new CustomBlockManager();
	public final FishingManager fishingManager = new FishingManager();
	public final AlchemyManager alchemyManager = new AlchemyManager();
	public final EnchantManager enchantManager = new EnchantManager();
	public final SmithingManager smithingManager = new SmithingManager();

	@NotNull
	public ConfigManager configManager;
	public VaultEconomy economy;
	public RegionHandler regionHandler = new DefaultRegionHandler();
	public PlaceholderParser placeholderParser = new DefaultParser();
	public DataProvider dataProvider = new YAMLDataProvider();

	// Modules
	@NotNull
	public PartyModule partyModule;
	@NotNull
	public GuildModule guildModule;

	public boolean shouldDebugSQL, hasBungee;

	public MMOCore() {
		plugin = this;
	}

	@Override
	public void onLoad() {

		// Register MMOCore-specific objects
		MythicLib.plugin.getEntities().registerRestriction(new MMOCoreTargetRestriction());
		MythicLib.plugin.getModifiers().registerModifierType("attribute", configObject -> new AttributeModifier(configObject));

		// Skill creation
		MythicLib.plugin.getSkills().registerMechanic("mana", config -> new ManaMechanic(config));
		MythicLib.plugin.getSkills().registerMechanic("stamina", config -> new StaminaMechanic(config));
		MythicLib.plugin.getSkills().registerMechanic("stellium", config -> new StelliumMechanic(config));
		MythicLib.plugin.getSkills().registerMechanic("experience", config -> new ExperienceMechanic(config));

        // Register extra objective, drop items...
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
            loadManager.registerLoader(new WorldGuardMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("Citizens") != null)
			loadManager.registerLoader(new CitizensMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("Vault") != null) loadManager.registerLoader(new VaultMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
			loadManager.registerLoader(new MythicMobsMMOLoader());
	}

	@Override
	public void onEnable() {
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

		// Checks if the server runs with Bungee
		hasBungee = SpigotConfig.bungee & !Bukkit.getServer().getOnlineMode();

        //Setups the channel for Bungee
        if(hasBungee) {
            getServer().getMessenger().registerOutgoingPluginChannel(this,"namespace:give_mmocore_player");
            getServer().getMessenger().registerOutgoingPluginChannel(this,"namespace:get_mmocore_player");
            getServer().getMessenger().registerIncomingPluginChannel(this,"namespace:get_mmocore_player",new GetMMOCorePlayerListener());
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

		// Enable debug mode for extra debug tools
		if (getConfig().contains("debug")) {
			DebugMode.setLevel(getConfig().getInt("debug", 0));
			DebugMode.enableActionBar();
		}

        // Load quest module
        try {
            String questPluginName = UtilityMethods.enumName(getConfig().getString("quest-plugin"));
            PartyModuleType moduleType = PartyModuleType.valueOf(questPluginName);
            Validate.isTrue(moduleType.isValid(), "Plugin '" + moduleType.name() + "' is not installed");
            partyModule = moduleType.provideModule();
        } catch (RuntimeException exception) {
            getLogger().log(Level.WARNING, "Could not initialize quest module: " + exception.getMessage());
            partyModule = new MMOCorePartyModule();
        }

        // Load party module
        try {
            String partyPluginName = UtilityMethods.enumName(getConfig().getString("party-plugin"));
            PartyModuleType moduleType = PartyModuleType.valueOf(partyPluginName);
            Validate.isTrue(moduleType.isValid(), "Plugin '" + moduleType.name() + "' is not installed");
            partyModule = moduleType.provideModule();
        } catch (RuntimeException exception) {
            getLogger().log(Level.WARNING, "Could not initialize party module: " + exception.getMessage());
            partyModule = new MMOCorePartyModule();
        }

		// Load guild module
		try {
			String pluginName = UtilityMethods.enumName(getConfig().getString("guild-plugin"));
			GuildModuleType moduleType = GuildModuleType.valueOf(pluginName);
			Validate.isTrue(moduleType.isValid(), "Plugin '" + moduleType.name() + "' is not installed");
			guildModule = moduleType.provideModule();
		} catch (RuntimeException exception) {
			getLogger().log(Level.WARNING, "Could not initialize guild module: " + exception.getMessage());
			guildModule = new MMOCoreGuildModule();
		}

		// Skill casting
		try {
			SkillCastingMode mode = SkillCastingMode.valueOf(UtilityMethods.enumName(getConfig().getString("skill-casting.mode")));
			Bukkit.getPluginManager().registerEvents(mode.loadFromConfig(getConfig().getConfigurationSection("skill-casting")), this);
		} catch (RuntimeException exception) {
			getLogger().log(Level.WARNING, "Could not load skill casting: " + exception.getMessage());
		}

		if (configManager.overrideVanillaExp = getConfig().getBoolean("override-vanilla-exp"))
			Bukkit.getPluginManager().registerEvents(new VanillaExperienceOverride(), this);

		if (getConfig().getBoolean("hotbar-swapping.enabled"))
			try {
				Bukkit.getPluginManager().registerEvents(new HotbarSwap(getConfig().getConfigurationSection("hotbar-swapping")), this);
			} catch (RuntimeException exception) {
				getLogger().log(Level.WARNING, "Could not load hotbar swapping: " + exception.getMessage());
			}

		if (getConfig().getBoolean("prevent-spawner-xp")) {
			Bukkit.getPluginManager().registerEvents(new NoSpawnerEXP(), this);
		}

		if (getConfig().getBoolean("death-exp-loss.enabled"))
			Bukkit.getPluginManager().registerEvents(new DeathExperienceLoss(), this);

		if (getConfig().getBoolean("shift-click-player-profile-check"))
			Bukkit.getPluginManager().registerEvents(new PlayerProfileCheck(), this);

		Bukkit.getPluginManager().registerEvents(new WaypointsListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new GoldPouchesListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new LootableChestsListener(), this);
		Bukkit.getPluginManager().registerEvents(new GuildListener(), this);
		Bukkit.getPluginManager().registerEvents(new FishingListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerCollectStats(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerPressKeyListener(), this);
		// Bukkit.getPluginManager().registerEvents(new ClassTriggers(), this);

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
		lootChests.getActive().forEach(chest -> chest.expire(false));
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
		if (clearBefore)
			reloadConfig();

		configManager = new ConfigManager();

		statManager.initialize(clearBefore);
		if (clearBefore)
			MythicLib.plugin.getSkills().initialize(true);
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
		waypointManager.initialize(clearBefore);
		requestManager.initialize(clearBefore);
		soundManager.initialize(clearBefore);
		configItems.initialize(clearBefore);

		if (getConfig().isConfigurationSection("action-bar"))
			actionBarManager.reload(getConfig().getConfigurationSection("action-bar"));

		if (clearBefore)
			PlayerData.getAll().forEach(PlayerData::update);
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
