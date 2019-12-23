package net.Indyuce.mmocore.comp;

import java.util.Iterator;
import java.util.function.Consumer;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.mmogroup.mmolib.api.player.MMOData;
import net.mmogroup.mmolib.api.stat.SharedStat;
import net.mmogroup.mmolib.api.stat.StatMap;

public class MMOLibHook {
	public MMOLibHook() {
		StatMap.registerUpdate(SharedStat.ARMOR, new AttributeStatHandler(Attribute.GENERIC_ARMOR, StatType.ARMOR));
		StatMap.registerUpdate(SharedStat.ARMOR_TOUGHNESS, new AttributeStatHandler(Attribute.GENERIC_ARMOR_TOUGHNESS, StatType.ARMOR));

		StatMap.registerUpdate(SharedStat.ATTACK_DAMAGE, new AttributeStatHandler(Attribute.GENERIC_ATTACK_DAMAGE, StatType.ATTACK_DAMAGE));
		StatMap.registerUpdate(SharedStat.ATTACK_SPEED, new AttributeStatHandler(Attribute.GENERIC_ATTACK_SPEED, StatType.ATTACK_SPEED));
		StatMap.registerUpdate(SharedStat.KNOCKBACK_RESISTANCE, new AttributeStatHandler(Attribute.GENERIC_KNOCKBACK_RESISTANCE, StatType.KNOCKBACK_RESISTANCE));
		StatMap.registerUpdate(SharedStat.ARMOR, new AttributeStatHandler(Attribute.GENERIC_MAX_HEALTH, StatType.MAX_HEALTH));

		Consumer<MMOData> moveSpeed = new MovementSpeedStat();
		StatMap.registerUpdate(SharedStat.MOVEMENT_SPEED, moveSpeed);
		StatMap.registerUpdate(SharedStat.SPEED_MALUS_REDUCTION, moveSpeed);
	}

	public class AttributeStatHandler implements Consumer<MMOData> {
		private final Attribute attribute;
		private final StatType stat;

		public AttributeStatHandler(Attribute attribute, StatType stat) {
			this.attribute = attribute;
			this.stat = stat;
		}

		@Override
		public void accept(MMOData data) {
			AttributeInstance ins = data.getPlayer().getAttribute(attribute);
			removeModifiers(ins);
			ins.setBaseValue(data.getMMOCore().getStats().getStat(stat));
		}

		private void removeModifiers(AttributeInstance ins) {
			for (Iterator<AttributeModifier> iterator = ins.getModifiers().iterator(); iterator.hasNext();) {
				AttributeModifier attribute = iterator.next();
				if (attribute.getName().startsWith("mmolib."))
					ins.removeModifier(attribute);
			}
		}
	}

	/*
	 * both used for the 'movement speed' and for the 'speed malus reduction'
	 * stats because the movement speed must be refreshed every time one of
	 * these stats are changed.
	 */
	public class MovementSpeedStat implements Consumer<MMOData> {

		@Override
		public void accept(MMOData data) {
			double speedMalus = MMOCore.plugin.configManager.speedMalus * (1 - data.getMMOCore().getStats().getStat(StatType.SPEED_MALUS_REDUCTION) / 100);
			double movementSpeed = data.getMMOCore().getStats().getStat(StatType.MOVEMENT_SPEED);

			for (ItemStack item : data.getPlayer().getEquipment().getArmorContents())
				if (item != null)
					if (item.getType().name().contains("IRON") || item.getType().name().contains("DIAMOND"))
						movementSpeed *= 1 - speedMalus;
			data.getPlayer().setWalkSpeed((float) Math.min(1, movementSpeed));
		}
	}
}
