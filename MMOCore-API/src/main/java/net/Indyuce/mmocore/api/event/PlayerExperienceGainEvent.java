package net.Indyuce.mmocore.api.event;

import javax.annotation.Nullable;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.Indyuce.mmocore.experience.Profession;

public class PlayerExperienceGainEvent extends PlayerDataEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	// if null, this is main experience
	private final Profession profession;
	private final EXPSource source;

	private double experience;
	private boolean cancelled;

	public PlayerExperienceGainEvent(PlayerData player, double experience, EXPSource source) {
		this(player, null, experience, source);
	}

	public PlayerExperienceGainEvent(PlayerData player, @Nullable Profession profession, double experience, EXPSource source) {
		super(player);

		this.profession = profession;
		this.experience = experience;
		this.source = source;
	}

	public double getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean hasProfession() {
		return profession != null;
	}

	public Profession getProfession() {
		return profession;
	}

	public EXPSource getSource() {
		return source;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
