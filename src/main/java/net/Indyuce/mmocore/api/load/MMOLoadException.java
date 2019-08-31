package net.Indyuce.mmocore.api.load;

import java.util.logging.Level;

import net.Indyuce.mmocore.MMOCore;

public class MMOLoadException extends IllegalArgumentException {
	private static final long serialVersionUID = -8839506644440697800L;

	private MMOLineConfig config;

	public MMOLoadException(String message) {
		super(message);
	}

	public MMOLoadException(MMOLineConfig config, Exception exception) {
		super(exception.getMessage());

		this.config = config;
	}

	public boolean hasConfig() {
		return config != null;
	}

	public void printConsole(String prefix, String obj) {
		MMOCore.plugin.getLogger().log(Level.WARNING, "[" + prefix + "] " + (hasConfig() ? "Could not load  " + obj + " '" + config.toString() + "': " : "") + getMessage());
	}
}
