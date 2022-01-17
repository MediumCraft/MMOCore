package net.Indyuce.mmocore.party.dungeon;

import de.erethon.dungeonsxl.DungeonsXL;
import de.erethon.dungeonsxl.api.player.PlayerGroup;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.PartyModule;

public class DungeonsPartyModule implements PartyModule<DungeonsParty> {

    @Override
    public DungeonsParty getParty(PlayerData playerData) {
        PlayerGroup group = DungeonsXL.getInstance().getPlayerGroup(playerData.getPlayer());
        return group == null ? null : new DungeonsParty(group);
    }
}
