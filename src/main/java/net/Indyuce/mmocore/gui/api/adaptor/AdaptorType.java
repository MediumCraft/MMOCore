package net.Indyuce.mmocore.gui.api.adaptor;

import net.Indyuce.mmocore.gui.api.GeneratedInventory;

import java.util.function.Function;

public enum AdaptorType {
    CLASSIC_ADAPTOR(ClassicAdaptor::new),
    THREE_DIM_ADAPTOR(ThreeDimAdaptor::new);

    private final Function<GeneratedInventory,Adaptor> supplier;

    AdaptorType(Function<GeneratedInventory,Adaptor> supplier) {
        this.supplier = supplier;
    }

    public Adaptor supply(GeneratedInventory inv) {
        return this.supplier.apply(inv);
    }

}
