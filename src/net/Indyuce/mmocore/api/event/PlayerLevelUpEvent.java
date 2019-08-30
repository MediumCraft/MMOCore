package net.Indyuce.mmocore.api.event;

import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;

public class PlayerLevelUpEvent extends PlayerDataEvent {
	private static final HandlerList handlers = new HandlerList();

	// if null, this is main level
	private final Profession profession;
	private final int level;

	public PlayerLevelUpEvent(PlayerData player, int level) {
		this(player, null, level);
	}

	public PlayerLevelUpEvent(PlayerData player, Profession profession, int level) {
		super(player);

		this.profession = profession;
		this.level = level;
	}

	public int getNewLevel() {
		return level;
	}

	public boolean hasProfession() {
		return profession != null;
	}

	public Profession getProfession() {
		return profession;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
