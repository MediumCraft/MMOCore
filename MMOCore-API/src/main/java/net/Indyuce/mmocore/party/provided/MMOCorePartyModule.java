package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.PartyModule;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import java.util.*;

public class MMOCorePartyModule implements PartyModule {
    protected final Set<Party> parties = new HashSet<>();
    protected final Map<UUID, Party> playerParties = new HashMap<>();

    public MMOCorePartyModule() {
        Bukkit.getPluginManager().registerEvents(new PartyListener(this), MMOCore.plugin);
    }

    @Deprecated
    public void registerParty(Party party) {
        parties.add(party);
    }

    /**
     * Creates and registers a new party with given owner
     */
    public Party newRegisteredParty(PlayerData owner) {
        final Party party = new Party(this, owner);
        parties.add(party);
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

    @Override
    public Party getParty(PlayerData playerData) {
        return this.playerParties.get(playerData.getUniqueId());
    }

    public void setParty(PlayerData playerData, Party party) {
        this.playerParties.put(playerData.getUniqueId(), party);
    }
}
