package net.Indyuce.mmocore.util;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConfigUtils {

    public static <T> void iterateConfigSectionList(@NotNull ConfigurationSection config,
                                                    @NotNull List<T> list,
                                                    @NotNull Function<ConfigurationSection, T> subconfigHandler,
                                                    @NotNull Function<Integer, T> fill,
                                                    @NotNull BiConsumer<String, RuntimeException> errorHandler) {
        int expectedOrdinal = 1;

        for (String key : config.getKeys(false))
            try {
                final int index = Integer.parseInt(key);
                final ConfigurationSection subconfig = config.getConfigurationSection(key);
                Validate.notNull(subconfig, "Not a configuration section");

                // Replace
                if (index < expectedOrdinal) list.set(index, subconfigHandler.apply(subconfig));
                else {
                    while (expectedOrdinal < index)
                        list.add(fill.apply(expectedOrdinal++));
                    list.add(subconfigHandler.apply(subconfig));
                    expectedOrdinal++;
                }

            } catch (RuntimeException exception) {
                errorHandler.accept(key, exception);
            }
    }
}
