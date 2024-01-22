package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.source.type.SpecificExperienceSource;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MineBlockExperienceSource extends SpecificExperienceSource<Material> {
    private final Material material;
    private final boolean silkTouch;
    private final boolean crop;

    /**
     * Set to false by default.
     * <p>
     * When set to true, the exp source will trigger when breaking
     * blocks that were placed by players. This can be used for crops
     */
    private final boolean playerPlaced;

    public MineBlockExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser, config);

        config.validate("type");
        material = Material.valueOf(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
        silkTouch = config.getBoolean("silk-touch", true);
        crop = config.getBoolean("crop", false);
        playerPlaced = config.getBoolean("player-placed", false);
    }

    @Override
    public ExperienceSourceManager<MineBlockExperienceSource> newManager() {
        return new ExperienceSourceManager<MineBlockExperienceSource>() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void a(BlockBreakEvent event) {
                if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
                if (UtilityMethods.isFake(event)) return;

                PlayerData data = PlayerData.get(event.getPlayer());

                for (MineBlockExperienceSource source : getSources()) {
                    if (source.silkTouch && hasSilkTouch(event.getPlayer().getInventory().getItemInMainHand()))
                        continue;
                    if (source.crop && !MythicLib.plugin.getVersion().getWrapper().isCropFullyGrown(event.getBlock()))
                        continue;
                    if (!source.playerPlaced && event.getBlock().hasMetadata("player_placed"))
                        continue;

                    if (source.matches(data, event.getBlock().getType()))
                        source.giveExperience(data, 1, event.getBlock().getLocation());
                }
            }
        };
    }

    private boolean hasSilkTouch(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
    }

    @Override
    public boolean matchesParameter(PlayerData player, Material obj) {

        return material == obj;
    }
}
