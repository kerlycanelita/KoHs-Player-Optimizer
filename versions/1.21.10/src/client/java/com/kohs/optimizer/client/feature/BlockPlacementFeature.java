package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class BlockPlacementFeature implements OptimizationFeature {
    private final Long2LongOpenHashMap pendingPlacements = new Long2LongOpenHashMap();
    private long suppressedDuplicatePlacements;

    private long lastConfirmationMs;
    private float estimatedRttMs = 50.0f;
    private int serverConfirmations;
    private int predictionTimeouts;

    private float placementsPerSecond;
    private int recentPlacementCount;
    private long velocityWindowStartMs;
    private int lastEffectiveTimeoutMs;
    private int lastEffectiveDebounceMs;
    private static final long VELOCITY_WINDOW_MS = 2000;

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        if (!config.blockPlacement.enabled || client.player == null || client.world == null) {
            if (config.blockPlacement.clearPredictionsWhenWorldChanges) {
                pendingPlacements.clear();
            }
            return;
        }

        long now = System.currentTimeMillis();
        prunePendingPlacements(client, config, now);
        updatePlacementVelocity(now);
    }

    @Override
    public void onWorldTick(ClientWorld world, MinecraftClient client, OptimizerConfig config) {
        if (!config.blockPlacement.enabled) {
            return;
        }
        prunePendingPlacements(client, config, System.currentTimeMillis());
    }

    public boolean shouldAllowBlockInteract(
        ClientPlayerEntity player,
        Hand hand,
        BlockHitResult blockHitResult,
        OptimizerConfig config
    ) {
        if (!config.blockPlacement.enabled || player == null || player.getEntityWorld() == null) {
            return true;
        }

        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof BlockItem)) {
            return true;
        }

        BlockPos placementPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        long packedPos = placementPos.asLong();
        long now = System.currentTimeMillis();

        int effectiveDebounce = computeEffectiveDebounce(config);
        lastEffectiveDebounceMs = effectiveDebounce;
        long previous = pendingPlacements.getOrDefault(packedPos, 0L);
        if (previous > 0L && now - previous <= effectiveDebounce) {
            suppressedDuplicatePlacements++;
            return false;
        }

        int maxPending = Math.max(4, config.blockPlacement.maxPendingPredictions);
        while (pendingPlacements.size() >= maxPending) {
            evictOldestPending();
        }

        pendingPlacements.put(packedPos, now);
        recentPlacementCount++;
        return true;
    }

    private int computeEffectiveDebounce(OptimizerConfig config) {
        int base = Math.max(0, config.blockPlacement.placementDebounceMs);

        if (config.blockPlacement.velocityTracking && placementsPerSecond > 5.0f) {
            float scale = MathHelper.clamp(1.0f - (placementsPerSecond - 5.0f) * 0.05f, 0.4f, 1.0f);
            return Math.max(20, (int) (base * scale));
        }
        return base;
    }

    private void prunePendingPlacements(MinecraftClient client, OptimizerConfig config, long nowMs) {
        if (client.world == null) {
            return;
        }

        int effectiveTimeout = computeEffectiveTimeout(config);
        lastEffectiveTimeoutMs = effectiveTimeout;

        LongIterator keyIterator = pendingPlacements.keySet().iterator();
        while (keyIterator.hasNext()) {
            long packedPos = keyIterator.nextLong();
            BlockPos blockPos = BlockPos.fromLong(packedPos);
            long createdAt = pendingPlacements.get(packedPos);

            if (!client.world.getBlockState(blockPos).isAir()) {
                keyIterator.remove();
                serverConfirmations++;
                updateRttEstimate(nowMs, createdAt);
                continue;
            }

            if (nowMs - createdAt > effectiveTimeout) {
                keyIterator.remove();
                predictionTimeouts++;
            }
        }
    }

    private int computeEffectiveTimeout(OptimizerConfig config) {
        if (!config.blockPlacement.adaptiveTimeout) {
            return config.blockPlacement.predictionTimeoutMs;
        }
        float multiplier = MathHelper.clamp(config.blockPlacement.adaptiveTimeoutMultiplier, 1.0f, 4.0f);
        int adaptive = (int) (estimatedRttMs * multiplier);
        return Math.max(config.blockPlacement.predictionTimeoutMs, Math.min(adaptive, 2000));
    }

    private void updateRttEstimate(long nowMs, long sentMs) {
        float rtt = (float) (nowMs - sentMs);
        if (rtt > 0 && rtt < 5000) {
            estimatedRttMs = estimatedRttMs * 0.8f + rtt * 0.2f;
            lastConfirmationMs = nowMs;
        }
    }

    private void evictOldestPending() {
        long oldestTime = Long.MAX_VALUE;
        long oldestKey = 0L;
        for (var entry : pendingPlacements.long2LongEntrySet()) {
            if (entry.getLongValue() < oldestTime) {
                oldestTime = entry.getLongValue();
                oldestKey = entry.getLongKey();
            }
        }
        if (oldestTime != Long.MAX_VALUE) {
            pendingPlacements.remove(oldestKey);
        }
    }

    private void updatePlacementVelocity(long nowMs) {
        if (nowMs - velocityWindowStartMs > VELOCITY_WINDOW_MS) {
            if (velocityWindowStartMs > 0) {
                float windowSec = (nowMs - velocityWindowStartMs) / 1000.0f;
                placementsPerSecond = windowSec > 0 ? recentPlacementCount / windowSec : 0.0f;
            }
            velocityWindowStartMs = nowMs;
            recentPlacementCount = 0;
        }
    }

    public int pendingCount() {
        return pendingPlacements.size();
    }

    public long suppressedDuplicatePlacements() {
        return suppressedDuplicatePlacements;
    }

    public float getEstimatedRttMs() {
        return estimatedRttMs;
    }

    public int getServerConfirmations() {
        return serverConfirmations;
    }

    public int getPredictionTimeouts() {
        return predictionTimeouts;
    }

    public float getPlacementsPerSecond() {
        return placementsPerSecond;
    }

    public int getLastEffectiveTimeoutMs() {
        return lastEffectiveTimeoutMs;
    }

    public int getLastEffectiveDebounceMs() {
        return lastEffectiveDebounceMs;
    }

    public long getLastConfirmationMs() {
        return lastConfirmationMs;
    }
}
