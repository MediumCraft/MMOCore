package net.Indyuce.mmocore.manager.social;

import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.Party;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.manager.MMOCoreManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class PartyManager implements MMOCoreManager {
	private final Set<Party> parties = new HashSet<>();
	private final Set<StatModifier> buffs = new HashSet<>();

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
		// IMPORTANT: clears all party members before unregistering the party
		party.forEachMember(party::removeMember);
		Validate.isTrue(party.getMembers().isEmpty(), "Tried unregistering a non-empty party");
		parties.remove(party);
	}

	public Set<StatModifier> getBonuses() {
		return buffs;
	}

	@Override
	public void initialize(boolean clearBefore) {
		if (clearBefore)
			buffs.clear();

		ConfigurationSection config = MMOCore.plugin.getConfig().getConfigurationSection("party.buff");
		if (config != null)
			for (String key : config.getKeys(false))
				try {
					StatType stat = StatType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					buffs.add(new StatModifier("mmocoreParty", stat.name(), config.getString(key)));
				} catch (IllegalArgumentException exception) {
					MMOCore.log(Level.WARNING, "Could not load party buff '" + key + "': " + exception.getMessage());
				}
	}
}
