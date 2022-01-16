package net.Indyuce.mmocore.manager;

import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.SoundObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SoundManager {
    private final Map<SoundEvent, SoundObject> sounds = new HashMap<>();

    public SoundManager(FileConfiguration config) {
        for (SoundEvent sound : SoundEvent.values())
            sounds.put(sound, new SoundObject(config.getString(sound.name().replace("_", "-").toLowerCase())));
    }

    @NotNull
    public SoundObject getSound(SoundEvent event) {
        return Objects.requireNonNull(sounds.get(event), "Could not find sound for " + event.name());
    }
}
