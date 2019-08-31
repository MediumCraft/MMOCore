package net.Indyuce.mmocore.comp.citizens;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import net.citizensnpcs.api.npc.NPC;

public class CitizenInteractEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();

	private NPC npc;

	public CitizenInteractEvent(Player who, NPC npc) {
		super(who);
		this.npc = npc;
	}

	public NPC getNPC() {
		return npc;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
