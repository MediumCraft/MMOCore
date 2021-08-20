package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.provider.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EnchantItemExperienceSource extends ExperienceSource<Void> {
    private final List<Enchantment> enchants = new ArrayList<>();

    public EnchantItemExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser);

        if (config.contains("enchant"))
            for (String key : config.getString("enchant").split(","))
                enchants.add(MythicLib.plugin.getVersion().getWrapper().getEnchantmentFromString(key.toLowerCase().replace("-", "_")));
    }

    @Override
    public boolean matchesParameter(PlayerData player, Void v) {
        return true;
    }

    @Override
    public ExperienceSourceManager<EnchantItemExperienceSource> newManager() {
        return new ExperienceSourceManager<EnchantItemExperienceSource>() {

            @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
            public void a(EnchantItemEvent event) {
                PlayerData player = PlayerData.get(event.getEnchanter());
                for (EnchantItemExperienceSource source : getSources())
                    if (source.matches(player, null)) {
                        Map<Enchantment, Integer> ench = new HashMap<>(event.getEnchantsToAdd());

                        if (!source.enchants.isEmpty())
                            ench.keySet().removeIf(enchantment -> !source.enchants.contains(enchantment));

                        if (ench.isEmpty())
                            continue;

                        double exp = 0;
                        for (Entry<Enchantment, Integer> entry : ench.entrySet())
                            exp += MMOCore.plugin.enchantManager.getBaseExperience(entry.getKey()) * entry.getValue();
                        getDispenser().giveExperience(player, (int) exp, event.getEnchantBlock().getLocation());
                    }
            }
        };
    }
}
