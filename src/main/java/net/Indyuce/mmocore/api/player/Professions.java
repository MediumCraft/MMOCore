package net.Indyuce.mmocore.api.player;

import java.util.HashMap;
import java.util.Map;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.math.particle.SmallParticleEffect;

public class Professions {
	private final Map<String, Integer> exp = new HashMap<>();
	private final Map<String, Integer> level = new HashMap<>();
	private final PlayerData playerData;

	public Professions(PlayerData playerData) {
		this.playerData = playerData;
	}

	public Professions load(ConfigurationSection config) {
		for (String key : config.getKeys(false))
			if (MMOCore.plugin.professionManager.has(key)) {
				exp.put(key, config.getInt(key + ".exp"));
				level.put(key, config.getInt(key + ".level"));
			}

		return this;
	}

	public void save(ConfigurationSection config) {
		for (String id : exp.keySet())
			config.set(id + ".exp", exp.get(id));
		for (String id : level.keySet())
			config.set(id + ".level", level.get(id));
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public int getLevel(String profession) {
		return Math.max(1, level.containsKey(profession) ? level.get(profession) : 1);
	}

	public int getLevel(Profession profession) {
		return getLevel(profession.getId());
	}

	public int getExperience(Profession profession) {
		return getExperience(profession.getId());
	}

	public int getExperience(String id) {
		return exp.containsKey(id) ? exp.get(id) : 0;
	}

	public void setExperience(Profession profession, int value) {
		exp.put(profession.getId(), value);
	}

	public void setLevel(Profession profession, int value) {
		level.put(profession.getId(), value);
	}

	public void giveLevels(Profession profession, int value) {
		int total = 0, level = getLevel(profession);
		while (value-- > 0)
			total += MMOCore.plugin.configManager.getNeededExperience(level + value + 1);
		giveExperience(profession, total);
	}

	public void giveExperience(Profession profession, int value) {
		giveExperience(profession, value, null);
	}

	public void giveExperience(Profession profession, int value, Location loc) {
		value = MMOCore.plugin.boosterManager.calculateExp(profession, value);
		exp.put(profession.getId(), exp.containsKey(profession.getId()) ? exp.get(profession.getId()) + value : value);

		// display hologram
		if (MMOItems.plugin.getConfig().getBoolean("game-indicators.exp.enabled")) {
			if (loc != null && MMOCore.plugin.hologramSupport != null)
				MMOCore.plugin.hologramSupport.displayIndicator(loc.add(.5, 1.5, .5), MMOCore.plugin.configManager.getSimpleMessage("exp-hologram", "exp", "" + value).message(), playerData.getPlayer());
		}

		int needed, exp, level;
		boolean check = false;
		while ((exp = this.exp.get(profession.getId())) >= (needed = MMOCore.plugin.configManager.getNeededExperience((level = getLevel(profession)) + 1))) {
			this.exp.put(profession.getId(), exp - needed);
			this.level.put(profession.getId(), level + 1);
			check = true;
			playerData.giveExperience((int) profession.getExperience().calculate(level));
			Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(playerData, profession, level + 1));

			new ConfigMessage("profession-level-up").addPlaceholders("level", "" + (level + 1), "profession", profession.getName()).send(playerData.getPlayer());
			playerData.getPlayer().playSound(playerData.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		}
		if (check)
			new SmallParticleEffect(playerData.getPlayer(), Particle.SPELL_INSTANT);

		String bar = "" + ChatColor.BOLD;
		int chars = (int) ((double) exp / needed * 20);
		for (int j = 0; j < 20; j++)
			bar += (j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "") + "|";
		MMOCore.plugin.configManager.getSimpleMessage("exp-notification", "profession", profession.getName(), "progress", bar, "ratio", MMOCore.plugin.configManager.decimal.format((double) exp / needed * 100)).send(playerData.getPlayer());
	}
}
