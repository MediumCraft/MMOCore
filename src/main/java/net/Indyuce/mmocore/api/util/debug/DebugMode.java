package net.Indyuce.mmocore.api.util.debug;

import net.Indyuce.mmocore.MMOCore;

public class DebugMode {
	public DebugMode() {
		if (MMOCore.plugin.getConfig().getBoolean("debug-action-bar.enabled"))
			new ActionBarRunnable().runTaskTimer(MMOCore.plugin, 0, 10);
	}
}
