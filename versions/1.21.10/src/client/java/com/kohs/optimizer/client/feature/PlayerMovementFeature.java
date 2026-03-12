package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.config.OptimizerConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

public final class PlayerMovementFeature implements OptimizationFeature {
    private final CompatibilityState compatibilityState;
    private final Int2ObjectOpenHashMap<PlayerRotationState> rotationStates = new Int2ObjectOpenHashMap<>();

    private int lastProcessedPlayers;
    private int lastJitterFilteredSamples;
    private int lastExtrapolatedSamples;

    public PlayerMovementFeature(CompatibilityState compatibilityState) {
        this.compatibilityState = compatibilityState;
    }

    @Override
    public void onWorldTick(ClientWorld world, MinecraftClient client, OptimizerConfig config) {
        if (!config.playerMovement.enabled || client.player == null) {
            rotationStates.clear();
            lastProcessedPlayers = 0;
            lastJitterFilteredSamples = 0;
            lastExtrapolatedSamples = 0;
            return;
        }

        boolean disableByLithium = compatibilityState.lithiumLoaded()
            && (
                config.compatibility.disableMovementHooksWithLithium
                    || config.compatibility.autoDisableConflictingHooks
            );
        if (disableByLithium) {
            return;
        }

        int processed = 0;
        int jitterFiltered = 0;
        int extrapolated = 0;

        for (AbstractClientPlayerEntity remotePlayer : world.getPlayers()) {
            if (remotePlayer == client.player) {
                continue;
            }
            processed++;

            int entityId = remotePlayer.getId();
            PlayerRotationState state = rotationStates.computeIfAbsent(entityId, k -> new PlayerRotationState());

            float targetYaw = remotePlayer.getYaw();
            float targetPitch = remotePlayer.getPitch();

            float distanceFactor = 1.0f;
            if (config.playerMovement.distanceAdaptiveSmoothing) {
                double distanceSq = client.player.squaredDistanceTo(remotePlayer);
                float nearSq = config.playerMovement.nearDistanceThreshold * config.playerMovement.nearDistanceThreshold;
                float farSq = config.playerMovement.farDistanceThreshold * config.playerMovement.farDistanceThreshold;

                if (distanceSq > farSq) {
                    distanceFactor = config.playerMovement.farSmoothingMultiplier;
                } else if (distanceSq > nearSq && farSq > nearSq) {
                    float t = (float) ((distanceSq - nearSq) / (farSq - nearSq));
                    distanceFactor = MathHelper.lerp(t, 1.0f, config.playerMovement.farSmoothingMultiplier);
                }
            }

            float bodySmoothing;
            float headSmoothing;
            float pitchSmoothing;
            if (config.playerMovement.useEmaSmoothing) {
                float alpha = MathHelper.clamp(config.playerMovement.emaSmoothingAlpha / distanceFactor, 0.01f, 1.0f);
                bodySmoothing = alpha;
                headSmoothing = MathHelper.clamp(alpha * 1.15f, 0.01f, 1.0f);
                pitchSmoothing = alpha;
            } else {
                bodySmoothing = MathHelper.clamp(config.playerMovement.bodyYawSmoothing / distanceFactor, 0.0f, 1.0f);
                headSmoothing = MathHelper.clamp(config.playerMovement.headYawSmoothing / distanceFactor, 0.0f, 1.0f);
                pitchSmoothing = MathHelper.clamp(config.playerMovement.pitchSmoothing / distanceFactor, 0.0f, 1.0f);
            }

            float yawDelta = Math.abs(wrapDegrees(targetYaw - state.lastYaw));
            float pitchDelta = Math.abs(targetPitch - state.lastPitch);
            float deadZone = config.playerMovement.jitterFilter ? config.playerMovement.jitterDeadZone : 0.0f;

            float smoothedBodyYaw = remotePlayer.getBodyYaw();
            float smoothedHeadYaw = remotePlayer.getHeadYaw();
            float smoothedPitch = remotePlayer.getPitch();

            if (yawDelta > deadZone) {
                smoothedBodyYaw = emaLerp(bodySmoothing, state.smoothedBodyYaw, targetYaw);
                smoothedHeadYaw = emaLerp(headSmoothing, state.smoothedHeadYaw, targetYaw);

                if (config.playerMovement.velocityExtrapolation) {
                    float velocity = wrapDegrees(targetYaw - state.lastYaw);
                    float extrapolation = velocity * MathHelper.clamp(config.playerMovement.extrapolationStrength, 0.0f, 0.5f);
                    smoothedBodyYaw += extrapolation;
                    smoothedHeadYaw += extrapolation;
                    extrapolated++;
                }
            } else if (config.playerMovement.jitterFilter) {
                jitterFiltered++;
            }

            if (pitchDelta > deadZone) {
                smoothedPitch = emaLerp(pitchSmoothing, state.smoothedPitch, targetPitch);
                if (config.playerMovement.velocityExtrapolation) {
                    float pitchVelocity = targetPitch - state.lastPitch;
                    smoothedPitch += pitchVelocity * MathHelper.clamp(config.playerMovement.extrapolationStrength, 0.0f, 0.5f);
                    extrapolated++;
                }
            } else if (config.playerMovement.jitterFilter) {
                jitterFiltered++;
            }

            smoothedPitch = MathHelper.clamp(smoothedPitch, -90.0f, 90.0f);

            remotePlayer.setBodyYaw(smoothedBodyYaw);
            remotePlayer.setHeadYaw(smoothedHeadYaw);
            remotePlayer.setPitch(smoothedPitch);

            state.smoothedBodyYaw = smoothedBodyYaw;
            state.smoothedHeadYaw = smoothedHeadYaw;
            state.smoothedPitch = smoothedPitch;
            state.lastYaw = targetYaw;
            state.lastPitch = targetPitch;
        }

        lastProcessedPlayers = processed;
        lastJitterFilteredSamples = jitterFiltered;
        lastExtrapolatedSamples = extrapolated;

        if (rotationStates.size() > world.getPlayers().size() + 10) {
            rotationStates.clear();
        }
    }

    private static float emaLerp(float alpha, float current, float target) {
        float delta = wrapDegrees(target - current);
        return current + alpha * delta;
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) wrapped -= 360.0f;
        if (wrapped < -180.0f) wrapped += 360.0f;
        return wrapped;
    }

    public int getLastProcessedPlayers() {
        return lastProcessedPlayers;
    }

    public int getLastJitterFilteredSamples() {
        return lastJitterFilteredSamples;
    }

    public int getLastExtrapolatedSamples() {
        return lastExtrapolatedSamples;
    }

    private static final class PlayerRotationState {
        float smoothedBodyYaw;
        float smoothedHeadYaw;
        float smoothedPitch;
        float lastYaw;
        float lastPitch;
    }
}
