package net.Indyuce.mmocore.util;

import net.Indyuce.mmocore.MMOCore;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class FileUtils {

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

    public static void loadObjectsFromFolder(@NotNull Plugin plugin,
                                             @NotNull String path,
                                             boolean singleObject,
                                             @NotNull BiConsumer<String, ConfigurationSection> action,
                                             @NotNull String errorMessageFormat) {

        // Action to perform
        final Consumer<File> fileAction = file -> {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (singleObject) try {
                final String name = file.getName().substring(0, file.getName().length() - 4);
                action.accept(name, config);
            } catch (Throwable throwable) {
                MMOCore.plugin.getLogger().log(Level.WARNING, errorMessageFormat.formatted(file.getName(), throwable.getMessage()));
            }
            else for (String key : config.getKeys(false))
                try {
                    action.accept(key, config.getConfigurationSection(key));
                } catch (Throwable throwable) {
                    MMOCore.plugin.getLogger().log(Level.WARNING, errorMessageFormat.formatted(key, file.getName(), throwable.getMessage()));
                }
        };

        // Perform on all paths
        exploreFolderRecursively(getFile(plugin, path), fileAction);
    }

    public static void loadObjectsFromFolderRaw(@NotNull Plugin plugin,
                                                @NotNull String path,
                                                @NotNull Consumer<File> action,
                                                @NotNull String errorMessageFormat) {

        // Action to perform
        final Consumer<File> fileAction = file -> {
            try {
                action.accept(file);
            } catch (Throwable throwable) {
                MMOCore.plugin.getLogger().log(Level.WARNING, errorMessageFormat.formatted(file.getName(), throwable.getMessage()));
            }
        };

        // Perform on all paths
        exploreFolderRecursively(getFile(plugin, path), fileAction);
    }

    private static void exploreFolderRecursively(@Nullable File file, @NotNull Consumer<File> action) {
        if (!file.exists()) return;
        if (file.isFile()) action.accept(file);
        else Arrays.stream(file.listFiles()).sorted().forEach(subfile -> exploreFolderRecursively(subfile, action));
    }

    @NotNull
    public static File getFile(@NotNull Plugin plugin, @NotNull String path) {
        return new File(plugin.getDataFolder() + "/" + path);
    }

    public static boolean moveIfExists(@NotNull Plugin plugin,
                                       @NotNull String filePath,
                                       @NotNull String newFolderPath) {
        final File existing = getFile(plugin, filePath);
        final boolean result = existing.exists();
        if (result) {
            final String fullPath = newFolderPath + "/" + filePath;
            mkdirFolders(plugin, fullPath);
            Validate.isTrue(existing.renameTo(getFile(plugin, fullPath)), "Could not move '%s' to '%s'".formatted(filePath, newFolderPath));
        }
        return result;
    }

    private static void mkdirFolders(@NotNull Plugin plugin, @NotNull String fullPath) {
        String currentPath = "";
        final String[] subpaths = fullPath.split("/");
        for (int i = 0; i < subpaths.length - 1; i++) {
            currentPath += "/" + subpaths[i];
            getFile(plugin, currentPath).mkdir();
        }
    }

    public static void copyDefaultFile(@NotNull Plugin plugin, @NotNull String path) {
        mkdirFolders(plugin, path);

        final File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) try {
            Files.copy(MMOCore.plugin.getResource("default/" + path), file.getAbsoluteFile().toPath());
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not load default file '" + path + "'", throwable);
        }
    }
}
