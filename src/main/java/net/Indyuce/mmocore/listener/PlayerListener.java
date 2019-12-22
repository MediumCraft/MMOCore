
package net.Indyuce.mmocore.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.codingforcookies.armorequip.ArmorEquipEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerAttackEvent;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.gui.api.PluginInventory;
import net.Indyuce.mmoitems.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class PlayerListener implements Listener {

	/*
	 * initialize player data
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PlayerData.setup(player).getStats().updateAll();
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
	 * this stuff on HIGH level so other plugins can check if the player just
	 * entered combat
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void d(EntityDamageByEntityEvent event) {
		if(event.getEntity().hasMetadata("NPC")) return;
		if (event.getEntity() instanceof Player)
			PlayerData.get((Player) event.getEntity()).updateCombat();

		if (event.getDamager() instanceof Player)
			PlayerData.get((Player) event.getDamager()).updateCombat();

		if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player)
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
	@EventHandler
	public void f(PlayerCombatEvent event) {
		if (!event.entersCombat())
			event.getData().getSkillData().resetData();
	}

	/*
	 * updates the player's movement speed when equipping an armor to update the
	 * speed malus reduction from the armors.
	 */
	@EventHandler
	public void g(ArmorEquipEvent event) {
		PlayerData.get(event.getPlayer()).getStats().update(StatType.MOVEMENT_SPEED);
	}

	/*
	 * apply damage modifiers.
	 */
	@EventHandler
	public void h(PlayerAttackEvent event) {
		double damage = event.getDamage();
		double d = 1, s = 1;
		
		if(MMOCore.plugin.isMILoaded())
			for (DamageType type : event.getAttackInfo().getTypes())
				s += (net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getStats()
				.getStat(AttackResult.DamageType.valueOf(type.name()).getStat()) / 100);

		damage /= s;
		
		PlayerStats stats = event.getData().getStats();
		for (DamageType type : event.getAttackInfo().getTypes())
			d += (stats.getStat((StatType) type.getMMOCoreStat())) / 100;

		event.setDamage(damage * d);
	}
}
