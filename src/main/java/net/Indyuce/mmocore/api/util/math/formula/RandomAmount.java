package net.Indyuce.mmocore.api.util.math.formula;

import org.apache.commons.lang.Validate;

import java.util.Random;

public class RandomAmount {
    private final double min, max;

    private static final Random random = new Random();

    public RandomAmount(double min, double max) {
        this.min = min;
        this.max = max;
        Validate.isTrue(max >= min, "Max value must be greater than min");
    }

    public RandomAmount(String value) {
        String[] split = value.split("-");
        min = Double.parseDouble(split[0]);
        max = split.length > 1 ? Double.parseDouble(split[1]) : min;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double calculate() {
        return min + random.nextDouble() * (max - min);
    }

    public int calculateInt() {
        return (int) (min + random.nextInt((int) (max - min + 1)));
    }
}
