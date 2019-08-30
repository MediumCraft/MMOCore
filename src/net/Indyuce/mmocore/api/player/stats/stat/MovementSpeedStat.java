package net.Indyuce.mmocore.api.player.stats.stat;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;

public class MovementSpeedStat extends AttributeStat {

	/*
	 * both used for the 'movement speed' and for the 'speed malus reduction'
	 * stats because the movement speed must be refreshed every time one of
	 * these stats are changed.
	 */
	public MovementSpeedStat() {
		super(Attribute.GENERIC_MOVEMENT_SPEED);
	}

	@Override
	public void refresh(PlayerData player, double val) {
		double speedMalus = MMOCore.plugin.configManager.speedMalus * (1 - player.getStats().getStat(StatType.SPEED_MALUS_REDUCTION) / 100);
		double movementSpeed = player.getStats().getStat(StatType.MOVEMENT_SPEED);

		for (ItemStack item : player.getPlayer().getEquipment().getArmorContents())
			if (item != null)
				if (item.getType().name().contains("IRON") || item.getType().name().contains("DIAMOND"))
					movementSpeed *= 1 - speedMalus;
		player.getPlayer().setWalkSpeed((float) Math.min(1, movementSpeed));
	}
}
