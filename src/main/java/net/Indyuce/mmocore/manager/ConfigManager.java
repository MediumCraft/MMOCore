package net.Indyuce.mmocore.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.AnvilGUI;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;

public class ConfigManager {

	public boolean overrideVanillaExp, hotbarSwap;
	public double expPartyBuff, regenPartyBuff;
	public String partyChatPrefix, noSkillBoundPlaceholder;
	public ChatColor staminaFull, staminaHalf, staminaEmpty;
	public int combatLogTimer, lootChestExpireTime;

	public final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
	public final DecimalFormat decimal = new DecimalFormat("0.#", formatSymbols), decimals = new DecimalFormat("0.##", formatSymbols);

	private FileConfiguration messages;
	private boolean chatInput;

	/*
	 * the instance must be created after the other managers since all it does
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

		loadDefaultFile("attributes.yml");
		loadDefaultFile("items.yml");
		loadDefaultFile("messages.yml");
		loadDefaultFile("stats.yml");
		loadDefaultFile("waypoints.yml");
		loadDefaultFile("restrictions.yml");
		// loadDefaultFile("chests.yml");
		loadDefaultFile("commands.yml");
		loadDefaultFile("guilds.yml");

		loadOptions();
	}

	public void loadOptions() {
		messages = new ConfigFile("messages").getConfig();
		hotbarSwap = MMOCore.plugin.getConfig().getBoolean("hotbar-swap");
		chatInput = MMOCore.plugin.getConfig().getBoolean("use-chat-input");
		partyChatPrefix = MMOCore.plugin.getConfig().getString("party.chat-prefix");
		formatSymbols.setDecimalSeparator(getFirstChar(MMOCore.plugin.getConfig().getString("number-format.decimal-separator"), ','));
		combatLogTimer = MMOCore.plugin.getConfig().getInt("combat-log.timer");
		lootChestExpireTime = Math.max(MMOCore.plugin.getConfig().getInt("loot-chest-expire-time"), 1) * 1000;
		noSkillBoundPlaceholder = getSimpleMessage("no-skill-placeholder").message();

		staminaFull = getColorOrDefault("stamina-whole", ChatColor.GREEN);
		staminaHalf = getColorOrDefault("stamina-half", ChatColor.DARK_GREEN);
		staminaEmpty = getColorOrDefault("stamina-empty", ChatColor.WHITE);
	}

	private ChatColor getColorOrDefault(String key, ChatColor defaultColor) {
		try {
			return ChatColor.valueOf(MMOCore.plugin.getConfig().getString("resource-bar-colors." + key).toUpperCase());
		} catch (IllegalArgumentException exception) {
			MMOCore.log(Level.WARNING, "Could not read resource bar color from '" + key + "': using default.");
			return defaultColor;
		}
	}

	public DecimalFormat newFormat(String pattern) {
		return new DecimalFormat(pattern, formatSymbols);
	}

	private char getFirstChar(String str, char defaultChar) {
		return str == null || str.isEmpty() ? defaultChar : str.charAt(0);
	}

	public PlayerInput newPlayerInput(Player player, InputType type, Consumer<String> output) {
		return chatInput ? new ChatInput(player, type, output) : new AnvilGUI(player, type, output);
	}

	public void loadDefaultFile(String name) {
		loadDefaultFile("", name);
	}

	public void loadDefaultFile(String path, String name) {
		File folder = new File(MMOCore.plugin.getDataFolder() + (path.isEmpty() ? "" : "/" + path));
		if (!folder.exists())
			folder.mkdir();

		File file = new File(MMOCore.plugin.getDataFolder() + (path.isEmpty() ? "" : "/" + path), name);
		if (!file.exists())
			try {
				Files.copy(MMOCore.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public List<String> getMessage(String key) {
		return messages.getStringList(key);
	}

	public SimpleMessage getSimpleMessage(String key, String... placeholders) {
		String format = messages.getString(key, "");
		for (int j = 0; j < placeholders.length - 1; j += 2)
			format = format.replace("{" + placeholders[j] + "}", placeholders[j + 1]);
		return new SimpleMessage(ChatColor.translateAlternateColorCodes('&', format));
	}

	public class SimpleMessage {
		private final String message;

		public SimpleMessage(String message) {
			this.message = message;
		}

		public String message() {
			return message.startsWith("%") ? message.substring(1) : message;
		}

		public boolean send(Player player) {
			if (!message.isEmpty()) {
				if (message.startsWith("%"))
					PlayerData.get(player.getUniqueId()).displayActionBar(message.substring(1));
				else
					player.sendMessage(message);
			}
			return !message.isEmpty();
		}
	}
}
