package net.Indyuce.mmocore.gui.api.item;

import org.jetbrains.annotations.Nullable;

public class ErrorPlaceholders extends Placeholders {

    @Nullable
    @Override
    public String getPlaceholder(String placeholder) {
        return "???";
    }
}
