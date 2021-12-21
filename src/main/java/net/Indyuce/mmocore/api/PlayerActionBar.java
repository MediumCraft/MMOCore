package net.Indyuce.mmocore.api;

import java.text.DecimalFormat;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import io.lumine.mythic.lib.MythicLib;

public class PlayerActionBar extends BukkitRunnable {
	boolean initialized = false;
	
	private ActionBarConfig config;
	private DecimalFormat digit;
	
	public void reload(ConfigurationSection cfg) {		
		config = new ActionBarConfig(cfg);
		digit = MythicLib.plugin.getMMOConfig().newDecimalFormat(config.digit);

		if(!initialized && config.enabled) {
			runTaskTimer(MMOCore.plugin, 0, config.ticks);
			initialized = true;
		}
	}
	
	public long getTimeOut() {
		return config.timeout;
	}

	@Override
	public void run() {
		for (PlayerData data : PlayerData.getAll()) 
			if (data.isOnline() && !data.getPlayer().isDead() && !data.isCasting() && data.canSeeActionBar()) {
				data.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MMOCore.plugin.placeholderParser.parse(data.getPlayer(),
						MythicLib.plugin.parseColors((data.getProfess().hasActionBar() ? data.getProfess().getActionBar() : config.format)
								.replace("{health}", digit.format(data.getPlayer().getHealth()))
								.replace("{max_health}", "" + StatType.MAX_HEALTH.format(data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()))
								.replace("{mana_icon}", data.getProfess().getManaDisplay().getIcon())
								.replace("{mana}", digit.format(data.getMana()))
								.replace("{max_mana}", "" + StatType.MAX_MANA.format(data.getStats().getStat(StatType.MAX_MANA)))
								.replace("{stamina}", digit.format(data.getStamina()))
								.replace("{max_stamina}", "" + StatType.MAX_STAMINA.format(data.getStats().getStat(StatType.MAX_STAMINA)))
								.replace("{stellium}", digit.format(data.getStellium()))
								.replace("{max_stellium}", "" + StatType.MAX_STELLIUM.format(data.getStats().getStat(StatType.MAX_STELLIUM)))
								.replace("{class}", data.getProfess().getName())
								.replace("{xp}", "" + data.getExperience())
								.replace("{armor}", "" + StatType.ARMOR.format(data.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getValue()))
								.replace("{level}", "" + data.getLevel())
								.replace("{name}", data.getPlayer().getDisplayName())))));
			}
	}
	
	private static class ActionBarConfig {
		private final boolean enabled;
		private final int ticks, timeout;
		private final String digit, format;
		
		private ActionBarConfig(ConfigurationSection config) {
			enabled = config.getBoolean("enabled", false);
			timeout = config.getInt("", 60);
			digit = config.getString("decimal", "0.#");
			ticks = config.getInt("ticks-to-update", 5);
			format = config.getString("format", "please format me :c");
		}
	}
}
