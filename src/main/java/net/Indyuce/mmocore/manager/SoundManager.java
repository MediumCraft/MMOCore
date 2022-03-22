package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.SoundObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SoundManager implements MMOCoreManager {
    private final Map<SoundEvent, SoundObject> sounds = new HashMap<>();

    @NotNull
    public SoundObject getSound(SoundEvent event) {
        return Objects.requireNonNull(sounds.get(event), "Could not find sound for " + event.name());
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore)
            sounds.clear();

        FileConfiguration config = new ConfigFile("sounds").getConfig();
        for (SoundEvent sound : SoundEvent.values())
            sounds.put(sound, new SoundObject(config.getString(sound.name().replace("_", "-").toLowerCase())));
    }
}
