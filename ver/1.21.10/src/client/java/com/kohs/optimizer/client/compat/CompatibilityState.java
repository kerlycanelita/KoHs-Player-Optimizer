package com.kohs.optimizer.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public record CompatibilityState(
    boolean sodiumLoaded,
    boolean lithiumLoaded,
    boolean reesesSodiumOptionsLoaded
) {
    public static CompatibilityState detect() {
        FabricLoader loader = FabricLoader.getInstance();
        return new CompatibilityState(
            loader.isModLoaded("sodium"),
            loader.isModLoaded("lithium"),
            loader.isModLoaded("reeses-sodium-options")
        );
    }
}
