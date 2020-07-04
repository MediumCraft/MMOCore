package net.Indyuce.mmocore;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.PlayerActionBar;
import net.Indyuce.mmocore.api.loot.LootChest;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.social.guilds.Guild;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.debug.DebugMode;
import net.Indyuce.mmocore.command.AttributesCommand;
import net.Indyuce.mmocore.command.ClassCommand;
import net.Indyuce.mmocore.command.DepositCommand;
import net.Indyuce.mmocore.command.FriendsCommand;
import net.Indyuce.mmocore.command.GuildCommand;
import net.Indyuce.mmocore.command.MMOCoreCommand;
import net.Indyuce.mmocore.command.PartyCommand;
import net.Indyuce.mmocore.command.PlayerStatsCommand;
import net.Indyuce.mmocore.command.QuestsCommand;
import net.Indyuce.mmocore.command.SkillsCommand;
import net.Indyuce.mmocore.command.WaypointsCommand;
import net.Indyuce.mmocore.command.WithdrawCommand;
import net.Indyuce.mmocore.comp.citizens.CitizenInteractEventListener;
import net.Indyuce.mmocore.comp.citizens.CitizensMMOLoader;
import net.Indyuce.mmocore.comp.flags.DefaultFlags;
import net.Indyuce.mmocore.comp.flags.FlagPlugin;
import net.Indyuce.mmocore.comp.flags.ResidenceFlags;
import net.Indyuce.mmocore.comp.flags.WorldGuardFlags;
import net.Indyuce.mmocore.comp.holograms.CMIPlugin;
import net.Indyuce.mmocore.comp.holograms.HologramSupport;
import net.Indyuce.mmocore.comp.holograms.HologramsPlugin;
import net.Indyuce.mmocore.comp.holograms.HolographicDisplaysPlugin;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsDrops;
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
import net.Indyuce.mmocore.listener.BlockListener;
import net.Indyuce.mmocore.listener.GoldPouchesListener;
import net.Indyuce.mmocore.listener.GuildListener;
import net.Indyuce.mmocore.listener.LootableChestsListener;
import net.Indyuce.mmocore.listener.PartyListener;
import net.Indyuce.mmocore.listener.PlayerListener;
import net.Indyuce.mmocore.listener.SpellCast;
import net.Indyuce.mmocore.listener.WaypointsListener;
import net.Indyuce.mmocore.listener.option.DeathExperienceLoss;
import net.Indyuce.mmocore.listener.option.HealthScale;
import net.Indyuce.mmocore.listener.option.NoSpawnerEXP;
import net.Indyuce.mmocore.listener.option.RedirectVanillaExp;
import net.Indyuce.mmocore.listener.option.VanillaExperienceOverride;
import net.Indyuce.mmocore.listener.profession.FishingListener;
import net.Indyuce.mmocore.listener.profession.PlayerCollectStats;
import net.Indyuce.mmocore.manager.AttributeManager;
import net.Indyuce.mmocore.manager.ClassManager;
import net.Indyuce.mmocore.manager.ConfigItemManager;
import net.Indyuce.mmocore.manager.ConfigManager;
import net.Indyuce.mmocore.manager.CustomBlockManager;
import net.Indyuce.mmocore.manager.DropTableManager;
import net.Indyuce.mmocore.manager.ExperienceManager;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.manager.LootChestManager;
import net.Indyuce.mmocore.manager.MMOLoadManager;
import net.Indyuce.mmocore.manager.QuestManager;
import net.Indyuce.mmocore.manager.RestrictionManager;
import net.Indyuce.mmocore.manager.SkillManager;
import net.Indyuce.mmocore.manager.WaypointManager;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.mysql.MySQLDataProvider;
import net.Indyuce.mmocore.manager.data.yaml.YAMLDataProvider;
import net.Indyuce.mmocore.manager.profession.AlchemyManager;
import net.Indyuce.mmocore.manager.profession.EnchantManager;
import net.Indyuce.mmocore.manager.profession.FishingManager;
import net.Indyuce.mmocore.manager.profession.ProfessionManager;
import net.Indyuce.mmocore.manager.profession.SmithingManager;
import net.Indyuce.mmocore.manager.social.BoosterManager;
import net.Indyuce.mmocore.manager.social.PartyManager;
import net.Indyuce.mmocore.manager.social.RequestManager;
import net.mmogroup.mmolib.comp.Metrics;
import net.mmogroup.mmolib.version.SpigotPlugin;

public class MMOCore extends JavaPlugin {
	public static MMOCore plugin;

