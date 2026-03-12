package com.kohs.optimizer.client;

import com.kohs.optimizer.KohsPlayerOptimizerMod;
import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfigManager;
import com.kohs.optimizer.client.feature.FeatureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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

    public static boolean shouldSuppressWaterOverlay() {
        return featureManager != null && featureManager.shouldSuppressWaterOverlay();
    }

    public static boolean shouldSuppressLavaOverlay() {
        return featureManager != null && featureManager.shouldSuppressLavaOverlay();
    }

    public static float inWallOverlayAlpha(float vanillaAlpha) {
        if (featureManager == null) {
            return vanillaAlpha;
        }
        return featureManager.inWallOverlayAlpha(vanillaAlpha);
    }

    public static float underwaterOverlayAlpha(float vanillaAlpha) {
        if (featureManager == null) {
            return vanillaAlpha;
        }
        return featureManager.underwaterOverlayAlpha(vanillaAlpha);
    }

    public static float lavaOverlayAlpha(float vanillaAlpha) {
        if (featureManager == null) {
            return vanillaAlpha;
        }
        return featureManager.lavaOverlayAlpha(vanillaAlpha);
    }

    public static float attackCooldownVisualMultiplier() {
        if (featureManager == null) {
            return 1.0f;
        }
        return featureManager.attackCooldownVisualMultiplier();
    }

    public static void onEntityHit(Entity target) {
        if (featureManager != null) {
            featureManager.onEntityHit(target);
        }
    }

    public static boolean shouldAllowBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (featureManager == null) {
            return true;
        }
        return featureManager.shouldAllowBlockInteract(player, hand, hitResult);
    }
}
