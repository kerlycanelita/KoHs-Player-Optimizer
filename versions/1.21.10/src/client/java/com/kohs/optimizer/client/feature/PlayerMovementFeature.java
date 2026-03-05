package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

public final class PlayerMovementFeature implements OptimizationFeature {
    private final CompatibilityState compatibilityState;

    public PlayerMovementFeature(CompatibilityState compatibilityState) {
        this.compatibilityState = compatibilityState;
    }

    @Override
    public void onWorldTick(ClientWorld world, MinecraftClient client, OptimizerConfig config) {
        if (!config.playerMovement.enabled || client.player == null) {
            return;
        }

        if (config.compatibility.autoDisableConflictingHooks
            && compatibilityState.lithiumLoaded()
            && config.compatibility.disableMovementHooksWithLithium) {
            return;
        }

        float bodySmoothing = MathHelper.clamp(config.playerMovement.bodyYawSmoothing, 0.0f, 1.0f);
        float headSmoothing = MathHelper.clamp(config.playerMovement.headYawSmoothing, 0.0f, 1.0f);
        float pitchSmoothing = MathHelper.clamp(config.playerMovement.pitchSmoothing, 0.0f, 1.0f);

        for (AbstractClientPlayerEntity remotePlayer : world.getPlayers()) {
            if (remotePlayer == client.player) {
                continue;
            }

            float targetYaw = remotePlayer.getYaw();
            float targetPitch = remotePlayer.getPitch();

            float bodyYaw = MathHelper.lerp(bodySmoothing, remotePlayer.getBodyYaw(), targetYaw);
            remotePlayer.setBodyYaw(bodyYaw);

            float headYaw = MathHelper.lerp(headSmoothing, remotePlayer.getHeadYaw(), targetYaw);
            remotePlayer.setHeadYaw(headYaw);

            float pitch = MathHelper.lerp(pitchSmoothing, remotePlayer.getPitch(), targetPitch);
            remotePlayer.setPitch(pitch);
        }
    }
}
