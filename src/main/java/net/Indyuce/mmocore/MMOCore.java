package net.Indyuce.mmocore;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.codingforcookies.armorequip.ArmorListener;

import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.debug.DebugMode;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.command.AttributesCommand;
import net.Indyuce.mmocore.command.ClassCommand;
import net.Indyuce.mmocore.command.DepositCommand;
import net.Indyuce.mmocore.command.FriendsCommand;
import net.Indyuce.mmocore.command.MMOCoreCommand;
import net.Indyuce.mmocore.command.PartyCommand;
import net.Indyuce.mmocore.command.PlayerStatsCommand;
import net.Indyuce.mmocore.command.QuestsCommand;
import net.Indyuce.mmocore.command.SkillsCommand;
import net.Indyuce.mmocore.command.WaypointsCommand;
import net.Indyuce.mmocore.command.WithdrawCommand;
import net.Indyuce.mmocore.comp.Metrics;
import net.Indyuce.mmocore.comp.ShopKeepersEntityHandler;
import net.Indyuce.mmocore.comp.citizens.CitizenInteractEventListener;
import net.Indyuce.mmocore.comp.citizens.CitizensMMOLoader;
import net.Indyuce.mmocore.comp.entity.MyPetEntityHandler;
import net.Indyuce.mmocore.comp.holograms.CMIPlugin;
import net.Indyuce.mmocore.comp.holograms.HologramSupport;
import net.Indyuce.mmocore.comp.holograms.HologramsPlugin;
import net.Indyuce.mmocore.comp.holograms.HolographicDisplaysPlugin;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsDrops;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsMMOLoader;
import net.Indyuce.mmocore.comp.placeholder.DefaultParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderAPIParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderParser;
import net.Indyuce.mmocore.comp.rpg.DefaultRPGUtilHandler;
import net.Indyuce.mmocore.comp.rpg.RPGUtilHandler;
import net.Indyuce.mmocore.comp.vault.VaultEconomy;
import net.Indyuce.mmocore.comp.vault.VaultMMOLoader;
import net.Indyuce.mmocore.comp.worldguard.DefaultRegionHandler;
import net.Indyuce.mmocore.comp.worldguard.RegionHandler;
import net.Indyuce.mmocore.comp.worldguard.WorldGuardMMOLoader;
import net.Indyuce.mmocore.comp.worldguard.WorldGuardRegionHandler;
import net.Indyuce.mmocore.listener.BlockListener;
import net.Indyuce.mmocore.listener.GoldPouchesListener;
import net.Indyuce.mmocore.listener.LootableChestsListener;
import net.Indyuce.mmocore.listener.PartyListener;
import net.Indyuce.mmocore.listener.PlayerListener;
import net.Indyuce.mmocore.listener.SpellCast;
import net.Indyuce.mmocore.listener.WaypointsListener;
import net.Indyuce.mmocore.listener.event.PlayerAttackEventListener;
import net.Indyuce.mmocore.listener.profession.FishingListener;
import net.Indyuce.mmocore.listener.profession.PlayerCollectStats;
import net.Indyuce.mmocore.manager.AttributeManager;
import net.Indyuce.mmocore.manager.ClassManager;
import net.Indyuce.mmocore.manager.ConfigItemManager;
import net.Indyuce.mmocore.manager.ConfigManager;
import net.Indyuce.mmocore.manager.CustomBlockManager;
import net.Indyuce.mmocore.manager.DamageManager;
import net.Indyuce.mmocore.manager.DropTableManager;
import net.Indyuce.mmocore.manager.EntityManager;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.manager.LootableChestManager;
import net.Indyuce.mmocore.manager.MMOLoadManager;
import net.Indyuce.mmocore.manager.QuestManager;
import net.Indyuce.mmocore.manager.RestrictionManager;
import net.Indyuce.mmocore.manager.SkillManager;
import net.Indyuce.mmocore.manager.WaypointManager;
import net.Indyuce.mmocore.manager.profession.AlchemyManager;
import net.Indyuce.mmocore.manager.profession.EnchantManager;
import net.Indyuce.mmocore.manager.profession.FishingManager;
import net.Indyuce.mmocore.manager.profession.ProfessionManager;
import net.Indyuce.mmocore.manager.profession.SmithingManager;
import net.Indyuce.mmocore.manager.social.BoosterManager;
import net.Indyuce.mmocore.manager.social.PartyManager;
import net.Indyuce.mmocore.manager.social.RequestManager;
import net.Indyuce.mmocore.version.ServerVersion;
import net.Indyuce.mmocore.version.nms.NMSHandler;

