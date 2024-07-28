package net.Indyuce.mmocore.api.block;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockType {

    /**
     * Called when placing temporary blocks
     */
    void place(@NotNull BlockInfo.RegeneratingBlock placed);

    /**
     * Called when regenerating an older block with block regen
     */
    void regenerate(@NotNull BlockInfo.RegeneratingBlock regenerating);

    @NotNull String display();

    /**
     * Applies some extra break restrictions; returns TRUE if the block can be
     * broken. This method is used to prevent non mature crops from being broken
     * for example
     */
    boolean breakRestrictions(@NotNull Block block);

    int hashCode();

    boolean equals(@Nullable Object obj);
}
