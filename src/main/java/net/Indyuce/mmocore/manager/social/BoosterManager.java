package net.Indyuce.mmocore.manager.social;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.Indyuce.mmocore.api.experience.Booster;
import net.Indyuce.mmocore.api.experience.Profession;

public class BoosterManager {
	private List<Booster> map = new ArrayList<>();

	public void register(Booster booster) {

		// always flush booster list to reduce future calculations
		flush();

		for (Booster active : map)
			if (active.canStackWith(booster)) {
				active.addLength(booster.getLength());
				return;
			}

		map.add(booster);
	}

	public void flush() {
		for (Iterator<Booster> iterator = map.iterator(); iterator.hasNext();) {
			Booster next = iterator.next();
			if (next.isTimedOut())
				iterator.remove();
		}
	}

	public int calculateExp(Profession profession, double exp) {
		flush();

		for (Booster booster : map)
			if (booster.getProfession() == profession)
				exp = booster.calculateExp(exp);

		return (int) exp;
	}

	public List<Booster> getBoosters() {
		flush();

		return map;
	}

	public Booster get(int index) {
		return map.get(index);
	}
}
