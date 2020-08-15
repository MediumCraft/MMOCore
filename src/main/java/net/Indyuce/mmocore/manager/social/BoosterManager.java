package net.Indyuce.mmocore.manager.social;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.Indyuce.mmocore.api.experience.Booster;
import net.Indyuce.mmocore.api.experience.Profession;

public class BoosterManager {
	private final List<Booster> map = new ArrayList<>();

	/**
	 * If MMOCore can find a booster with the same profession and value, the two
	 * boosters will stack to reduce the amount of boosters displayed at the
	 * same time. Otherwise, booster is registered
	 * 
	 * @param booster
	 *            Booster to register
	 */
	public void register(Booster booster) {

		// flushes booster list to reduce future calculations
		flush();

		for (Booster active : map)
			if (active.canStackWith(booster)) {
				active.addLength(booster.getLength());
				return;
			}

		map.add(booster);
	}

	public Booster get(int index) {
		return map.get(index);
	}

	/**
	 * Cleans timed out boosters from the MMOCore registry
	 */
	public void flush() {
		for (Iterator<Booster> iterator = map.iterator(); iterator.hasNext();) {
			Booster next = iterator.next();
			if (next.isTimedOut())
				iterator.remove();
		}
	}

	/**
	 * @return Sums all current experience boosters values
	 */
	public double getMultiplier(Profession profession) {
		double d = 1;

		for (Booster booster : map)
			if (booster.getProfession() == profession && !booster.isTimedOut())
				d += booster.getExtra();

		return d;
	}

	public int calculateExp(Profession profession, double exp) {
		return (int) (exp * getMultiplier(profession));
	}

	/**
	 * @return Collection of currently registered boosters. Some of them can be
	 *         expired but are not unregistered yet!
	 */
	public List<Booster> getBoosters() {
		return map;
	}
}
