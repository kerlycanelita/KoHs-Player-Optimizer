package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfig;
import com.kohs.optimizer.client.config.OptimizerConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public final class FeatureManager {
    private final BlockInteriorRenderingFeature blockInteriorRenderingFeature;
    private final PlayerMovementFeature playerMovementFeature;
    private final BlockPlacementFeature blockPlacementFeature;
    private final InteractionOptimizationFeature interactionOptimizationFeature;
    private final CombatVisualFeature combatVisualFeature;
    private final List<OptimizationFeature> allFeatures;
    private final CompatibilityState compatibilityState;

    public FeatureManager(CompatibilityState compatibilityState) {
        this.compatibilityState = compatibilityState;
        this.blockInteriorRenderingFeature = new BlockInteriorRenderingFeature(compatibilityState);
        this.playerMovementFeature = new PlayerMovementFeature(compatibilityState);
        this.blockPlacementFeature = new BlockPlacementFeature();
        this.interactionOptimizationFeature = new InteractionOptimizationFeature();
        this.combatVisualFeature = new CombatVisualFeature();
        this.allFeatures = List.of(
            blockInteriorRenderingFeature,
            playerMovementFeature,
            blockPlacementFeature,
            interactionOptimizationFeature,
            combatVisualFeature
        );
    }

    public void onClientTick(MinecraftClient client) {
        OptimizerConfig config = OptimizerConfigManager.getConfig();
        for (OptimizationFeature feature : allFeatures) {
            feature.onClientTick(client, config);
        }
    }

    public void onWorldTick(ClientWorld world, MinecraftClient client) {
        OptimizerConfig config = OptimizerConfigManager.getConfig();
        for (OptimizationFeature feature : allFeatures) {
            feature.onWorldTick(world, client, config);
        }
    }

    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter, MinecraftClient client) {
        OptimizerConfig config = OptimizerConfigManager.getConfig();
        for (OptimizationFeature feature : allFeatures) {
            feature.onHudRender(drawContext, tickCounter, client, config);
        }

        if (config.debug.debugOverlay) {
            drawDebugOverlay(drawContext, client, config);
        }
    }

    public boolean shouldSuppressInWallOverlay() {
        return blockInteriorRenderingFeature.shouldSuppressInWallOverlay(OptimizerConfigManager.getConfig());
    }

    public float attackCooldownVisualMultiplier() {
        return combatVisualFeature.attackCooldownVisualMultiplier(OptimizerConfigManager.getConfig());
    }

    private void drawDebugOverlay(DrawContext drawContext, MinecraftClient client, OptimizerConfig config) {
        if (client.textRenderer == null) {
            return;
        }

        int x = 6;
        int y = 6;
        int color = 0xDAB3FF;
        long nowMs = System.currentTimeMillis();

        String[] lines = new String[] {
            "KoHs Optimizer Debug",
            String.format(Locale.ROOT, "Inside opaque block: %s", blockInteriorRenderingFeature.isPlayerInsideOpaqueBlock()),
            String.format(Locale.ROOT, "Pending placements: %d", blockPlacementFeature.pendingCount()),
            String.format(Locale.ROOT, "Suppressed duplicates: %d", blockPlacementFeature.suppressedDuplicatePlacements()),
            String.format(Locale.ROOT, "Last left click: %d ms", nowMs - interactionOptimizationFeature.getLastLeftClickAtMs()),
            String.format(Locale.ROOT, "Last right click: %d ms", nowMs - interactionOptimizationFeature.getLastRightClickAtMs()),
            String.format(
                Locale.ROOT,
                "Mods sodium/lithium/reeses: %s/%s/%s",
                compatibilityState.sodiumLoaded(),
                compatibilityState.lithiumLoaded(),
                compatibilityState.reesesSodiumOptionsLoaded()
            ),
            String.format(Locale.ROOT, "Strict legit mode: %s", config.debug.strictLegitMode)
        };

        for (String line : lines) {
            drawContext.drawText(client.textRenderer, Text.literal(line), x, y, color, true);
            y += 10;
        }
    }
}
