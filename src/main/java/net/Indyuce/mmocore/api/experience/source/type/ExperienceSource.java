package net.Indyuce.mmocore.api.experience.source.type;

import javax.annotation.Nullable;

import org.bukkit.Location;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.EXPSource;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.Profession.ProfessionOption;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;

/**
 * Atrocious API that really needs rewriting
 * 
 * @author cympe
 */
public abstract class ExperienceSource<T> {
	private final Profession profession;
	private PlayerClass profess;

	public ExperienceSource(Profession profession) {
		this(profession, null);
	}

	public ExperienceSource(PlayerClass profess) {
		this(null, profess);
	}

	public ExperienceSource(Profession profession, PlayerClass profess) {
		this.profession = profession;
		this.profess = profess;
	}

	// TODO remove setter
	public void setClass(PlayerClass profess) {
		this.profess = profess;
	}

	public boolean hasRightClass(PlayerData data) {
		return profess == null || profess.equals(data.getProfess());
	}

	public boolean hasProfession() {
		return profession != null;
	}

	public boolean hasClass() {
		return profess != null;
	}

	public abstract ExperienceManager<?> newManager();

	public abstract boolean matches(PlayerData player, T obj);

	/**
	 * Gives experience to the right profession/class
	 * 
	 * @param player           Player to give exp to
	 * @param amount           Amount of experience given
	 * @param hologramLocation If location is null the default location will be
	 *                         the player's torso
	 */
	public void giveExperience(PlayerData player, int amount, @Nullable Location hologramLocation) {
		if (hasProfession()) {
			hologramLocation = !profession.getOption(ProfessionOption.EXP_HOLOGRAMS) ? null
					: hologramLocation == null ? getPlayerLocation(player) : hologramLocation;
			player.getCollectionSkills().giveExperience(profession, amount, EXPSource.SOURCE, hologramLocation);
		} else {
			hologramLocation = !MMOCore.plugin.getConfig().getBoolean("display-main-class-exp-holograms") ? null
					: hologramLocation == null ? getPlayerLocation(player) : hologramLocation;
			player.giveExperience(amount, EXPSource.SOURCE, hologramLocation);
		}
	}

	private Location getPlayerLocation(PlayerData player) {
		return player.isOnline() ? player.getPlayer().getLocation() : null;
	}
}
