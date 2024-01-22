package net.Indyuce.mmocore.command.api;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.command.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Commands which can be disabled using commands.yml
 */
public enum ToggleableCommand {
    PLAYER("player", "Displays player stats", config -> new PlayerStatsCommand(config), "p", "profile"),
    ATTRIBUTES("attributes", "Display and manage attributes", config -> new AttributesCommand(config), "att", "stats"),
    CLASS("class", "Select a new class", config -> new ClassCommand(config), "c"),
    WAYPOINTS("waypoints", "Display discovered waypoints", config -> new WaypointsCommand(config), "wp"),
    QUESTS("quests", "Display available quests", config -> new QuestsCommand(config), "q", "journal"),
    SKILLS("skills", "Spend skill points to unlock new skills", config -> new SkillsCommand(config), "s"),
    FRIENDS("friends", "Show online/offline friends", config -> new FriendsCommand(config), "f"),
    PARTY("party", "Invite players in a party to split exp", config -> new PartyCommand(config)),
    GUILD("guild", "Show players in current guild", config -> new GuildCommand(config)),
    WITHDRAW("withdraw", "Withdraw money into coins and notes", config -> new WithdrawCommand(config), v -> MMOCore.plugin.hasEconomy() && MMOCore.plugin.economy.isValid(), "w"),
    SKILL_TREES("skilltrees", "Open up the skill tree menu", config -> new SkillTreesCommand(config), "st", "trees", "tree"),
    DEPOSIT("deposit", "Open the currency deposit menu", config -> new DepositCommand(config), "d"),
    PVP_MODE("pvpmode", "Toggle on/off PVP mode.", config -> new PvpModeCommand(config), "pvp");

    private final String mainLabel;
    private final String description;
    private final Function<ConfigurationSection, RegisteredCommand> generator;
    private final List<String> aliases;
    private final Predicate<Void> enabled;

    ToggleableCommand(@NotNull String mainLabel, @NotNull String description, @NotNull Function<ConfigurationSection, RegisteredCommand> generator, @NotNull String... aliases) {
        this(mainLabel, description, generator, null, aliases);
    }

    ToggleableCommand(@NotNull String mainLabel, @NotNull String description, @NotNull Function<ConfigurationSection, RegisteredCommand> generator, @Nullable Predicate<Void> enabled, @NotNull String... aliases) {
        this.mainLabel = mainLabel;
        this.description = description;
        this.generator = generator;
        this.aliases = Arrays.asList(aliases);
        this.enabled = enabled == null ? v -> true : enabled;
    }

    public String getMainLabel() {
        return mainLabel;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getConfigPath() {
        return name().toLowerCase().replace("_", "-");
    }

    public boolean isEnabled() {
        return enabled.test(null);
    }

    public static void register() {

        // Load default config file
        if (!new File(MMOCore.plugin.getDataFolder(), "commands.yml").exists()) {
            final ConfigFile config = new ConfigFile("commands");

            for (ToggleableCommand cmd : values()) {
                final String path = cmd.getConfigPath();
                config.getConfig().set(path + ".main", cmd.mainLabel);
                config.getConfig().set(path + ".aliases", cmd.aliases);
                config.getConfig().set(path + ".description", cmd.description);
            }

            config.save();
        }


        try {

            // Find command map
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Enable commands individually
            final FileConfiguration config = new ConfigFile("commands").getConfig();
            for (ToggleableCommand cmd : values())
                if (cmd.isEnabled() && config.contains(cmd.getConfigPath()))
                    commandMap.register("mmocore", cmd.generator.apply(config.getConfigurationSection(cmd.getConfigPath())));

        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            MMOCore.plugin.getLogger().log(Level.WARNING, "Unable to register custom commands:");
            exception.printStackTrace();
        }
    }
}
