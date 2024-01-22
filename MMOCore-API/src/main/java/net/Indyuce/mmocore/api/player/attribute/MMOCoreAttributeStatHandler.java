package net.Indyuce.mmocore.api.player.attribute;

import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;

/**
 * This fixes an issue where registering new stat modifiers in ML
 * to add extra attribute points does NOT update the stats granted
 * by the attribute.
 * <p>
 * This stat handler MAY call subsequent stat handlers. There might
 * be infinite recursion problems if another attr. grants extra attribute pts.
 */
public class MMOCoreAttributeStatHandler extends StatHandler {
    private final PlayerAttribute attr;

    public MMOCoreAttributeStatHandler(ConfigurationSection config, PlayerAttribute attr) {
        super(config, "ADDITIONAL_" + attr.getId().toUpperCase().replace("-", "_"));

        this.attr = attr;
    }

    /**
     * This method is called on login but the MMOCore playerData
     * is not loaded yet, hence the try/catch clause
     */
    @Override
    public void runUpdate(StatInstance instance) {
        try {
            final PlayerData playerData = PlayerData.get(instance.getMap().getPlayerData());
            playerData.getAttributes().getInstance(attr).updateStats();
        } catch (NullPointerException exception) {
            // Player data is not loaded yet so there's nothing to update.
        }
    }
}
