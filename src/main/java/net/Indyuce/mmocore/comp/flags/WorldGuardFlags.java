package net.Indyuce.mmocore.comp.flags;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import net.Indyuce.mmocore.MMOCore;

public class WorldGuardFlags implements FlagPlugin {
	private final WorldGuard worldguard;
	private final WorldGuardPlugin worldguardPlugin;
	private final Map<String, StateFlag> flags = new HashMap<>();

	public WorldGuardFlags() {
		this.worldguard = WorldGuard.getInstance();
		this.worldguardPlugin = ((WorldGuardPlugin) Bukkit.getServer().getPluginManager()
				.getPlugin("WorldGuard"));

		FlagRegistry registry = worldguard.getFlagRegistry();
		for (CustomFlag customFlag : CustomFlag.values()) {
			StateFlag flag = new StateFlag(customFlag.getPath(), true);
			try {
				registry.register(flag);
				flags.put(customFlag.getPath(), flag);
				MMOCore.log(Level.INFO, "[FLAGDEBUG] Registered WG Flag\n"
						+ " - Info{name=" + flag.getName() + ",path=" + customFlag.getPath() + "}");
			} catch (Exception exception) {
				MMOCore.log(Level.SEVERE, "[FLAGDEBUG] FAILED to register WG Flag\n"
						+ " - Info{name=" + flag.getName() + ",path=" + customFlag.getPath() + "}");
				exception.printStackTrace();
			}
		}
	}

//	@Override
//	public boolean isPvpAllowed(Location loc) {
//		return getApplicableRegion(loc).queryState(null, Flags.PVP) != StateFlag.State.DENY;
//	}

	@Override
	public boolean isFlagAllowed(Location loc, CustomFlag customFlag) {
		return getApplicableRegion(loc).queryValue(null, flags.get(customFlag.getPath())) != StateFlag.State.DENY;
	}

	@Override
	public boolean isFlagAllowed(Player player, CustomFlag customFlag) {
		StateFlag flag = flags.get(customFlag.getPath());
		if(flag == null) MMOCore.log(Level.SEVERE, "[FLAGDEBUG] Found Null value WG Flag\n"
					+ " - Info{path=" + customFlag.getPath() + "}");
		else MMOCore.log(Level.INFO, "[FLAGDEBUG] Checking WG Flag\n"
				+ " - Info{name=" + flag.getName() + ",path=" + customFlag.getPath() + "}");
		
		return getApplicableRegion(player.getLocation()).queryValue(worldguardPlugin.wrapPlayer(player),
				flag) != StateFlag.State.DENY;
	}

	private ApplicableRegionSet getApplicableRegion(Location loc) {
		return worldguard.getPlatform().getRegionContainer().createQuery()
				.getApplicableRegions(BukkitAdapter.adapt(loc));
	}
}
