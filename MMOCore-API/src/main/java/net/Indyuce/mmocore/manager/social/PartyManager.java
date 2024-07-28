package net.Indyuce.mmocore.manager.social;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.MMOCoreManager;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class PartyManager implements MMOCoreManager {
    private final Set<StatModifier> buffs = new HashSet<>();

    public Set<StatModifier> getBonuses() {
        return buffs;
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            // Fixes MMOCore#1035, first remove existing buffs of players.
            PlayerData.getAll().forEach(PartyUtils::clearStatBonuses);
            buffs.clear();
        }

        ConfigurationSection config = MMOCore.plugin.getConfig().getConfigurationSection("party.buff");
        if (config != null)
            for (String key : config.getKeys(false))
                try {
                    buffs.add(new StatModifier("mmocoreParty", UtilityMethods.enumName(key), config.getString(key)));
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load party buff '" + key + "': " + exception.getMessage());
                }

        // Re-apply buffs to online players (MMOCore#1035)
        if (clearBefore)
            for (PlayerData playerData : PlayerData.getAll()) {
                AbstractParty party = MMOCore.plugin.partyModule.getParty(playerData);
                if (party != null) PartyUtils.applyStatBonuses(playerData, party.countMembers());
            }
    }
}
