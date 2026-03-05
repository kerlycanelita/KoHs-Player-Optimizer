package com.kohs.optimizer.client;

import com.kohs.optimizer.KohsPlayerOptimizerMod;
import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfigManager;
import com.kohs.optimizer.client.feature.FeatureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KohsPlayerOptimizerClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KohsPlayerOptimizerMod.MOD_ID + "/client");
    private static FeatureManager featureManager;
    private static CompatibilityState compatibilityState;

    @Override
    public void onInitializeClient() {
        OptimizerConfigManager.load();
        compatibilityState = CompatibilityState.detect();
        featureManager = new FeatureManager(compatibilityState);

        ClientTickEvents.END_CLIENT_TICK.register(featureManager::onClientTick);
        ClientTickEvents.END_WORLD_TICK.register(world -> featureManager.onWorldTick(world, MinecraftClient.getInstance()));
        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
            featureManager.onHudRender(drawContext, tickCounter, MinecraftClient.getInstance()));

        LOGGER.info(
            "KoHs Player Optimizer initialized (sodium={}, lithium={}, reeses={})",
            compatibilityState.sodiumLoaded(),
            compatibilityState.lithiumLoaded(),
            compatibilityState.reesesSodiumOptionsLoaded()
        );
    }

    public static boolean shouldSuppressInWallOverlay() {
        return featureManager != null && featureManager.shouldSuppressInWallOverlay();
    }

    public static float attackCooldownVisualMultiplier() {
        if (featureManager == null) {
            return 1.0f;
        }
        return featureManager.attackCooldownVisualMultiplier();
    }
}
