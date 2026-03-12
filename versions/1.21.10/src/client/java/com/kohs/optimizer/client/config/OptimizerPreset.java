package com.kohs.optimizer.client.config;

public enum OptimizerPreset {
    CUSTOM("Custom"),
    CRYSTAL("Crystal PvP"),
    NETHERITE("Netherite PvP"),
    MACE("Mace PvP"),
    SWORD("Sword PvP"),
    UHC("UHC");

    private final String displayName;

    OptimizerPreset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
