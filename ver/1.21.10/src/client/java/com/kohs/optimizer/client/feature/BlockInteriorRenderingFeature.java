package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class BlockInteriorRenderingFeature implements OptimizationFeature {
    private final CompatibilityState compatibilityState;
    private boolean playerInsideOpaqueBlock;

    public BlockInteriorRenderingFeature(CompatibilityState compatibilityState) {
        this.compatibilityState = compatibilityState;
    }

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        if (!config.rendering.enabled) {
            playerInsideOpaqueBlock = false;
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            playerInsideOpaqueBlock = false;
            return;
        }

        BlockPos eyeBlockPos = BlockPos.ofFloored(player.getEyePos());
        BlockState state = client.world.getBlockState(eyeBlockPos);
        playerInsideOpaqueBlock = !state.isAir() && state.shouldSuffocate(client.world, eyeBlockPos);
    }

    public boolean shouldSuppressInWallOverlay(OptimizerConfig config) {
        if (!config.rendering.enabled || !config.rendering.suppressInWallOverlay) {
            return false;
        }

        if (config.compatibility.autoDisableConflictingHooks
            && compatibilityState.sodiumLoaded()
            && config.rendering.reduceOverlayWhenSodiumLoaded) {
            return false;
        }

        if (config.compatibility.disableRenderingHooksWithSodium && compatibilityState.sodiumLoaded()) {
            return false;
        }

        return playerInsideOpaqueBlock;
    }

    public boolean isPlayerInsideOpaqueBlock() {
        return playerInsideOpaqueBlock;
    }
}
