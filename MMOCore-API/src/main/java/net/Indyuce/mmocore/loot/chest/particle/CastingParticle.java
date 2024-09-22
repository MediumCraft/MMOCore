package net.Indyuce.mmocore.loot.chest.particle;

import io.lumine.mythic.lib.UtilityMethods;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class CastingParticle {
    private final Consumer<Location> display;

    public CastingParticle(@NotNull ConfigurationSection config) {
        Validate.notNull(config, "Casting particle config cannot be null");

        final Particle particle = Particle.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("particle"), "Could not find particle name")));

        // Color
        if (particle.getDataType() == Particle.DustOptions.class) {
            final float size = Math.max((float) config.getDouble("size", 1), 0);
            final Color color = Color.fromRGB(config.getInt("color.red"), config.getInt("color.green"), config.getInt("color.blue"));
            display = loc -> loc.getWorld().spawnParticle(particle, loc, 1, new Particle.DustOptions(color, size));
        }

        // Material
        else if (particle.getDataType() == BlockData.class) {
            final Material material = Material.valueOf(UtilityMethods.enumName(Objects.requireNonNull(config.getString("material"), "Particle requires a block")));
            display = loc -> loc.getWorld().spawnParticle(particle, loc, 1, material.createBlockData());
        }

        // Anything else (does not work for all particles)
        else display = loc -> loc.getWorld().spawnParticle(particle, loc, 0);
    }

    public CastingParticle(Particle particle) {
        display = (loc) -> loc.getWorld().spawnParticle(particle, loc, 0);
    }

    public void display(Location loc) {
        display.accept(loc);
    }
}
