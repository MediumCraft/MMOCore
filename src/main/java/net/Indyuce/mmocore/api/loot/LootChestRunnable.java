package net.Indyuce.mmocore.api.loot;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;

public class LootChestRunnable extends BukkitRunnable {
	private final LootChestRegion region;

	public LootChestRunnable(LootChestRegion region) {
		this.region = region;

		runTaskTimer(MMOCore.plugin, region.getChestSpawnPeriod() * 20, region.getChestSpawnPeriod() * 20);
	}

	// TODO add option so that players cannot have more than X chests every X
	// time
	@Override
	public void run() {
		Optional<Player> found = region.getBounds().getPlayers().findAny();
		if (found.isPresent())
			region.spawnChest(PlayerData.get(found.get()));
	}
}
