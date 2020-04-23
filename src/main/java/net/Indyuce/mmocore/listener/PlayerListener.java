
package net.Indyuce.mmocore.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerRegenResourceEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.gui.api.PluginInventory;

public class PlayerListener implements Listener {

	/*
	 * initialize player data
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void a(PlayerJoinEvent event) {
		MMOCore.plugin.dataProvider.getDataManager().setup(event.getPlayer());
	}

	/*
	 * custom inventories register
	 */
	@EventHandler
	public void b(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof PluginInventory)
			((PluginInventory) event.getInventory().getHolder()).whenClicked(event);
	}

	@EventHandler
	public void c(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof PluginInventory)
			((PluginInventory) event.getInventory().getHolder()).whenClosed(event);
	}

	/*
	 * updates the player's combat log data every time he hits an entity, or
	 * gets hit by an entity or a projectile sent by another entity. updates
	 * this stuff on LOW level so other plugins can check if the player just
	 * entered combat
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void d(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC"))
			PlayerData.get((Player) event.getEntity()).updateCombat();

		if (event.getDamager() instanceof Player && !event.getDamager().hasMetadata("NPC"))
			PlayerData.get((Player) event.getDamager()).updateCombat();

		if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player)
			if (!((Player) ((Projectile) event.getDamager()).getShooter()).hasMetadata("NPC"))
				PlayerData.get((Player) ((Projectile) event.getDamager()).getShooter()).updateCombat();
	}

	@EventHandler
	public void e(PlayerQuitEvent event) {
		PlayerData playerData = PlayerData.get(event.getPlayer());
		if (playerData.hasParty())
			playerData.getParty().removeMember(playerData);
	}

	/*
	 * reset skill data when leaving combat
	 */
	// @EventHandler
	// public void f(PlayerCombatEvent event) {
	// if (!event.entersCombat())
	// event.getData().getSkillData().resetData();
	// }

	/*
	 * Warning: this really is not the best way to interface with MMOCore
	 * generation. Use instead PlayerRegenResourceEvent to be able to access
	 * directly the PlayerData without an extra map lookup.
	 */
	@Deprecated
	@EventHandler(priority = EventPriority.HIGH)
	public void g(PlayerRegenResourceEvent event) {
		if (event.getResource() == PlayerResource.HEALTH) {
			EntityRegainHealthEvent bukkitEvent = new EntityRegainHealthEvent(event.getPlayer(), event.getAmount(), RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(bukkitEvent);
			event.setCancelled(bukkitEvent.isCancelled());
			event.setAmount(bukkitEvent.getAmount());
		}
	}
}
