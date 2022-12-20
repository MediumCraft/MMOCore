package net.Indyuce.mmocore.experience.source;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

public class FromExperienceSource extends ExperienceSource {

    /**
     * Register all the children experience sources defined in experience-source.yml.
     */
    private final List<ExperienceSource> experienceSources = new ArrayList<>();

    public FromExperienceSource(ExperienceDispenser dispenser, MMOLineConfig config) {
        super(dispenser);

        config.validateKeys("source");
        List<String> list = new ConfigFile("exp-sources").getConfig().getStringList(config.getString("source"));
        Validate.isTrue(list != null && !list.isEmpty(), "There is no source matching " + config.getString("source"));
        list.stream()
                .map(MMOLineConfig::new)
                .forEach(mmoLineConfig ->
                        experienceSources.add(MMOCore.plugin.loadManager.loadExperienceSource(mmoLineConfig, dispenser)));
    }

    @Override
    public ExperienceSourceManager<FromExperienceSource> newManager() {
        return new ExperienceSourceManager<>() {

            /**
             * Used to register all the children experience sources.
             */
            @Override
            public void registerSource(FromExperienceSource source) {
                source.experienceSources.forEach(expSource -> MMOCore.plugin.experience.registerSource(expSource));
            }
        };
    }

    @Override
    public boolean matchesParameter(PlayerData player, Object obj) {
        return false;
    }
}
