package net.Indyuce.mmocore.api.util.debug;

import net.Indyuce.mmocore.MMOCore;

public class DebugMode {
	/*
	 * Debug Levels:
	 * 1:
	 *   - Print WorldGuard Flag Registry
	 * 2:
	 *   - Print Profession Trigger Things
	 * 3:
	 *   - Debug Action Bar
	 */
	public static int level = 0;
	
	public DebugMode(int i) {
		level = i;
		if (level > 2 && MMOCore.plugin.getConfig().getBoolean("debug-action-bar.enabled"))
			new ActionBarRunnable().runTaskTimer(MMOCore.plugin, 0, 10);
	}
}
