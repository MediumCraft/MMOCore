package net.Indyuce.mmocore.manager.social;

import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Party;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.manager.MMOManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;


public class PartyManager extends MMOManager {
	private final Set<Party> parties = new HashSet<>();
	private final Map<StatType, StatModifier> buffs = new HashMap<>();

	public void registerParty(Party party) {
		parties.add(party);
	}

	public Party newRegisteredParty(PlayerData owner) {
		Party party = new Party(owner);
		registerParty(party);
		return party;
	}

	public boolean isRegistered(Party party) {
		return parties.contains(party);
	}

	public void unregisterParty(Party party) {
		// IMPORTANT: clears all party stats before unregistering the party
		party.getMembers().forEach(party::removeMember);
		parties.remove(party);
	}

	public boolean hasBonus(StatType stat) {
		return buffs.containsKey(stat);
	}

	public StatModifier getBonus(StatType stat) {
		return buffs.get(stat);
	}

	public Set<StatType> getBonuses() {
		return buffs.keySet();
	}

	@Override
	public void reload() {
		ConfigurationSection config = MMOCore.plugin.getConfig().getConfigurationSection("party.buff");
		if (config != null)
			for (String key : config.getKeys(false))
				try {
					StatType stat = StatType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					buffs.put(stat, new StatModifier(config.getString(key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "Could not load party buff '" + key + "': " + exception.getMessage());
				}
	}

	@Override
	public void clear() {
		buffs.clear();
	}
}
