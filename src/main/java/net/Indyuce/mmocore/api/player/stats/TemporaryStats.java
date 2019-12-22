package net.Indyuce.mmocore.api.player.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class TemporaryStats {
	private final PlayerData playerData;
	private final Player player;

	private final Map<String, Double> stats = new HashMap<>();

	public TemporaryStats(PlayerStats stats) {
		this.playerData = stats.getData();
		this.player = playerData.getPlayer();

		for (StatType stat : StatType.values())
			this.stats.put(stat.name(), stats.getStat(stat));
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerData getData() {
		return playerData;
	}

	public double getStat(StatType stat) {
		return stats.get(stat.name());
	}

	public void damage(LivingEntity target, double value, DamageType... types) {
		MMOLib.plugin.getDamage().damage(playerData.getPlayer(), target, new AttackResult(true, value, types));
	}
}
