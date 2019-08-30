package net.Indyuce.mmocore.api.experience.source.type;

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

	public void giveExperience(PlayerData player, int amount) {
		if (hasProfession())
			player.getCollectionSkills().giveExperience(profession, amount);
		else
			player.giveExperience(amount);
	}
}
