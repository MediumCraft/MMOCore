package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;

/**
 * This fixes an issue where registering new stat modifiers in ML
 * to add extra attribute points does NOT update the stats granted
 * by the attribute.
 * <p>
 * This stat handler MAY call subsequent stat handlers. There might
 * be infinite recursion problems if another attr. grants extra attribute pts.
 */
public class MMOCoreAttributeStatHandler implements StatHandler {
    private final PlayerAttribute attr;
    private final String statName;

    public MMOCoreAttributeStatHandler(PlayerAttribute attr) {
        this.attr = attr;
        this.statName = "ADDITIONAL_" + attr.getId().toUpperCase().replace("-", "_");
    }

    public String getStat() {
        return statName;
    }

    /**
     * This method is called on login but the MMOCore playerData
     * is not loaded yet, hence the try/catch clause
     */
    @Override
    public void runUpdate(StatMap statMap) {
        try {
            final PlayerData playerData = MMOCore.plugin.dataProvider.getDataManager().get(statMap.getPlayerData().getUniqueId());
            playerData.getAttributes().getInstance(attr).updateStats();
        } catch (NullPointerException exception) {
            // Player data is not loaded yet so there's nothing to update.
        }
    }

    @Override
    public double getBaseValue(StatMap statMap) {
        return 0;
    }

    @Override
    public double getTotalValue(StatMap statMap) {
        return statMap.getStat(statName);
    }
}
