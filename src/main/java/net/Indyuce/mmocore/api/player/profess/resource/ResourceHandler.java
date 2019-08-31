package net.Indyuce.mmocore.api.player.profess.resource;

import java.util.function.Function;
import java.util.function.Predicate;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;

public class ResourceHandler {
	private final Function<PlayerData, Double> regen;
	private final Predicate<PlayerData> available;

	public ResourceHandler(PlayerClass profess, PlayerResource resource) {
		available = profess.hasOption(resource.getOffCombatRegen()) ? (data) -> !data.isInCombat() : (data) -> true;
		regen = profess.hasOption(resource.getMaxRegen()) ? (data) -> resource.getMax(data) * data.getStats().getStat(resource.getRegenStat()) / 100 : profess.hasOption(resource.getMissingRegen()) ? (data) -> Math.max(0, resource.getMax(data) - resource.getCurrent(data)) * data.getStats().getStat(resource.getRegenStat()) / 100 : (data) -> data.getStats().getStat(resource.getRegenStat());
	}

	public boolean isAvailable(PlayerData data) {
		return available.test(data);
	}

	public double getRegen(PlayerData data) {
		return regen.apply(data);
	}
}
