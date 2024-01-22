package net.Indyuce.mmocore.api;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Moved to MythicLib
 */
@Deprecated
public class SoundObject {

    @Nullable
    private final Sound sound;

    @Nullable
    private final String key;

    private final float volume;
    private final float pitch;

    @Deprecated
    public SoundObject(String input) {
        String[] split = input.split(",");

        Sound sound = null;
        String key = null;
        try {
            sound = Sound.valueOf(UtilityMethods.enumName(split[0]));
        } catch (Exception ignored) {
            key = split[0];
        }

        this.sound = sound;
        this.key = key != null ? key.toLowerCase() : null;


        volume = split.length > 1 ? Float.parseFloat(split[1]) : 1;
        pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1;
    }

    @Deprecated
    public SoundObject(ConfigurationSection config) {
        String input = config.getString("sound");

        Sound sound = null;
        String key = null;
        try {
            sound = Sound.valueOf(UtilityMethods.enumName(input));
        } catch (Exception ignored) {
            key = input;
        }

        this.sound = sound;
        this.key = key != null ? key.toLowerCase() : null;

        volume = (float) config.getDouble("volume", 1);
        pitch = (float) config.getDouble("pitch", 1);
    }

    /**
     * @return If this object is custom a custom sound, potentially
     * from a resource pack
     */
    @Deprecated
    public boolean isCustom() {
        return sound == null;
    }

    @Nullable
    @Deprecated
    public Sound getSound() {
        return sound;
    }

    @Nullable
    @Deprecated
    public String getKey() {
        return key;
    }

    @Deprecated
    public float getVolume() {
        return volume;
    }

    @Deprecated
    public float getPitch() {
        return pitch;
    }

    @Deprecated
    public void playTo(Player player) {
        playTo(player, volume, pitch);
    }

    @Deprecated
    public void playTo(Player player, float volume, float pitch) {
        if (isCustom())
            player.playSound(player.getLocation(), key, volume, pitch);
        else
            player.playSound(player.getLocation(), sound, volume, pitch);
    }

    @Deprecated
    public void playAt(Location loc) {
        playAt(loc, volume, pitch);
    }

    @Deprecated
    public void playAt(Location loc, float volume, float pitch) {
        if (isCustom())
            loc.getWorld().playSound(loc, key, volume, pitch);
        else
            loc.getWorld().playSound(loc, sound, volume, pitch);
    }
}
