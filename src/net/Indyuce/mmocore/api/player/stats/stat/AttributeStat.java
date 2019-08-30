package net.Indyuce.mmocore.api.player.stats.stat;

import org.bukkit.attribute.Attribute;

import net.Indyuce.mmocore.api.player.PlayerData;

public class AttributeStat extends PlayerStat {
	private final Attribute attribute;

	public AttributeStat(Attribute attribute) {
		super(attribute.name().substring(8));

		this.attribute = attribute;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	@Override
	public void refresh(PlayerData player, double val) {
		player.getPlayer().getAttribute(attribute).setBaseValue(val);
	}
}
