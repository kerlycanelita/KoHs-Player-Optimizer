package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public final class BlockPlacementFeature implements OptimizationFeature {
    private final Long2LongOpenHashMap pendingPlacements = new Long2LongOpenHashMap();
    private boolean previousUsePressed;
    private long suppressedDuplicatePlacements;

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        if (!config.blockPlacement.enabled || client.player == null || client.world == null) {
            previousUsePressed = false;
            if (config.blockPlacement.clearPredictionsWhenWorldChanges) {
                pendingPlacements.clear();
            }
            return;
        }

        boolean usePressed = client.options.useKey.isPressed();
        if (usePressed && !previousUsePressed) {
            registerPlacementIntent(client, config);
        }
        previousUsePressed = usePressed;

        prunePendingPlacements(client, config, System.currentTimeMillis());
    }

    @Override
    public void onWorldTick(ClientWorld world, MinecraftClient client, OptimizerConfig config) {
        if (!config.blockPlacement.enabled) {
            return;
        }
        prunePendingPlacements(client, config, System.currentTimeMillis());
    }

    private void registerPlacementIntent(MinecraftClient client, OptimizerConfig config) {
        if (!(client.crosshairTarget instanceof BlockHitResult blockHitResult)) {
            return;
        }

        BlockPos placementPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        long packedPos = placementPos.asLong();
        long now = System.currentTimeMillis();
        long previous = pendingPlacements.getOrDefault(packedPos, 0L);

        if (previous > 0L && now - previous <= config.blockPlacement.placementDebounceMs) {
            suppressedDuplicatePlacements++;
            return;
        }

        pendingPlacements.put(packedPos, now);
    }

    private void prunePendingPlacements(MinecraftClient client, OptimizerConfig config, long nowMs) {
        if (client.world == null) {
            return;
        }

        LongIterator keyIterator = pendingPlacements.keySet().iterator();
        while (keyIterator.hasNext()) {
            long packedPos = keyIterator.nextLong();
            BlockPos blockPos = BlockPos.fromLong(packedPos);

            if (!client.world.getBlockState(blockPos).isAir()) {
                keyIterator.remove();
                continue;
            }

            long createdAt = pendingPlacements.get(packedPos);
            if (nowMs - createdAt > config.blockPlacement.predictionTimeoutMs) {
                keyIterator.remove();
            }
        }
    }

    public int pendingCount() {
        return pendingPlacements.size();
    }

    public long suppressedDuplicatePlacements() {
        return suppressedDuplicatePlacements;
    }
}
