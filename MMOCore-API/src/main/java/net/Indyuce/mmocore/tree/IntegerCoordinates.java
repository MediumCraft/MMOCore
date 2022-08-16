package net.Indyuce.mmocore.tree;

import org.apache.commons.lang.Validate;

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
        return x + "." + y;
    }
}
