package net.Indyuce.mmocore.api.experience.source.type;

import org.bukkit.Location;

import net.Indyuce.mmocore.api.experience.EXPSource;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.manager.profession.ExperienceManager;

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

	public void giveExperience(PlayerData player, int amount, Location location) {
		if (hasProfession())
			player.getCollectionSkills().giveExperience(profession, amount, location == null ? player.getPlayer().getLocation() : location, EXPSource.SOURCE);
		else
			player.giveExperience(amount, location == null ? player.getPlayer().getLocation() : location, EXPSource.SOURCE);
	}
}
