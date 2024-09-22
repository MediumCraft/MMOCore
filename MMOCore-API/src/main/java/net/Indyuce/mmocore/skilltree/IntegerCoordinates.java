package net.Indyuce.mmocore.skilltree;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class IntegerCoordinates {
    private final int x, y;

    public IntegerCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Deprecated
    public IntegerCoordinates(String str) {
        String[] split = str.split("\\.");
        Validate.isTrue(split.length == 2, "Invalid format");
        x = Integer.parseInt(split[0]);
        y = Integer.parseInt(split[1]);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @NotNull
    public IntegerCoordinates add(@NotNull IntegerCoordinates other) {
        return new IntegerCoordinates(x + other.x, y + other.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerCoordinates that = (IntegerCoordinates) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return new StringBuilder("(").append(x).append(", ").append(y).append(")").toString();
    }

    @NotNull
    public static IntegerCoordinates from(@Nullable Object object) {
        Validate.notNull(object, "Could not read coordinates");

        if (object instanceof ConfigurationSection) {
            final ConfigurationSection config = (ConfigurationSection) object;
            return new IntegerCoordinates(config.getInt("x"), config.getInt("y"));
        }

        if (object instanceof String) {
            final String[] split = ((String) object).split("[:.,]");
            Validate.isTrue(split.length > 1, "Must provide two coordinates, X and Y, got " + Arrays.asList(split));
            return new IntegerCoordinates(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }

        throw new RuntimeException("Needs either a string or configuration section");
    }
}
