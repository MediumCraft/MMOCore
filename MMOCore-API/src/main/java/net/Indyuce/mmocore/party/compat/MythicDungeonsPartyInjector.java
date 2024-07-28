package net.Indyuce.mmocore.party.compat;

import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.party.provided.Party;
import net.playavalon.mythicdungeons.api.party.IDungeonParty;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Used to inject MMOCore party system into MythicDungeons
 */
public class MythicDungeonsPartyInjector extends MMOCorePartyModule {
    public MythicDungeonsPartyInjector() {
        MMOCore.plugin.getLogger().log(Level.INFO, "Injecting MMOCore party system into MythicDungeons");
    }

    @Override
    public Party newRegisteredParty(PlayerData owner) {
        final CustomParty party = new CustomParty(this, owner);
        parties.add(party);
        party.initDungeonParty(MMOCore.plugin);
        return party;
    }

    private static class CustomParty extends Party implements IDungeonParty {
        public CustomParty(MMOCorePartyModule module, PlayerData owner) {
            super(module, owner);
        }

        @Override
        public void addPlayer(Player player) {
            addMember(PlayerData.get(player));
        }

        @Override
        public void removePlayer(Player player) {
            removeMember(PlayerData.get(player));
        }

        @Override
        public List<Player> getPlayers() {
            return getMembers().stream().map(SynchronizedDataHolder::getPlayer).collect(Collectors.toList());
        }

        @NotNull
        @Override
        public OfflinePlayer getLeader() {
            return getOwner().getPlayer();
        }
    }
}
