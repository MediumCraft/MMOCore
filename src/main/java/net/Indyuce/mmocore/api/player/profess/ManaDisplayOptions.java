package net.Indyuce.mmocore.api.player.profess;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class ManaDisplayOptions {
	private final ChatColor color;
	private final String name;
	private final char barCharacter;

	public ManaDisplayOptions(ConfigurationSection config) {
		Validate.notNull(config, "Could not load mana display options");

		name = config.getString("name");
		Validate.notNull(name, "Could not load mana name");

		String format = config.getString("color").toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.notNull(format, "Could not load mana color");
		color = ChatColor.valueOf(format);

		format = config.getString("char");
		Validate.notNull(format, "Could not load mana bar character");
		Validate.notEmpty(format, "Could not load mana bar character");
		barCharacter = format.charAt(0);
	}

	public ManaDisplayOptions(ChatColor color, String name, char barCharacter) {
		Validate.notNull(color, "Color cannot be null");
		Validate.notNull(name, "name cannot be null");
		Validate.notNull(barCharacter, "barCharacter cannot be null");

		this.color = color;
		this.name = name;
		this.barCharacter = barCharacter;
	}
	
	public String getName() {
		return name;
	}

	public String generateBar(double mana, double max) {
		String format = "";
		double ratio = 20 * mana / max;
		for (double j = 1; j < 20; j++)
			format += "" + (ratio >= j ? color : ChatColor.WHITE) + barCharacter;
		return format;
	}
}
