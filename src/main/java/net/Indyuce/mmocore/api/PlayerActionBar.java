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
import net.mmogroup.mmolib.MMOLib;

public class PlayerActionBar extends BukkitRunnable {
	private final int ticks;
	private final DecimalFormat digit;
	private final String format;
	
	public PlayerActionBar(ConfigurationSection config) {
		digit = new DecimalFormat(config.getString("decimal"), MMOCore.plugin.configManager.formatSymbols);
		ticks = config.getInt("ticks-to-update");
		format = config.getString("format");
		
		runTaskTimer(MMOCore.plugin, 0, ticks);
	}

	@Override
	public void run() {
		for (PlayerData data : PlayerData.getAll()) 
			if (data.isOnline() && !data.getPlayer().isDead() && !data.isCasting() && data.canSeeActionBar()) {
				data.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MMOCore.plugin.placeholderParser.parse(data.getPlayer(),
						MMOLib.plugin.parseColors(new String(format)
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
}
