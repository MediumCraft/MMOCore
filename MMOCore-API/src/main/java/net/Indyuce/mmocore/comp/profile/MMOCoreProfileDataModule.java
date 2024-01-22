package net.Indyuce.mmocore.comp.profile;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.event.ProfileCreateEvent;
import fr.phoenixdevt.profiles.event.ProfileRemoveEvent;
import fr.phoenixdevt.profiles.placeholder.PlaceholderProcessor;
import fr.phoenixdevt.profiles.placeholder.PlaceholderRequest;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MMOCoreProfileDataModule implements ProfileDataModule, PlaceholderProcessor {

    @Override
    public JavaPlugin getOwningPlugin() {
        return MMOCore.plugin;
    }

    @Override
    public String getIdentifier() {
        return "mmocore";
    }

    @Override
    public ProfileDataModule getDataModule() {
        return this;
    }

    @Override
    public void processPlaceholderRequest(PlaceholderRequest placeholderRequest) {
        final PlayerData fictiveData = new PlayerData(new MMOPlayerData(placeholderRequest.getProfile().getUniqueId()));
        MMOCore.plugin.playerDataManager.loadData(fictiveData).thenRun(() -> {
            placeholderRequest.addPlaceholder("class", fictiveData.getProfess().getName());
            placeholderRequest.addPlaceholder("level", fictiveData.getLevel());

            for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll())
                placeholderRequest.addPlaceholder("attribute_" + attribute.getId().replace("-", "_"), fictiveData.getAttributes().getInstance(attribute).getBase());

            for (Profession profession : MMOCore.plugin.professionManager.getAll())
                placeholderRequest.addPlaceholder("profession_" + profession.getId().replace("-", "_"), fictiveData.getCollectionSkills().getLevel(profession));

            placeholderRequest.addPlaceholder("exp", MythicLib.plugin.getMMOConfig().decimal.format(fictiveData.getExperience()));
            placeholderRequest.addPlaceholder("exp_next_level", MythicLib.plugin.getMMOConfig().decimal.format(fictiveData.getLevelUpExperience()));

            placeholderRequest.validate();
        });
    }

    @EventHandler
    public void onProfileCreate(ProfileCreateEvent event) {
        event.validate(this);
    }

    @EventHandler
    public void onProfileDelete(ProfileRemoveEvent event) {
        event.validate(this);
    }
}