	public ConfigManager configManager;
	public WaypointManager waypointManager;
	public RestrictionManager restrictionManager;
	public RequestManager requestManager;
	public ConfigItemManager configItems;
	public VaultEconomy economy;
	public HologramSupport hologramSupport;
	public InventoryManager inventoryManager;
	public PlayerActionBar actionBarManager;
	public RegionHandler regionHandler = new DefaultRegionHandler();
	public FlagPlugin flagPlugin = new DefaultFlags();
	public PlaceholderParser placeholderParser = new DefaultParser();
	public DataProvider dataProvider = new YAMLDataProvider();
	public final SkillManager skillManager = new SkillManager();
	public final ClassManager classManager = new ClassManager();
	public final DropTableManager dropTableManager = new DropTableManager();
	public final CustomBlockManager mineManager = new CustomBlockManager();
	public final BoosterManager boosterManager = new BoosterManager();
	public final AttributeManager attributeManager = new AttributeManager();
	public final PartyManager partyManager = new PartyManager();
	public final QuestManager questManager = new QuestManager();
	public final ProfessionManager professionManager = new ProfessionManager();
	public final ExperienceManager experience = new ExperienceManager();
	public final LootChestManager lootChests = new LootChestManager();

	/*
	 * professions
	 */
	public final FishingManager fishingManager = new FishingManager();
	public final AlchemyManager alchemyManager = new AlchemyManager();
	public final EnchantManager enchantManager = new EnchantManager();
	public final SmithingManager smithingManager = new SmithingManager();

	public final MMOLoadManager loadManager = new MMOLoadManager();

	public void onLoad() {
		plugin = this;

		/*
		 * register extra objective, drop items...
		 */
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
			loadManager.registerLoader(new WorldGuardMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("Citizens") != null)
			loadManager.registerLoader(new CitizensMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("Vault") != null)
			loadManager.registerLoader(new VaultMMOLoader());

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
			loadManager.registerLoader(new MythicMobsMMOLoader());
		
		/*
		 *  WorldGuard closes the flag registry after 'onLoad()',
		 *  so it must be registered here or it will throw an IllegalStateException
		 */
		if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
			flagPlugin = new WorldGuardFlags();
	}

