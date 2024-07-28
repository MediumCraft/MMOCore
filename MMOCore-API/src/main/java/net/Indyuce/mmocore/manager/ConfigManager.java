package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.util.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {
    public final CommandVerbose commandVerbose = new CommandVerbose();

    public boolean overrideVanillaExp, canCreativeCast, passiveSkillsNeedBinding, cobbleGeneratorXP, saveDefaultClassInfo, splitMainExp, splitProfessionExp, disableQuestBossBar,
            pvpModeEnabled, pvpModeInvulnerabilityCanDamage, forceClassSelection, enableGlobalSkillTreeGUI, enableSpecificSkillTreeGUI;
    public String partyChatPrefix, noSkillBoundPlaceholder;
    public ChatColor staminaFull, staminaHalf, staminaEmpty;
    public long combatLogTimer, lootChestExpireTime, lootChestPlayerCooldown, globalSkillCooldown;
    public double lootChestsChanceWeight, dropItemsChanceWeight, fishingDropsChanceWeight, partyMaxExpSplitRange, pvpModeToggleOnCooldown, pvpModeToggleOffCooldown, pvpModeCombatCooldown,
            pvpModeCombatTimeout, pvpModeInvulnerabilityTimeRegionChange, pvpModeInvulnerabilityTimeCommand, pvpModeRegionEnterCooldown, pvpModeRegionLeaveCooldown;
    public int maxPartyLevelDifference, maxPartyPlayers, minCombatLevel, maxCombatLevelDifference, skillTreeScrollStepX, skillTreeScrollStepY, waypointWarpTime;
    public final List<EntityDamageEvent.DamageCause> combatLogDamageCauses = new ArrayList<>();

    private final FileConfiguration messages;

    /*
     * The instance must be created after the other managers since all it does
     * is to update them based on the config except for the classes which are
     * already loaded based on the config
     */
    public ConfigManager() {

        // Backwards compatibility for older configs
        {
            FileUtils.moveIfExists(MMOCore.plugin, "attributes.yml", "attributes");
            FileUtils.moveIfExists(MMOCore.plugin, "exp-tables.yml", "exp-tables");
            FileUtils.moveIfExists(MMOCore.plugin, "loot-chests.yml", "loot-chests");
            FileUtils.moveIfExists(MMOCore.plugin, "waypoints.yml", "waypoints");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "attributes").exists()) {
            copyDefaultFile("attributes/default_attributes.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "classes").exists()) {
            copyDefaultFile("classes/mage/arcane-mage.yml");
            copyDefaultFile("classes/mage/mage.yml");
            copyDefaultFile("classes/human.yml");
            copyDefaultFile("classes/marksman.yml");
            copyDefaultFile("classes/paladin.yml");
            copyDefaultFile("classes/rogue.yml");
            copyDefaultFile("classes/warrior.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "drop-tables").exists())
            copyDefaultFile("drop-tables/example_drop_tables.yml");

        if (!FileUtils.getFile(MMOCore.plugin, "exp-tables").exists())
            copyDefaultFile("exp-tables/default_exp_tables.yml");

        if (!FileUtils.getFile(MMOCore.plugin, "expcurves").exists()) {
            copyDefaultFile("expcurves/levels.txt");
            copyDefaultFile("expcurves/mining.txt");
            copyDefaultFile("expcurves/skill-tree-node.txt");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "loot-chests").exists())
            copyDefaultFile("loot-chests/default_loot_chests.yml");

        if (!FileUtils.getFile(MMOCore.plugin, "professions").exists()) {
            copyDefaultFile("professions/alchemy.yml");
            copyDefaultFile("professions/farming.yml");
            copyDefaultFile("professions/fishing.yml");
            copyDefaultFile("professions/mining.yml");
            copyDefaultFile("professions/smelting.yml");
            copyDefaultFile("professions/smithing.yml");
            copyDefaultFile("professions/woodcutting.yml");
            copyDefaultFile("professions/enchanting.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "quests").exists()) {
            copyDefaultFile("quests/adv-begins.yml");
            copyDefaultFile("quests/tutorial.yml");
            copyDefaultFile("quests/fetch-mango.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "skill-trees").exists()) {
            copyDefaultFile("skill-trees/combat.yml");
            copyDefaultFile("skill-trees/mage-arcane-mage.yml");
            copyDefaultFile("skill-trees/rogue-marksman.yml");
            copyDefaultFile("skill-trees/warrior-paladin.yml");
            copyDefaultFile("skill-trees/general.yml");
        }

        if (!FileUtils.getFile(MMOCore.plugin, "waypoints").exists()) {
            copyDefaultFile("waypoints/default_waypoints.yml");
        }

        copyDefaultFile("conditions.yml");
        copyDefaultFile("exp-sources.yml");
        copyDefaultFile("guilds.yml");
        copyDefaultFile("items.yml");
        copyDefaultFile("messages.yml");
        copyDefaultFile("restrictions.yml");
        copyDefaultFile("sounds.yml");
        copyDefaultFile("stats.yml");

        final ConfigurationSection config = MMOCore.plugin.getConfig();
        commandVerbose.reload(MMOCore.plugin.getConfig().getConfigurationSection("command-verbose"));

        messages = new ConfigFile("messages").getConfig();
        partyChatPrefix = MMOCore.plugin.getConfig().getString("party.chat-prefix");
        maxPartyPlayers = Math.max(2, MMOCore.plugin.getConfig().getInt("party.max-players", 8));
        // Combat log
        combatLogTimer = MMOCore.plugin.getConfig().getInt("combat-log.timer") * 1000L;
        combatLogDamageCauses.clear();
        for (String key : MMOCore.plugin.getConfig().getStringList("combat-log.causes"))
            try {
                combatLogDamageCauses.add(EntityDamageEvent.DamageCause.valueOf(UtilityMethods.enumName(key)));
            } catch (Exception exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not find damage cause called '" + key + "'");
            }
        enableGlobalSkillTreeGUI = MMOCore.plugin.getConfig().getBoolean("enable-global-skill-tree-gui");
        enableSpecificSkillTreeGUI = MMOCore.plugin.getConfig().getBoolean("enable-specific-skill-tree-gui");
        lootChestExpireTime = Math.max(MMOCore.plugin.getConfig().getInt("loot-chests.chest-expire-time"), 1) * 20;
        lootChestPlayerCooldown = (long) MMOCore.plugin.getConfig().getDouble("player-cooldown") * 1000L;
        globalSkillCooldown = MMOCore.plugin.getConfig().getLong("global-skill-cooldown") * 50;
        noSkillBoundPlaceholder = String.valueOf(messages.get("no-skill-placeholder"));
        lootChestsChanceWeight = MMOCore.plugin.getConfig().getDouble("chance-stat-weight.loot-chests");
        dropItemsChanceWeight = MMOCore.plugin.getConfig().getDouble("chance-stat-weight.drop-items");
        fishingDropsChanceWeight = MMOCore.plugin.getConfig().getDouble("chance-stat-weight.fishing-drops");
        maxPartyLevelDifference = MMOCore.plugin.getConfig().getInt("party.max-level-difference");
        partyMaxExpSplitRange = MMOCore.plugin.getConfig().getDouble("party.max-exp-split-range");
        splitMainExp = MMOCore.plugin.getConfig().getBoolean("party.main-exp-split");
        splitProfessionExp = MMOCore.plugin.getConfig().getBoolean("party.profession-exp-split");
        disableQuestBossBar = MMOCore.plugin.getConfig().getBoolean("mmocore-quests.disable-boss-bar");
        forceClassSelection = MMOCore.plugin.getConfig().getBoolean("force-class-selection");
        waypointWarpTime = MMOCore.plugin.getConfig().getInt("waypoints.default-warp-time");

        // Combat
        pvpModeEnabled = config.getBoolean("pvp_mode.enabled");
        pvpModeToggleOnCooldown = config.getDouble("pvp_mode.cooldown.toggle_on");
        pvpModeToggleOffCooldown = config.getDouble("pvp_mode.cooldown.toggle_off");
        pvpModeCombatCooldown = config.getDouble("pvp_mode.cooldown.combat");
        pvpModeRegionEnterCooldown = config.getDouble("pvp_mode.cooldown.region_enter");
        pvpModeRegionLeaveCooldown = config.getDouble("pvp_mode.cooldown.region_leave");
        pvpModeCombatTimeout = config.getDouble("pvp_mode.combat_timeout");
        pvpModeInvulnerabilityTimeCommand = config.getDouble("pvp_mode.invulnerability.time.command");
        pvpModeInvulnerabilityTimeRegionChange = config.getDouble("pvp_mode.invulnerability.time.region_change");
        pvpModeInvulnerabilityCanDamage = config.getBoolean("pvp_mode.invulnerability.can_damage");
        minCombatLevel = config.getInt("pvp_mode.min_level");
        maxCombatLevelDifference = config.getInt("pvp_mode.max_level_difference");
        skillTreeScrollStepX = config.getInt("skill-tree-scroll-step-x", 1);
        skillTreeScrollStepY = config.getInt("skill-tree-scroll-step-y", 1);
        // Resources
        staminaFull = getColorOrDefault("stamina-whole", ChatColor.GREEN);
        staminaHalf = getColorOrDefault("stamina-half", ChatColor.DARK_GRAY);
        staminaEmpty = getColorOrDefault("stamina-empty", ChatColor.WHITE);

        passiveSkillsNeedBinding = MMOCore.plugin.getConfig().getBoolean("passive-skill-need-bound");
        canCreativeCast = MMOCore.plugin.getConfig().getBoolean("can-creative-cast");
        cobbleGeneratorXP = MMOCore.plugin.getConfig().getBoolean("should-cobblestone-generators-give-exp");
        saveDefaultClassInfo = MMOCore.plugin.getConfig().getBoolean("save-default-class-info");
        overrideVanillaExp = MMOCore.plugin.getConfig().getBoolean("override-vanilla-exp");
    }

    @NotNull
    private ChatColor getColorOrDefault(String key, ChatColor defaultColor) {
        try {
            return ChatColor.valueOf(MMOCore.plugin.getConfig().getString("resource-bar-colors." + key).toUpperCase());
        } catch (IllegalArgumentException exception) {
            MMOCore.log(Level.WARNING, "Could not read resource bar color from '" + key + "': using default.");
            return defaultColor;
        }
    }

    @Deprecated
    public PlayerInput newPlayerInput(Player player, InputType type, Consumer<String> output) {
        return new ChatInput(player, type, null, output);
    }

    public void copyDefaultFile(String path) {
        FileUtils.copyDefaultFile(MMOCore.plugin, path);
    }

    @Deprecated
    public void loadDefaultFile(String name) {
        copyDefaultFile(name);
    }

    @Deprecated
    public void copyDefaultFile(String path, String name) {
        if (path.isEmpty()) copyDefaultFile(name);
        else copyDefaultFile(path + "/" + name);
    }

    @Deprecated
    public List<String> getMessage(String key) {
        return messages.getStringList(key);
    }

    /**
     * @return The original object, which should be cloned afterwards!!
     */
    @Nullable
    public Object getMessageObject(String key) {
        return messages.get(key);
    }

    @Deprecated
    public SimpleMessage getSimpleMessage(String key, String... placeholders) {
        SimpleMessage wrapper = new SimpleMessage(ConfigMessage.fromKey(key));
        wrapper.message.addPlaceholders(placeholders);
        return wrapper;
    }

    @Deprecated
    public static class SimpleMessage {
        private final ConfigMessage message;

        @Deprecated
        public SimpleMessage(ConfigMessage message) {
            this.message = message;
        }

        @Deprecated
        public String message() {
            return message.getLines().isEmpty() ? "" : message.getLines().get(0);
        }

        @Deprecated
        public boolean send(Player player) {
            message.send(player);
            return !message.getLines().isEmpty();
        }
    }
}
