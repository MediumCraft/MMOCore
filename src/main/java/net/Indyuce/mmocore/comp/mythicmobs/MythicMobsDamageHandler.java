package net.Indyuce.mmocore.comp.mythicmobs;

import org.bukkit.entity.Entity;

import net.Indyuce.mmocore.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo.DamageType;

public class MythicMobsDamageHandler implements DamageHandler {

	@Override
	public DamageInfo getDamage(Entity entity) {
		return new DamageInfo(DamageType.MAGICAL);
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return entity.hasMetadata("skill-damage");
	}
}
