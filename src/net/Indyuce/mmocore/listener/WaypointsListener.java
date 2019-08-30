package net.Indyuce.mmocore.listener;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.Waypoint;
import net.Indyuce.mmocore.api.math.particle.SmallParticleEffect;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;

public class WaypointsListener implements Listener {
	@EventHandler
	public void a(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (!event.isSneaking())
			return;

		Waypoint waypoint = MMOCore.plugin.waypointManager.getCurrentWaypoint(player);
		if (waypoint == null || !waypoint.hasSneakEnabled())
			return;

		PlayerData data = PlayerData.get(player);
		if (!data.hasWaypoint(waypoint)) {
			data.unlockWaypoint(waypoint);
			new SmallParticleEffect(player, Particle.SPELL_WITCH);
			player.sendMessage(MMOCore.plugin.configManager.getSimpleMessage("new-waypoint", "waypoint", waypoint.getName()));
			player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1.2f);
			return;
		}

		InventoryManager.WAYPOINTS.newInventory(data, waypoint).open();
	}
}
