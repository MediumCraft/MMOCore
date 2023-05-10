package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {
    public final CommandVerbose commandVerbose = new CommandVerbose();

    public boolean overrideVanillaExp, canCreativeCast, passiveSkillNeedBound, cobbleGeneratorXP, saveDefaultClassInfo, splitMainExp, splitProfessionExp, disableQuestBossBar,
            pvpModeEnabled, pvpModeInvulnerabilityCanDamage;
    public String partyChatPrefix, noSkillBoundPlaceholder;
    public ChatColor staminaFull, staminaHalf, staminaEmpty;
    public long combatLogTimer, lootChestExpireTime, lootChestPlayerCooldown, globalSkillCooldown;
    public double lootChestsChanceWeight, dropItemsChanceWeight, fishingDropsChanceWeight, partyMaxExpSplitRange, pvpModeToggleOnCooldown, pvpModeToggleOffCooldown, pvpModeCombatCooldown,
            pvpModeCombatTimeout, pvpModeInvulnerabilityTimeRegionChange, pvpModeInvulnerabilityTimeCommand, pvpModeRegionEnterCooldown, pvpModeRegionLeaveCooldown;
    public int maxPartyLevelDifference, maxSkillSlots, minCombatLevel, maxCombatLevelDifference;
    public final List<EntityDamageEvent.DamageCause> combatLogDamageCauses = new ArrayList<>();

    private final FileConfiguration messages;

    /*
     * The instance must be created after the other managers since all it does
     * is to update them based on the config except for the classes which are
     * already loaded based on the config
     */
    public ConfigManager() {
        // loadDefaultFile("recipes", "brewing.yml");
        // loadDefaultFile("recipes", "furnace.yml");

        if (!new File(MMOCore.plugin.getDataFolder() + "/drop-tables").exists())
            loadDefaultFile("drop-tables", "example-drop-tables.yml");

        if (!new File(MMOCore.plugin.getDataFolder() + "/professions").exists()) {
            loadDefaultFile("professions", "alchemy.yml");
            loadDefaultFile("professions", "farming.yml");
            loadDefaultFile("professions", "fishing.yml");
            loadDefaultFile("professions", "mining.yml");
            loadDefaultFile("professions", "smelting.yml");
            loadDefaultFile("professions", "smithing.yml");
            loadDefaultFile("professions", "woodcutting.yml");
            loadDefaultFile("professions", "enchanting.yml");
        }

        if (!new File(MMOCore.plugin.getDataFolder() + "/quests").exists()) {
            loadDefaultFile("quests", "adv-begins.yml");
            loadDefaultFile("quests", "tutorial.yml");
            loadDefaultFile("quests", "fetch-mango.yml");
        }

        if (!new File(MMOCore.plugin.getDataFolder() + "/classes").exists()) {
            loadDefaultFile("classes", "arcane-mage.yml");
            loadDefaultFile("classes", "human.yml");
            loadDefaultFile("classes", "mage.yml");
            loadDefaultFile("classes", "marksman.yml");
            loadDefaultFile("classes", "paladin.yml");
            loadDefaultFile("classes", "rogue.yml");
            loadDefaultFile("classes", "warrior.yml");
        }

        if (!new File(MMOCore.plugin.getDataFolder() + "/expcurves").exists()) {
            loadDefaultFile("expcurves", "levels.txt");
            loadDefaultFile("expcurves", "mining.txt");
        }

        if (!new File(MMOCore.plugin.getDataFolder() + "/skill-trees").exists()) {
            loadDefaultFile("skill-trees", "combat.yml");
            loadDefaultFile("skill-trees", "mage-arcane-mage.yml");
            loadDefaultFile("skill-trees", "rogue-marksman.yml");
            loadDefaultFile("skill-trees", "warrior-paladin.yml");
            loadDefaultFile("skill-trees", "general.yml");
        }

        loadDefaultFile("attributes.yml");
        loadDefaultFile("items.yml");
        loadDefaultFile("messages.yml");
        loadDefaultFile("stats.yml");
        loadDefaultFile("waypoints.yml");
        loadDefaultFile("restrictions.yml");
        loadDefaultFile("sounds.yml");
        loadDefaultFile("loot-chests.yml");
        loadDefaultFile("exp-tables.yml");
        loadDefaultFile("exp-sources.yml");
        loadDefaultFile("triggers.yml");
        loadDefaultFile("conditions.yml");
        loadDefaultFile("guilds.yml");

        final ConfigurationSection config = MMOCore.plugin.getConfig();
        commandVerbose.reload(MMOCore.plugin.getConfig().getConfigurationSection("command-verbose"));

        messages = new ConfigFile("messages").getConfig();
        partyChatPrefix = MMOCore.plugin.getConfig().getString("party.chat-prefix");

        // Combat log
        combatLogTimer = MMOCore.plugin.getConfig().getInt("combat-log.timer") * 1000L;
        combatLogDamageCauses.clear();
        for (String key : MMOCore.plugin.getConfig().getStringList("combat-log.causes"))
            try {
                combatLogDamageCauses.add(EntityDamageEvent.DamageCause.valueOf(UtilityMethods.enumName(key)));
            } catch (Exception exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not find damage cause called '" + key + "'");
            }

        lootChestExpireTime = Math.max(MMOCore.plugin.getConfig().getInt("loot-chests.chest-expire-time"), 1) * 20;
        lootChestPlayerCooldown = (long) MMOCore.plugin.getConfig().getDouble("player-cooldown") * 1000L;
        globalSkillCooldown = MMOCore.plugin.getConfig().getLong("global-skill-cooldown") * 50;
        noSkillBoundPlaceholder = getSimpleMessage("no-skill-placeholder").message();
        lootChestsChanceWeight = MMOCore.plugin.getConfig().getDouble("chance-stat-weight.loot-chests");
        dropItemsChanceWeight = MMOCore.plugin.getConfig().getDouble("chance-stat-weight.drop-items");
        fishingDropsChanceWeight = MMOCore.plugin.getConfig().getDouble("chance-stat-weight.fishing-drops");
        maxPartyLevelDifference = MMOCore.plugin.getConfig().getInt("party.max-level-difference");
        partyMaxExpSplitRange = MMOCore.plugin.getConfig().getDouble("party.max-exp-split-range");
        splitMainExp = MMOCore.plugin.getConfig().getBoolean("party.main-exp-split");
        splitProfessionExp = MMOCore.plugin.getConfig().getBoolean("party.profession-exp-split");
        disableQuestBossBar = MMOCore.plugin.getConfig().getBoolean("mmocore-quests.disable-boss-bar");

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

        // Resources
        staminaFull = getColorOrDefault("stamina-whole", ChatColor.GREEN);
        staminaHalf = getColorOrDefault("stamina-half", ChatColor.DARK_GREEN);
        staminaEmpty = getColorOrDefault("stamina-empty", ChatColor.WHITE);

        passiveSkillNeedBound = MMOCore.plugin.getConfig().getBoolean("passive-skill-need-bound");
        canCreativeCast = MMOCore.plugin.getConfig().getBoolean("can-creative-cast");
        cobbleGeneratorXP = MMOCore.plugin.getConfig().getBoolean("should-cobblestone-generators-give-exp");
        saveDefaultClassInfo = MMOCore.plugin.getConfig().getBoolean("save-default-class-info");
        maxSkillSlots = MMOCore.plugin.getConfig().getInt("max-skill-slots");
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

    public void loadDefaultFile(String name) {
        loadDefaultFile("", name);
    }

    public void loadDefaultFile(String path, String name) {
        String newPath = "";
        if (!path.isEmpty()) {
            String[] subpaths = path.split("/");
            for (String subpath : subpaths) {
                newPath += "/" + subpath;
                File folder = new File(MMOCore.plugin.getDataFolder() + (newPath));
                if (!folder.exists()) folder.mkdir();
            }
        }

        File file = new File(MMOCore.plugin.getDataFolder() + (newPath), name);
        if (!file.exists()) try {
            Files.copy(MMOCore.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getMessage(String key) {
        return messages.getStringList(key);
    }

    /**
     * Merge with {@link net.Indyuce.mmocore.api.ConfigMessage}
     */
    @Deprecated
    public SimpleMessage getSimpleMessage(String key, String... placeholders) {
        String format = messages.getString(key, "{MessageNotFound:\"" + key + "\"}");
        for (int j = 0; j < placeholders.length - 1; j += 2)
            format = format.replace("{" + placeholders[j] + "}", placeholders[j + 1]);
        return new SimpleMessage(MythicLib.plugin.parseColors(format));
    }

    /**
     * Merge with {@link net.Indyuce.mmocore.api.ConfigMessage}
     */
    @Deprecated
    public static class SimpleMessage {
        private final String message;
        private final boolean actionbar;
        private final boolean hasPlaceholders;

        public SimpleMessage(String message) {
            this.actionbar = message.startsWith("%");
            this.message = actionbar ? message.substring(1) : message;
            this.hasPlaceholders = this.message.contains("%");
        }

        public String message() {
            return message;
        }

        public boolean send(Player player) {
            String msg = hasPlaceholders ? MMOCore.plugin.placeholderParser.parse(player, message) : message;

            if (!msg.isEmpty()) {
                if (actionbar) PlayerData.get(player.getUniqueId()).displayActionBar(msg);
                else player.sendMessage(msg);
            }
            return !msg.isEmpty();
        }
    }
}
