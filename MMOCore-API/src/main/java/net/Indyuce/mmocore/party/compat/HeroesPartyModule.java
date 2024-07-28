package net.Indyuce.mmocore.party.compat;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.party.HeroParty;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HeroesPartyModule implements PartyModule, Listener {

    @Nullable
    @Override
    public AbstractParty getParty(PlayerData playerData) {
        final Hero hero = Heroes.getInstance().getCharacterManager().getHero(playerData.getPlayer());
        return hero.getParty() == null ? null : new CustomParty(hero.getParty());
    }

    private static class CustomParty implements AbstractParty {
        private final HeroParty party;

        public CustomParty(HeroParty party) {
            this.party = party;
        }

        @Override
        public boolean hasMember(@NotNull Player player) {
            return party.isPartyMember(player);
        }

        @Override
        public List<PlayerData> getOnlineMembers() {
            final List<PlayerData> list = new ArrayList<>();

            for (Hero hero : party.getMembers())
                try {
                    list.add(PlayerData.get(hero.getPlayer()));
                } catch (Exception ignored) {
                }

            return list;
        }

        @Override
        public int countMembers() {
            return party.getMembers().size();
        }
    }
}
