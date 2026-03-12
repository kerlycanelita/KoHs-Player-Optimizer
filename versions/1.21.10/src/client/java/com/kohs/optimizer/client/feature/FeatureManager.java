package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.KohsPlayerOptimizerMod;
import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfig;
import com.kohs.optimizer.client.config.OptimizerConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FeatureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KohsPlayerOptimizerMod.MOD_ID + "/feature-manager");

    private final BlockInteriorRenderingFeature blockInteriorRenderingFeature;
    private final PlayerMovementFeature playerMovementFeature;
    private final BlockPlacementFeature blockPlacementFeature;
    private final InteractionOptimizationFeature interactionOptimizationFeature;
    private final CombatVisualFeature combatVisualFeature;
    private final List<OptimizationFeature> allFeatures;
    private final CompatibilityState compatibilityState;

    private final Map<String, Float> clientTickTimingMs = new LinkedHashMap<>();
    private final Map<String, Float> worldTickTimingMs = new LinkedHashMap<>();
    private final Map<String, Float> hudTimingMs = new LinkedHashMap<>();
    private long lastTimingLogAtMs;

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
            long start = System.nanoTime();
            feature.onClientTick(client, config);
            recordTiming(clientTickTimingMs, feature.getClass().getSimpleName(), System.nanoTime() - start);
        }
        maybeLogTimings(config);
    }

    public void onWorldTick(ClientWorld world, MinecraftClient client) {
        OptimizerConfig config = OptimizerConfigManager.getConfig();
        for (OptimizationFeature feature : allFeatures) {
            long start = System.nanoTime();
            feature.onWorldTick(world, client, config);
            recordTiming(worldTickTimingMs, feature.getClass().getSimpleName(), System.nanoTime() - start);
        }
    }

    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter, MinecraftClient client) {
        OptimizerConfig config = OptimizerConfigManager.getConfig();
        for (OptimizationFeature feature : allFeatures) {
            long start = System.nanoTime();
            feature.onHudRender(drawContext, tickCounter, client, config);
            recordTiming(hudTimingMs, feature.getClass().getSimpleName(), System.nanoTime() - start);
        }

        if (config.debug.debugOverlay) {
            drawDebugOverlay(drawContext, client, config);
        }

        if (config.debug.realtimePanelEnabled) {
            drawRealtimeDebugPanel(drawContext, client, config);
        }
    }

    public boolean shouldSuppressInWallOverlay() {
        return blockInteriorRenderingFeature.shouldSuppressInWallOverlay(OptimizerConfigManager.getConfig());
    }

    public boolean shouldSuppressWaterOverlay() {
        return blockInteriorRenderingFeature.shouldSuppressWaterOverlay(OptimizerConfigManager.getConfig());
    }

    public boolean shouldSuppressLavaOverlay() {
        return blockInteriorRenderingFeature.shouldSuppressLavaOverlay(OptimizerConfigManager.getConfig());
    }

    public float inWallOverlayAlpha(float vanillaAlpha) {
        return blockInteriorRenderingFeature.inWallOverlayAlpha(OptimizerConfigManager.getConfig(), vanillaAlpha);
    }

    public float underwaterOverlayAlpha(float vanillaAlpha) {
        return blockInteriorRenderingFeature.underwaterOverlayAlpha(OptimizerConfigManager.getConfig(), vanillaAlpha);
    }

    public float lavaOverlayAlpha(float vanillaAlpha) {
        return blockInteriorRenderingFeature.lavaOverlayAlpha(OptimizerConfigManager.getConfig(), vanillaAlpha);
    }

    public float attackCooldownVisualMultiplier() {
        return combatVisualFeature.attackCooldownVisualMultiplier(OptimizerConfigManager.getConfig());
    }

    public void onEntityHit(Entity target) {
        combatVisualFeature.onEntityHit(OptimizerConfigManager.getConfig(), target);
    }

    public boolean shouldAllowBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        return blockPlacementFeature.shouldAllowBlockInteract(player, hand, hitResult, OptimizerConfigManager.getConfig());
    }

    private void recordTiming(Map<String, Float> timingMap, String key, long elapsedNs) {
        float elapsedMs = elapsedNs / 1_000_000.0f;
        float previous = timingMap.getOrDefault(key, elapsedMs);
        float smoothed = previous * 0.8f + elapsedMs * 0.2f;
        timingMap.put(key, smoothed);
    }

    private void maybeLogTimings(OptimizerConfig config) {
        if (!config.debug.logFeatureTimings) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastTimingLogAtMs < 5000L) {
            return;
        }
        lastTimingLogAtMs = now;

        StringBuilder sb = new StringBuilder("Feature timings ms ");
        appendTimingMap(sb, "client", clientTickTimingMs);
        appendTimingMap(sb, "world", worldTickTimingMs);
        appendTimingMap(sb, "hud", hudTimingMs);
        LOGGER.info(sb.toString());
    }

    private void appendTimingMap(StringBuilder sb, String label, Map<String, Float> map) {
        sb.append('[').append(label).append(':');
        boolean first = true;
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(entry.getKey()).append('=')
                .append(String.format(Locale.ROOT, "%.3f", entry.getValue()));
        }
        sb.append("] ");
    }

    private void drawDebugOverlay(DrawContext drawContext, MinecraftClient client, OptimizerConfig config) {
        if (client.textRenderer == null) {
            return;
        }

        int x = 6;
        int y = 6;
        int headerColor = 0xA855F7;
        int valueColor = 0xDAB3FF;
        long nowMs = System.currentTimeMillis();

        drawContext.drawTextWithShadow(client.textRenderer,
            Text.literal("[KoHs Optimizer Debug]"), x, y, headerColor);
        y += 12;

        String[] lines = new String[] {
            String.format(Locale.ROOT, "Rendering inside:%s alpha:%.2f water:%s lava:%s",
                blockInteriorRenderingFeature.isPlayerInsideOpaqueBlock(),
                blockInteriorRenderingFeature.getCurrentAlpha(),
                blockInteriorRenderingFeature.isPlayerInsideWater(),
                blockInteriorRenderingFeature.isPlayerInsideLava()),
            String.format(Locale.ROOT, "Placement pending:%d suppressed:%d rtt:%.0fms confirms:%d timeouts:%d pps:%.1f",
                blockPlacementFeature.pendingCount(),
                blockPlacementFeature.suppressedDuplicatePlacements(),
                blockPlacementFeature.getEstimatedRttMs(),
                blockPlacementFeature.getServerConfirmations(),
                blockPlacementFeature.getPredictionTimeouts(),
                blockPlacementFeature.getPlacementsPerSecond()),
            String.format(Locale.ROOT, "Interaction L:%dms R:%dms latency:%.2fms buf:%d/%d dbl:%d",
                nowMs - interactionOptimizationFeature.getLastLeftClickAtMs(),
                nowMs - interactionOptimizationFeature.getLastRightClickAtMs(),
                interactionOptimizationFeature.getAverageInputLatencyMs(),
                interactionOptimizationFeature.getAttackBufferSize(),
                interactionOptimizationFeature.getUseBufferSize(),
                interactionOptimizationFeature.getDoubleClickCount()),
            String.format(Locale.ROOT, "Combat combo:%dx hits:%d cooldown:%.2f",
                combatVisualFeature.getComboCount(),
                combatVisualFeature.getRegisteredHits(),
                combatVisualFeature.getDisplayedCooldown()),
            String.format(Locale.ROOT, "Movement players:%d jitterFiltered:%d extrapolated:%d",
                playerMovementFeature.getLastProcessedPlayers(),
                playerMovementFeature.getLastJitterFilteredSamples(),
                playerMovementFeature.getLastExtrapolatedSamples()),
            String.format(Locale.ROOT, "Compat sodium:%s lithium:%s reeses:%s legit:%s",
                compatibilityState.sodiumLoaded(),
                compatibilityState.lithiumLoaded(),
                compatibilityState.reesesSodiumOptionsLoaded(),
                config.debug.strictLegitMode)
        };

        for (String line : lines) {
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(line), x, y, valueColor);
            y += 10;
        }
    }

    private void drawRealtimeDebugPanel(DrawContext drawContext, MinecraftClient client, OptimizerConfig config) {
        if (client.textRenderer == null) {
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add("KoHs realtime panel");

        if (config.debug.realtimeShowRendering) {
            lines.add("|- Rendering");
            lines.add("|  |- insideOpaque: " + blockInteriorRenderingFeature.isPlayerInsideOpaqueBlock());
            lines.add("|  |- insideWater: " + blockInteriorRenderingFeature.isPlayerInsideWater());
            lines.add("|  |- insideLava: " + blockInteriorRenderingFeature.isPlayerInsideLava());
            lines.add("|  |- alpha: " + String.format(Locale.ROOT, "%.3f", blockInteriorRenderingFeature.getCurrentAlpha()));
        }

        if (config.debug.realtimeShowMovement) {
            lines.add("|- Movement");
            lines.add("|  |- players: " + playerMovementFeature.getLastProcessedPlayers());
            lines.add("|  |- jitterFiltered: " + playerMovementFeature.getLastJitterFilteredSamples());
            lines.add("|  |- extrapolated: " + playerMovementFeature.getLastExtrapolatedSamples());
        }

        if (config.debug.realtimeShowPlacement) {
            lines.add("|- Placement");
            lines.add("|  |- pending: " + blockPlacementFeature.pendingCount());
            lines.add("|  |- suppressed: " + blockPlacementFeature.suppressedDuplicatePlacements());
            lines.add("|  |- confirms/timeouts: " + blockPlacementFeature.getServerConfirmations() + "/" + blockPlacementFeature.getPredictionTimeouts());
            lines.add("|  |- pps: " + String.format(Locale.ROOT, "%.2f", blockPlacementFeature.getPlacementsPerSecond()));
        }

        if (config.debug.realtimeShowInteraction) {
            lines.add("|- Interaction");
            lines.add("|  |- attack buffer: " + interactionOptimizationFeature.getAttackBufferSize());
            lines.add("|  |- use buffer: " + interactionOptimizationFeature.getUseBufferSize());
            lines.add("|  |- avg latency ms: " + String.format(Locale.ROOT, "%.2f", interactionOptimizationFeature.getAverageInputLatencyMs()));
            lines.add("|  |- double clicks: " + interactionOptimizationFeature.getDoubleClickCount());
        }

        if (config.debug.realtimeShowCombat) {
            lines.add("|- Combat");
            lines.add("|  |- combo: " + combatVisualFeature.getComboCount());
            lines.add("|  |- hits: " + combatVisualFeature.getRegisteredHits());
            lines.add("|  |- cooldown: " + String.format(Locale.ROOT, "%.2f", combatVisualFeature.getDisplayedCooldown()));
        }

        if (config.debug.realtimeShowCompat) {
            lines.add("|- Compat");
            lines.add("|  |- sodium: " + compatibilityState.sodiumLoaded());
            lines.add("|  |- lithium: " + compatibilityState.lithiumLoaded());
            lines.add("|  |- reeses: " + compatibilityState.reesesSodiumOptionsLoaded());
            lines.add("|  |- strictLegit: " + config.debug.strictLegitMode);
        }

        if (config.debug.realtimeShowTiming) {
            lines.add("|- Timings (ms)");
            appendTimingLines(lines, "client", clientTickTimingMs);
            appendTimingLines(lines, "world", worldTickTimingMs);
            appendTimingLines(lines, "hud", hudTimingMs);
        }

        int maxWidth = 140;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(line));
        }
        int panelW = maxWidth + 10;
        int panelH = lines.size() * 10 + 8;
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int x = MathHelper.clamp(config.debug.realtimePanelX, 2, Math.max(2, screenW - panelW - 2));
        int y = MathHelper.clamp(config.debug.realtimePanelY, 2, Math.max(2, screenH - panelH - 2));

        drawContext.fill(x, y, x + panelW, y + panelH, 0xB0150824);
        drawContext.fill(x, y, x + panelW, y + 1, 0xFFA855F7);
        drawContext.fill(x, y + panelH - 1, x + panelW, y + panelH, 0xFFA855F7);
        drawContext.fill(x, y, x + 1, y + panelH, 0xFF7733CC);
        drawContext.fill(x + panelW - 1, y, x + panelW, y + panelH, 0xFF7733CC);

        int drawY = y + 4;
        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? 0xFFF0E6FF : 0xFFDAB3FF;
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(lines.get(i)), x + 5, drawY, color);
            drawY += 10;
        }
    }

    private void appendTimingLines(List<String> lines, String label, Map<String, Float> map) {
        if (map.isEmpty()) {
            lines.add("|  |- " + label + ": (empty)");
            return;
        }
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            lines.add("|  |- " + label + "." + entry.getKey() + ": " +
                String.format(Locale.ROOT, "%.3f", entry.getValue()));
        }
    }
}
