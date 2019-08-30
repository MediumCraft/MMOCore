package net.Indyuce.mmocore.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmocore.comp.rpg.damage.DamageInfo.DamageType;

public class DamageManager implements Listener, DamageHandler {
	private final Map<Integer, DamageInfo> customDamage = new HashMap<>();
	private final List<DamageHandler> handlers = new ArrayList<>();

	public DamageManager() {
		handlers.add(this);
	}

	public void registerHandler(DamageHandler handler) {
		handlers.add(handler);
	}

	public void damage(PlayerData data, LivingEntity target, double value, DamageType... types) {
		customDamage.put(target.getEntityId(), new DamageInfo(value, types));
		target.damage(value, data.getPlayer());
	}

	@Override
	public DamageInfo getDamage(Entity entity) {
		return customDamage.get(entity.getEntityId());
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return customDamage.containsKey(entity.getEntityId());
	}

	@Deprecated
	public boolean isCustomDamaged(Entity target) {
		return findInfo(target) != null;
	}

	public DamageInfo findInfo(Entity target) {

		for (DamageHandler handler : handlers)
			if (handler.hasDamage(target))
				return handler.getDamage(target);

		/*
		 * any non registered damage source is considered weapon
		 */
		return new DamageInfo(DamageType.WEAPON);
	}

	/*
	 * remove custom damage info as soon as the event is finished to save
	 * memory. if ignore cancelled, some can remain
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void a(EntityDamageByEntityEvent event) {
		customDamage.remove(event.getEntity().getEntityId());
	}
}
