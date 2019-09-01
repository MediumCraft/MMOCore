package net.Indyuce.mmocore.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.input.AnvilGUI;
import net.Indyuce.mmocore.api.input.ChatInput;
import net.Indyuce.mmocore.api.input.PlayerInput;
import net.Indyuce.mmocore.api.input.PlayerInput.InputType;

public class ConfigManager {

	public float speedMalus;
	public boolean overrideVanillaExp, hotbarSwap;
	public double expPartyBuff, regenPartyBuff;
	public String partyChatPrefix;

	private List<Integer> neededExp = new ArrayList<>();
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

		loadDefaultFile("attributes.yml");
		loadDefaultFile("items.yml");
		loadDefaultFile("messages.yml");
		loadDefaultFile("levels.txt");
		loadDefaultFile("stats.yml");
		loadDefaultFile("waypoints.yml");
		loadDefaultFile("restrictions.yml");
		loadDefaultFile("chests.yml");

		loadOptions();
	}

	public void loadOptions() {
		speedMalus = (float) MMOCore.plugin.getConfig().getDouble("heavy-armors.speed-malus") / 100;
		messages = new ConfigFile("messages").getConfig();
		hotbarSwap = MMOCore.plugin.getConfig().getBoolean("hotbar-swap");
		chatInput = MMOCore.plugin.getConfig().getBoolean("use-chat-input");
		expPartyBuff = MMOCore.plugin.getConfig().getDouble("party.buff.experience");
		regenPartyBuff = MMOCore.plugin.getConfig().getDouble("party.buff.health-regen");
		partyChatPrefix = MMOCore.plugin.getConfig().getString("party.chat-prefix");

		neededExp.clear();
		int line = 0;
		try {
			line++;
			File txt = new File(MMOCore.plugin.getDataFolder(), "levels.txt");
			BufferedReader reader = new BufferedReader(new FileReader(txt));
			String readLine;
			while ((readLine = reader.readLine()) != null)
				neededExp.add(Integer.valueOf(readLine));
			reader.close();
		} catch (IOException | IllegalArgumentException e) {
			MMOCore.plugin.getLogger().log(Level.SEVERE, "Could not read line " + line + " from levels.txt");
			e.printStackTrace();
		}
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

	public int getNeededExperience(int level) {
		return neededExp.get(level - 1 >= neededExp.size() ? neededExp.size() - 1 : level - 1);
	}

	public List<String> getMessage(String key) {
		return messages.getStringList(key);
	}

	public String getSimpleMessage(String key, String... placeholders) {
		String format = messages.getString(key);
		for (int j = 0; j < placeholders.length - 1; j += 2)
			format = format.replace("{" + placeholders[j] + "}", placeholders[j + 1]);
		return ChatColor.translateAlternateColorCodes('&', format);
	}
}