public class MMOCore extends JavaPlugin {
	public static MMOCore plugin;

	public final ClassManager classManager = new ClassManager();
	public ConfigManager configManager;
	public WaypointManager waypointManager;
	public RestrictionManager restrictionManager;
	public final DropTableManager dropTableManager = new DropTableManager();
	public final CustomBlockManager mineManager = new CustomBlockManager();
	public final BoosterManager boosterManager = new BoosterManager();
	public LootableChestManager chestManager;
	public RequestManager requestManager;
	public final AttributeManager attributeManager = new AttributeManager();
	public final PartyManager partyManager = new PartyManager();
	public final QuestManager questManager = new QuestManager();
	public ConfigItemManager configItems;
	public SkillManager skillManager;
	public final ProfessionManager professionManager = new ProfessionManager();
	public VaultEconomy economy;
	public HologramSupport hologramSupport;
	public PlaceholderParser placeholderParser = new DefaultParser();
	public final DamageManager damage = new DamageManager();
	public final EntityManager entities = new EntityManager();
	public NMSHandler nms;
	public ServerVersion version;
	public InventoryManager inventoryManager;
	public RegionHandler regionHandler;

	/*
	 * professions
	 */
	public final FishingManager fishingManager = new FishingManager();
	public final AlchemyManager alchemyManager = new AlchemyManager();
	public final EnchantManager enchantManager = new EnchantManager();
	public final SmithingManager smithingManager = new SmithingManager();

	public final MMOLoadManager loadManager = new MMOLoadManager();
	public RPGUtilHandler rpgUtilHandler = new DefaultRPGUtilHandler();

	public static final DecimalFormat digit = new DecimalFormat("0.#"), digit2 = new DecimalFormat("0.##"), digit3 = new DecimalFormat("0.###");
	public static final SimpleDateFormat smoothDateFormat = new SimpleDateFormat("");

	public void onLoad() {
		plugin = this;
		version = new ServerVersion(Bukkit.getServer().getClass());

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
	}

