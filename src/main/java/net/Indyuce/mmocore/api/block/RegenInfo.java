package net.Indyuce.mmocore.api.block;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class RegenInfo {
	private final BlockType temporary;
	private final int regenTime;

	public RegenInfo(ConfigurationSection config) {
		Validate.notNull(config, "Could not read regen info config");

		temporary = config.contains("temp-block") ? MMOCore.plugin.loadManager.loadBlockType(new MMOLineConfig(config.getString("temp-block"))) : null;
		regenTime = config.getInt("time", 2 * 60 * 20);
	}

	public int getTime() {
		return regenTime;
	}

	public boolean hasTemporaryBlock() {
		return temporary != null;
	}

	public BlockType getTemporaryBlock() {
		return temporary;
	}
}
