package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BlockInteriorRenderingFeature implements OptimizationFeature {
    private static final double[][] SAMPLE_OFFSETS = {
        {0.0, 0.0, 0.0},
        {0.15, 0.0, 0.0},
        {-0.15, 0.0, 0.0},
        {0.0, 0.0, 0.15},
        {0.0, 0.0, -0.15}
    };

    private final CompatibilityState compatibilityState;
    private boolean playerInsideOpaqueBlock;
    private boolean playerInsideWater;
    private boolean playerInsideLava;
    private float currentAlpha;
    private float targetAlpha;

    public BlockInteriorRenderingFeature(CompatibilityState compatibilityState) {
        this.compatibilityState = compatibilityState;
    }

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        if (!config.rendering.enabled) {
            playerInsideOpaqueBlock = false;
            playerInsideWater = false;
            playerInsideLava = false;
            currentAlpha = 0.0f;
            targetAlpha = 0.0f;
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            playerInsideOpaqueBlock = false;
            playerInsideWater = false;
            playerInsideLava = false;
            currentAlpha = 0.0f;
            targetAlpha = 0.0f;
            return;
        }

        Vec3d eyePos = player.getEyePos();

        if (config.rendering.multiPointSampling) {
            playerInsideOpaqueBlock = multiPointOpaqueCheck(client, eyePos);
        } else {
            BlockPos eyeBlockPos = BlockPos.ofFloored(eyePos);
            BlockState state = client.world.getBlockState(eyeBlockPos);
            playerInsideOpaqueBlock = !state.isAir() && state.shouldSuffocate(client.world, eyeBlockPos);
        }

        BlockPos eyeBlock = BlockPos.ofFloored(eyePos);
        playerInsideWater = client.world.getFluidState(eyeBlock).isIn(FluidTags.WATER);
        playerInsideLava = client.world.getFluidState(eyeBlock).isIn(FluidTags.LAVA);

        targetAlpha = playerInsideOpaqueBlock ? 1.0f : 0.0f;
        if (config.rendering.smoothAlphaTransition) {
            float speed = MathHelper.clamp(config.rendering.alphaTransitionSpeed, 0.01f, 1.0f);
            currentAlpha += (targetAlpha - currentAlpha) * speed;
            if (Math.abs(targetAlpha - currentAlpha) < 0.001f) {
                currentAlpha = targetAlpha;
            }
        } else {
            currentAlpha = targetAlpha;
        }
    }

    private boolean multiPointOpaqueCheck(MinecraftClient client, Vec3d eyePos) {
        int solidSamples = 0;
        int total = SAMPLE_OFFSETS.length;

        for (double[] offset : SAMPLE_OFFSETS) {
            BlockPos pos = BlockPos.ofFloored(
                eyePos.x + offset[0],
                eyePos.y + offset[1],
                eyePos.z + offset[2]
            );
            BlockState state = client.world.getBlockState(pos);
            if (!state.isAir() && state.shouldSuffocate(client.world, pos)) {
                solidSamples++;
            }
        }
        return solidSamples > total / 2;
    }

    public boolean shouldSuppressInWallOverlay(OptimizerConfig config) {
        if (!config.rendering.enabled || !config.rendering.suppressInWallOverlay) {
            return false;
        }
        if (isRenderingSuppressionDisabledByCompat(config)) {
            return false;
        }
        return playerInsideOpaqueBlock && inWallOverlayAlpha(config, 0.1f) <= 0.01f;
    }

    public float inWallOverlayAlpha(OptimizerConfig config, float vanillaAlpha) {
        if (!config.rendering.enabled) {
            return vanillaAlpha;
        }
        if (isRenderingSuppressionDisabledByCompat(config)) {
            return vanillaAlpha;
        }

        float configured = MathHelper.clamp(config.rendering.insideBlockOverlayAlpha, 0.0f, 1.0f);
        if (!config.rendering.suppressInWallOverlay) {
            return configured;
        }

        float suppressProgress = MathHelper.clamp(currentAlpha, 0.0f, 1.0f);
        return MathHelper.clamp(configured * (1.0f - suppressProgress), 0.0f, 1.0f);
    }

    public boolean shouldSuppressWaterOverlay(OptimizerConfig config) {
        return config.rendering.enabled && config.rendering.suppressWaterOverlay && playerInsideWater;
    }

    public float underwaterOverlayAlpha(OptimizerConfig config, float vanillaAlpha) {
        if (!config.rendering.enabled || !playerInsideWater) {
            return vanillaAlpha;
        }
        if (!config.rendering.suppressWaterOverlay) {
            return vanillaAlpha;
        }
        return 0.0f;
    }

    public boolean shouldSuppressLavaOverlay(OptimizerConfig config) {
        return config.rendering.enabled && config.rendering.suppressLavaOverlay && playerInsideLava;
    }

    public float lavaOverlayAlpha(OptimizerConfig config, float vanillaAlpha) {
        if (!config.rendering.enabled || !playerInsideLava) {
            return vanillaAlpha;
        }
        if (!config.rendering.suppressLavaOverlay) {
            return vanillaAlpha;
        }
        return 0.0f;
    }

    private boolean isRenderingSuppressionDisabledByCompat(OptimizerConfig config) {
        if (config.compatibility.disableRenderingHooksWithSodium && compatibilityState.sodiumLoaded()) {
            return true;
        }
        return config.compatibility.autoDisableConflictingHooks
            && compatibilityState.sodiumLoaded()
            && config.rendering.reduceOverlayWhenSodiumLoaded;
    }

    public boolean isPlayerInsideOpaqueBlock() {
        return playerInsideOpaqueBlock;
    }

    public boolean isPlayerInsideWater() {
        return playerInsideWater;
    }

    public boolean isPlayerInsideLava() {
        return playerInsideLava;
    }

    public float getCurrentAlpha() {
        return currentAlpha;
    }

    public float getTargetAlpha() {
        return targetAlpha;
    }
}