	public void onEnable() {

		/*
		 * decimal format
		 */
		DecimalFormatSymbols symbols = digit.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		digit.setDecimalFormatSymbols(symbols);
		digit2.setDecimalFormatSymbols(symbols);
		digit3.setDecimalFormatSymbols(symbols);

		try {
			getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
			nms = (NMSHandler) Class.forName("net.Indyuce.mmocore.version.nms.NMSHandler_" + version.toString().substring(1)).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
			getLogger().log(Level.INFO, "Your server version is not compatible.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		new Metrics(this);

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
		} else
			regionHandler = new DefaultRegionHandler();

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

		if (Bukkit.getPluginManager().getPlugin("ShopKeepers") != null) {
			entities.registerHandler(new ShopKeepersEntityHandler());
			getLogger().log(Level.INFO, "Hooked onto ShopKeepers");
		}

		if (Bukkit.getPluginManager().getPlugin("MyPet") != null) {
			entities.registerHandler(new MyPetEntityHandler());
			getLogger().log(Level.INFO, "Hooked onto MyPet");
		}

		if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
			Bukkit.getServer().getPluginManager().registerEvents(new MythicMobsDrops(), this);
			getLogger().log(Level.INFO, "Hooked onto MythicMobs");
		}

		/*
		 * resource regeneration. must check if entity is dead otherwise regen
		 * will make the 'respawn' button glitched plus HURT entity effect bug
		 */
		new BukkitRunnable() {
			public void run() {
				for (PlayerData data : PlayerData.getAll())
					if (data.isOnline() && !data.getPlayer().isDead()) {

						data.giveStellium(data.getStats().getStat(StatType.STELLIUM_REGENERATION));

						if (data.canRegen(PlayerResource.HEALTH))
							data.heal(data.calculateRegen(PlayerResource.HEALTH));

						if (data.canRegen(PlayerResource.MANA))
							data.giveMana(data.calculateRegen(PlayerResource.MANA));

						if (data.canRegen(PlayerResource.STAMINA))
							data.giveStamina(data.calculateRegen(PlayerResource.STAMINA));
					}
			}
		}.runTaskTimerAsynchronously(MMOCore.plugin, 100, 20);

		saveDefaultConfig();
		reloadPlugin();

		/*
		 * enable debug mode for extra debug tools.
		 */
		if (getConfig().getBoolean("debug"))
			new DebugMode();

		Bukkit.getPluginManager().registerEvents(new PlayerAttackEventListener(), this);

		Bukkit.getPluginManager().registerEvents(new DamageManager(), this);
		Bukkit.getPluginManager().registerEvents(new WaypointsListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new GoldPouchesListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new LootableChestsListener(), this);
		Bukkit.getPluginManager().registerEvents(new SpellCast(), this);
		Bukkit.getPluginManager().registerEvents(new PartyListener(), this);
		Bukkit.getPluginManager().registerEvents(new FishingListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerCollectStats(), this);

		Bukkit.getPluginManager().registerEvents(new ArmorListener(), this);

		/*
		 * initialize player data from all online players. this is very
		 * important to do that after registering all the professses otherwise
		 * the player datas can't recognize what profess the player has and
		 * professes will be lost
		 */
		Bukkit.getOnlinePlayers().forEach(player -> PlayerData.setup(player).setPlayer(player));

		// commands
		getCommand("player").setExecutor(new PlayerStatsCommand());
		getCommand("attributes").setExecutor(new AttributesCommand());
		getCommand("class").setExecutor(new ClassCommand());
		getCommand("waypoints").setExecutor(new WaypointsCommand());
		getCommand("quests").setExecutor(new QuestsCommand());
		getCommand("skills").setExecutor(new SkillsCommand());
		getCommand("friends").setExecutor(new FriendsCommand());
		getCommand("party").setExecutor(new PartyCommand());

		if (hasEconomy() && economy.isValid()) {
			getCommand("withdraw").setExecutor(new WithdrawCommand());
			getCommand("deposit").setExecutor(new DepositCommand());
		}

		MMOCoreCommand mmoCoreCommand = new MMOCoreCommand();
		getCommand("mmocore").setExecutor(mmoCoreCommand);
		getCommand("mmocore").setTabCompleter(mmoCoreCommand);
	}

	public void onDisable() {
		for (PlayerData playerData : PlayerData.getAll()) {
			ConfigFile config = new ConfigFile(playerData.getUniqueId());
			playerData.getQuestData().resetBossBar();
			playerData.saveInConfig(config.getConfig());
			config.save();
		}

		mineManager.resetRemainingBlocks();
	}

	public void reloadPlugin() {
		configManager = new ConfigManager();
		skillManager = new SkillManager();

		mineManager.clear();
		mineManager.reload();

		fishingManager.clear();
		alchemyManager.clear();
		smithingManager.clear();

		partyManager.clear();
		partyManager.reload();

		attributeManager.clear();
		attributeManager.reload();

		professionManager.clear();
		professionManager.reload();

		classManager.clear();
		classManager.reload();

		inventoryManager = new InventoryManager();

		dropTableManager.clear();
		dropTableManager.reload();

		questManager.clear();
		questManager.reload();

		chestManager = new LootableChestManager(new ConfigFile("chests").getConfig());
		waypointManager = new WaypointManager(new ConfigFile("waypoints").getConfig());
		restrictionManager = new RestrictionManager(new ConfigFile("restrictions").getConfig());
		requestManager = new RequestManager();
		configItems = new ConfigItemManager(new ConfigFile("items").getConfig());

		StatType.load();
	}

	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void log(Level level, String message) {
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