	public void onEnable() {

		new SpigotPlugin(70575, this).checkForUpdate();
		new Metrics(this);

		if (getConfig().contains("mysql") && getConfig().getBoolean("mysql.enabled"))
			dataProvider = new MySQLDataProvider();

		if (Bukkit.getPluginManager().getPlugin("Vault") != null)
			economy = new VaultEconomy();

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
		} else if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
			flagPlugin = new ResidenceFlags();
			getLogger().log(Level.INFO, "Hooked onto Residence");
		}

		if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
			hologramSupport = new HolographicDisplaysPlugin();
			getLogger().log(Level.INFO, "Hooked onto HolographicDisplays");
		} else if (Bukkit.getPluginManager().getPlugin("CMI") != null) {
			hologramSupport = new CMIPlugin();
			getLogger().log(Level.INFO, "Hooked onto CMI Holograms");
		} else if (Bukkit.getPluginManager().getPlugin("Holograms") != null) {
			hologramSupport = new HologramsPlugin();
			getLogger().log(Level.INFO, "Hooked onto Holograms");
		}

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			Bukkit.getServer().getPluginManager().registerEvents(new MythicMobsDrops(), this);
			MMOCore.plugin.getLogger().log(Level.INFO, "Hooked onto MythicMobs");
		}

		/*
		 * resource regeneration. must check if entity is dead otherwise regen
		 * will make the 'respawn' button glitched plus HURT entity effect bug
		 */
		new BukkitRunnable() {
			public void run() {
				for (PlayerData player : PlayerData.getAll())
					if (player.isOnline() && !player.getPlayer().isDead())
						for (PlayerResource resource : PlayerResource.values()) {
							double d = player.getProfess().getHandler(resource).getRegen(player);
							if (d != 0)
								resource.regen(player, d);
						}
			}
		}.runTaskTimer(MMOCore.plugin, 100, 20);

		/*
		 * clean active loot chests every 5 minutes. cannot register this
		 * runnable in the loot chest manager because it is instanced when the
		 * plugin loads
		 */
		new BukkitRunnable() {
			public void run() {
				for (LootChest chest : new HashSet<>(lootChests.getActive()))
					if (chest.shouldExpire())
						chest.unregister(false);
			}
		}.runTaskTimer(this, 5 * 60 * 20, 5 * 60 * 20);

		/*
		 * For the sake of the lord, make sure they aren't using MMOItems Mana
		 * and Stamina Addon...This should prevent a couple error reports
		 * produced by people not reading the installation guide...
		 */
		if (Bukkit.getPluginManager().getPlugin("MMOItemsMana") != null) {
			getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "MMOCore is not meant to be used with MMOItems ManaAndStamina");
			getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Please read the installation guide!");
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "[MMOCore] MMOCore is not meant to be used with MMOItems ManaAndStamina");
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "[MMOCore] Please read the installation guide!");
			return;
		}

		saveDefaultConfig();
		reloadPlugin();

		/*
		 * default action bar. only ran if the action bar is enabled
		 */
		if (getConfig().getBoolean("action-bar.enabled"))
			new PlayerActionBar(getConfig().getConfigurationSection("action-bar"));

		if (getConfig().getBoolean("vanilla-exp-redirection.enabled"))
			Bukkit.getPluginManager().registerEvents(new RedirectVanillaExp(getConfig().getDouble("vanilla-exp-redirection.ratio")), this);

		/*
		 * enable debug mode for extra debug tools.
		 */
		if (getConfig().contains("debug"))
			new DebugMode(getConfig().getInt("debug", 0));

		if (configManager.overrideVanillaExp = getConfig().getBoolean("override-vanilla-exp"))
			Bukkit.getPluginManager().registerEvents(new VanillaExperienceOverride(), this);

		if (getConfig().getBoolean("health-scale.enabled"))
			Bukkit.getPluginManager().registerEvents(new HealthScale(), this);

		if (getConfig().getBoolean("prevent-spawner-xp"))
			Bukkit.getPluginManager().registerEvents(new NoSpawnerEXP(), this);

		if (getConfig().getBoolean("death-exp-loss.enabled"))
			Bukkit.getPluginManager().registerEvents(new DeathExperienceLoss(), this);

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
		 * initialize player data from all online players. this is very
		 * important to do that after registering all the professses otherwise
		 * the player datas can't recognize what profess the player has and
		 * professes will be lost
		 */
		Bukkit.getOnlinePlayers().forEach(player -> dataProvider.getDataManager().setup(player));

		/*
		 * load guild data after loading player data
		 */
		dataProvider.getGuildManager().load();

		/*
		 * commands
		 */
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

		MMOCoreCommand mmoCoreCommand = new MMOCoreCommand();
		getCommand("mmocore").setExecutor(mmoCoreCommand);
		getCommand("mmocore").setTabCompleter(mmoCoreCommand);

		if (getConfig().getBoolean("auto-save.enabled")) {
			int autosave = getConfig().getInt("auto-save.interval") * 20;
			new BukkitRunnable() {
				public void run() {
					for (PlayerData loaded : PlayerData.getAll())
						dataProvider.getDataManager().saveData(loaded);

					for (Guild guild : dataProvider.getGuildManager().getAll())
						dataProvider.getGuildManager().save(guild);
				}
			}.runTaskTimerAsynchronously(MMOCore.plugin, autosave, autosave);
		}
	}

	public void onDisable() {
		for (PlayerData data : PlayerData.getAll()) {
			data.getQuestData().resetBossBar();
			dataProvider.getDataManager().saveData(data);
		}

		for (Guild guild : dataProvider.getGuildManager().getAll())
			dataProvider.getGuildManager().save(guild);

		mineManager.resetRemainingBlocks();

		lootChests.getActive().forEach(chest -> chest.unregister(false));
	}

	public void reloadPlugin() {
		configManager = new ConfigManager();

		skillManager.reload();

		mineManager.clear();
		mineManager.reload();

		fishingManager.clear();
		alchemyManager.clear();
		smithingManager.clear();

		partyManager.clear();
		partyManager.reload();

		attributeManager.clear();
		attributeManager.reload();

		// experience must be loaded before professions and classes
		experience.reload();
		
		// drop tables must be loaded before professions
		dropTableManager.clear();
		dropTableManager.reload();

		professionManager.clear();
		professionManager.reload();

		classManager.clear();
		classManager.reload();

		inventoryManager = new InventoryManager();

		questManager.clear();
		questManager.reload();

		lootChests.reload();

		waypointManager = new WaypointManager(new ConfigFile("waypoints").getConfig());
		restrictionManager = new RestrictionManager(new ConfigFile("restrictions").getConfig());
		requestManager = new RequestManager();
		configItems = new ConfigItemManager(new ConfigFile("items").getConfig());

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
		if(DebugMode.level > (value - 1))
			plugin.getLogger().log(level, message);
	}

	public File getJarFile() {
		return getFile();
	}

	public boolean hasHolograms() {
		return hologramSupport != null;
	}

	public boolean hasEconomy() {
		return economy != null && economy.isValid();
	}
}
