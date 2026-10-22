package de.selsus74.kilnexpansion.kiln.charcoal_pile;

import net.minecraft.util.StringRepresentable;

public enum CharcoalPilePhase implements StringRepresentable {
    NONE("none"),
    IDLE("idle"),
    BURNING("burning"),
    DONE("done");

    private final String name;
    CharcoalPilePhase(String name) { this.name = name; }

    @Override
    public String getSerializedName() {
        return name;
    }
}