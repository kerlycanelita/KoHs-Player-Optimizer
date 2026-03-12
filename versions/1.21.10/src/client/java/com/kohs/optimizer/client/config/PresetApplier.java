package com.kohs.optimizer.client.config;

import net.minecraft.util.math.MathHelper;

public final class PresetApplier {
    private PresetApplier() {
    }

    public static void ensureCustomSnapshot(OptimizerConfig config) {
        config.normalize();
        if (!config.presets.customSnapshotInitialized) {
            captureCustomSnapshot(config);
            config.presets.customSnapshotInitialized = true;
        }
    }

    public static void selectPreset(OptimizerConfig config, OptimizerPreset preset) {
        ensureCustomSnapshot(config);
        OptimizerPreset current = config.presets.activePreset;
        if (current == preset) {
            return;
        }

        if (current == OptimizerPreset.CUSTOM) {
            captureCustomSnapshot(config);
        }

        if (preset == OptimizerPreset.CUSTOM) {
            restoreCustomSnapshot(config);
        } else {
            applyPreset(config, preset);
        }

        config.presets.activePreset = preset;
    }

    public static void captureCustomSnapshot(OptimizerConfig config) {
        OptimizerConfig.PresetSnapshot snapshot = config.presets.customSnapshot;
        snapshot.rendering = config.rendering.copy();
        snapshot.playerMovement = config.playerMovement.copy();
        snapshot.blockPlacement = config.blockPlacement.copy();
        snapshot.combatVisuals = config.combatVisuals.copy();
        snapshot.interactions = config.interactions.copy();
        snapshot.compatibility = config.compatibility.copy();
        snapshot.debug = config.debug.copy();
    }

    public static void restoreCustomSnapshot(OptimizerConfig config) {
        OptimizerConfig.PresetSnapshot snapshot = config.presets.customSnapshot;
        config.rendering = snapshot.rendering.copy();
        config.playerMovement = snapshot.playerMovement.copy();
        config.blockPlacement = snapshot.blockPlacement.copy();
        config.combatVisuals = snapshot.combatVisuals.copy();
        config.interactions = snapshot.interactions.copy();
        config.compatibility = snapshot.compatibility.copy();
        config.debug = snapshot.debug.copy();
    }

    private static void applyPreset(OptimizerConfig config, OptimizerPreset preset) {
        switch (preset) {
            case CRYSTAL -> applyCrystal(config);
            case NETHERITE -> applyNetherite(config);
            case MACE -> applyMace(config);
            case SWORD -> applySword(config);
            case UHC -> applyUhc(config);
            case CUSTOM -> restoreCustomSnapshot(config);
        }
    }

    private static void applyCrystal(OptimizerConfig config) {
        // Crystal PvP favors very fast block/place/attack feedback and low visual clutter.
        config.rendering.enabled = true;
        config.rendering.suppressInWallOverlay = true;
        config.rendering.insideBlockOverlayAlpha = 0.03f;
        config.rendering.multiPointSampling = true;
        config.rendering.smoothAlphaTransition = true;
        config.rendering.alphaTransitionSpeed = 0.22f;
        config.rendering.suppressWaterOverlay = true;
        config.rendering.suppressLavaOverlay = true;
        config.rendering.reduceOverlayWhenSodiumLoaded = true;

        config.playerMovement.enabled = true;
        config.playerMovement.useEmaSmoothing = true;
        config.playerMovement.emaSmoothingAlpha = 0.42f;
        config.playerMovement.distanceAdaptiveSmoothing = true;
        config.playerMovement.nearDistanceThreshold = 6.0f;
        config.playerMovement.farDistanceThreshold = 28.0f;
        config.playerMovement.farSmoothingMultiplier = 2.4f;
        config.playerMovement.jitterFilter = true;
        config.playerMovement.jitterDeadZone = 0.35f;
        config.playerMovement.velocityExtrapolation = true;
        config.playerMovement.extrapolationStrength = 0.23f;

        config.blockPlacement.enabled = true;
        config.blockPlacement.predictionTimeoutMs = 130;
        config.blockPlacement.placementDebounceMs = 32;
        config.blockPlacement.maxPendingPredictions = 96;
        config.blockPlacement.adaptiveTimeout = true;
        config.blockPlacement.adaptiveTimeoutMultiplier = 2.1f;
        config.blockPlacement.velocityTracking = true;
        config.blockPlacement.clearPredictionsWhenWorldChanges = true;

        config.combatVisuals.enabled = true;
        config.combatVisuals.attackCooldownVisualMultiplier = 1.05f;
        config.combatVisuals.showAttackPulse = true;
        config.combatVisuals.attackPulseTicks = 4;
        config.combatVisuals.vignetteGradientPulse = true;
        config.combatVisuals.pulseColorRed = 180;
        config.combatVisuals.pulseColorGreen = 50;
        config.combatVisuals.pulseColorBlue = 255;
        config.combatVisuals.showCooldownBar = true;
        config.combatVisuals.comboTracker = true;
        config.combatVisuals.comboTimeoutMs = 1200;

        config.interactions.enabled = true;
        config.interactions.immediateLeftClickAnimation = true;
        config.interactions.immediateRightClickAnimation = true;
        config.interactions.inputBuffering = true;
        config.interactions.maxBufferedInputs = 3;
        config.interactions.holdClickAcceleration = true;
        config.interactions.holdAccelerationDelayMs = 90;
        config.interactions.trackInputLatency = true;
        config.interactions.smartDoubleClickDetection = true;
        config.interactions.doubleClickWindowMs = 190;
    }

    private static void applyNetherite(OptimizerConfig config) {
        // Netherite PvP has heavier trades and shield/axe timing; keep visuals readable.
        config.rendering.enabled = true;
        config.rendering.suppressInWallOverlay = true;
        config.rendering.insideBlockOverlayAlpha = 0.05f;
        config.rendering.multiPointSampling = true;
        config.rendering.smoothAlphaTransition = true;
        config.rendering.alphaTransitionSpeed = 0.16f;
        config.rendering.suppressWaterOverlay = false;
        config.rendering.suppressLavaOverlay = true;
        config.rendering.reduceOverlayWhenSodiumLoaded = true;

        config.playerMovement.enabled = true;
        config.playerMovement.useEmaSmoothing = true;
        config.playerMovement.emaSmoothingAlpha = 0.28f;
        config.playerMovement.distanceAdaptiveSmoothing = true;
        config.playerMovement.nearDistanceThreshold = 8.0f;
        config.playerMovement.farDistanceThreshold = 34.0f;
        config.playerMovement.farSmoothingMultiplier = 2.0f;
        config.playerMovement.jitterFilter = true;
        config.playerMovement.jitterDeadZone = 0.5f;
        config.playerMovement.velocityExtrapolation = true;
        config.playerMovement.extrapolationStrength = 0.15f;

        config.blockPlacement.enabled = true;
        config.blockPlacement.predictionTimeoutMs = 180;
        config.blockPlacement.placementDebounceMs = 58;
        config.blockPlacement.maxPendingPredictions = 72;
        config.blockPlacement.adaptiveTimeout = true;
        config.blockPlacement.adaptiveTimeoutMultiplier = 1.8f;
        config.blockPlacement.velocityTracking = true;
        config.blockPlacement.clearPredictionsWhenWorldChanges = true;

        config.combatVisuals.enabled = true;
        config.combatVisuals.attackCooldownVisualMultiplier = 1.08f;
        config.combatVisuals.showAttackPulse = true;
        config.combatVisuals.attackPulseTicks = 5;
        config.combatVisuals.vignetteGradientPulse = true;
        config.combatVisuals.pulseColorRed = 130;
        config.combatVisuals.pulseColorGreen = 70;
        config.combatVisuals.pulseColorBlue = 200;
        config.combatVisuals.showCooldownBar = true;
        config.combatVisuals.comboTracker = true;
        config.combatVisuals.comboTimeoutMs = 1600;

        config.interactions.enabled = true;
        config.interactions.immediateLeftClickAnimation = true;
        config.interactions.immediateRightClickAnimation = false;
        config.interactions.inputBuffering = true;
        config.interactions.maxBufferedInputs = 2;
        config.interactions.holdClickAcceleration = true;
        config.interactions.holdAccelerationDelayMs = 140;
        config.interactions.trackInputLatency = true;
        config.interactions.smartDoubleClickDetection = true;
        config.interactions.doubleClickWindowMs = 220;
    }

    private static void applyMace(OptimizerConfig config) {
        // Mace PvP is vertical burst damage; prioritize responsive orientation updates.
        config.rendering.enabled = true;
        config.rendering.suppressInWallOverlay = true;
        config.rendering.insideBlockOverlayAlpha = 0.04f;
        config.rendering.multiPointSampling = true;
        config.rendering.smoothAlphaTransition = true;
        config.rendering.alphaTransitionSpeed = 0.2f;
        config.rendering.suppressWaterOverlay = false;
        config.rendering.suppressLavaOverlay = true;
        config.rendering.reduceOverlayWhenSodiumLoaded = true;

        config.playerMovement.enabled = true;
        config.playerMovement.useEmaSmoothing = true;
        config.playerMovement.emaSmoothingAlpha = 0.5f;
        config.playerMovement.distanceAdaptiveSmoothing = true;
        config.playerMovement.nearDistanceThreshold = 5.0f;
        config.playerMovement.farDistanceThreshold = 26.0f;
        config.playerMovement.farSmoothingMultiplier = 2.2f;
        config.playerMovement.jitterFilter = true;
        config.playerMovement.jitterDeadZone = 0.3f;
        config.playerMovement.velocityExtrapolation = true;
        config.playerMovement.extrapolationStrength = 0.3f;

        config.blockPlacement.enabled = true;
        config.blockPlacement.predictionTimeoutMs = 160;
        config.blockPlacement.placementDebounceMs = 46;
        config.blockPlacement.maxPendingPredictions = 80;
        config.blockPlacement.adaptiveTimeout = true;
        config.blockPlacement.adaptiveTimeoutMultiplier = 1.9f;
        config.blockPlacement.velocityTracking = true;
        config.blockPlacement.clearPredictionsWhenWorldChanges = true;

        config.combatVisuals.enabled = true;
        config.combatVisuals.attackCooldownVisualMultiplier = 1.0f;
        config.combatVisuals.showAttackPulse = true;
        config.combatVisuals.attackPulseTicks = 6;
        config.combatVisuals.vignetteGradientPulse = true;
        config.combatVisuals.pulseColorRed = 255;
        config.combatVisuals.pulseColorGreen = 140;
        config.combatVisuals.pulseColorBlue = 60;
        config.combatVisuals.showCooldownBar = true;
        config.combatVisuals.comboTracker = true;
        config.combatVisuals.comboTimeoutMs = 900;

        config.interactions.enabled = true;
        config.interactions.immediateLeftClickAnimation = true;
        config.interactions.immediateRightClickAnimation = true;
        config.interactions.inputBuffering = true;
        config.interactions.maxBufferedInputs = 3;
        config.interactions.holdClickAcceleration = true;
        config.interactions.holdAccelerationDelayMs = 110;
        config.interactions.trackInputLatency = true;
        config.interactions.smartDoubleClickDetection = true;
        config.interactions.doubleClickWindowMs = 180;
    }

    private static void applySword(OptimizerConfig config) {
        // Sword PvP values cadence and close-range tracking.
        config.rendering.enabled = true;
        config.rendering.suppressInWallOverlay = true;
        config.rendering.insideBlockOverlayAlpha = 0.05f;
        config.rendering.multiPointSampling = true;
        config.rendering.smoothAlphaTransition = true;
        config.rendering.alphaTransitionSpeed = 0.18f;
        config.rendering.suppressWaterOverlay = false;
        config.rendering.suppressLavaOverlay = false;
        config.rendering.reduceOverlayWhenSodiumLoaded = true;

        config.playerMovement.enabled = true;
        config.playerMovement.useEmaSmoothing = true;
        config.playerMovement.emaSmoothingAlpha = 0.37f;
        config.playerMovement.distanceAdaptiveSmoothing = true;
        config.playerMovement.nearDistanceThreshold = 7.0f;
        config.playerMovement.farDistanceThreshold = 30.0f;
        config.playerMovement.farSmoothingMultiplier = 1.8f;
        config.playerMovement.jitterFilter = true;
        config.playerMovement.jitterDeadZone = 0.4f;
        config.playerMovement.velocityExtrapolation = true;
        config.playerMovement.extrapolationStrength = 0.2f;

        config.blockPlacement.enabled = true;
        config.blockPlacement.predictionTimeoutMs = 150;
        config.blockPlacement.placementDebounceMs = 42;
        config.blockPlacement.maxPendingPredictions = 64;
        config.blockPlacement.adaptiveTimeout = true;
        config.blockPlacement.adaptiveTimeoutMultiplier = 1.7f;
        config.blockPlacement.velocityTracking = true;
        config.blockPlacement.clearPredictionsWhenWorldChanges = true;

        config.combatVisuals.enabled = true;
        config.combatVisuals.attackCooldownVisualMultiplier = 1.12f;
        config.combatVisuals.showAttackPulse = true;
        config.combatVisuals.attackPulseTicks = 4;
        config.combatVisuals.vignetteGradientPulse = true;
        config.combatVisuals.pulseColorRed = 120;
        config.combatVisuals.pulseColorGreen = 50;
        config.combatVisuals.pulseColorBlue = 200;
        config.combatVisuals.showCooldownBar = true;
        config.combatVisuals.comboTracker = true;
        config.combatVisuals.comboTimeoutMs = 1000;

        config.interactions.enabled = true;
        config.interactions.immediateLeftClickAnimation = true;
        config.interactions.immediateRightClickAnimation = false;
        config.interactions.inputBuffering = true;
        config.interactions.maxBufferedInputs = 2;
        config.interactions.holdClickAcceleration = true;
        config.interactions.holdAccelerationDelayMs = 120;
        config.interactions.trackInputLatency = true;
        config.interactions.smartDoubleClickDetection = true;
        config.interactions.doubleClickWindowMs = 200;
    }

    private static void applyUhc(OptimizerConfig config) {
        // UHC needs clarity and predictable timing under limited healing.
        config.rendering.enabled = true;
        config.rendering.suppressInWallOverlay = true;
        config.rendering.insideBlockOverlayAlpha = 0.06f;
        config.rendering.multiPointSampling = true;
        config.rendering.smoothAlphaTransition = true;
        config.rendering.alphaTransitionSpeed = 0.14f;
        config.rendering.suppressWaterOverlay = false;
        config.rendering.suppressLavaOverlay = true;
        config.rendering.reduceOverlayWhenSodiumLoaded = true;

        config.playerMovement.enabled = true;
        config.playerMovement.useEmaSmoothing = true;
        config.playerMovement.emaSmoothingAlpha = 0.3f;
        config.playerMovement.distanceAdaptiveSmoothing = true;
        config.playerMovement.nearDistanceThreshold = 8.0f;
        config.playerMovement.farDistanceThreshold = 36.0f;
        config.playerMovement.farSmoothingMultiplier = 1.7f;
        config.playerMovement.jitterFilter = true;
        config.playerMovement.jitterDeadZone = 0.45f;
        config.playerMovement.velocityExtrapolation = true;
        config.playerMovement.extrapolationStrength = 0.16f;

        config.blockPlacement.enabled = true;
        config.blockPlacement.predictionTimeoutMs = 170;
        config.blockPlacement.placementDebounceMs = 54;
        config.blockPlacement.maxPendingPredictions = 64;
        config.blockPlacement.adaptiveTimeout = true;
        config.blockPlacement.adaptiveTimeoutMultiplier = 1.6f;
        config.blockPlacement.velocityTracking = true;
        config.blockPlacement.clearPredictionsWhenWorldChanges = true;

        config.combatVisuals.enabled = true;
        config.combatVisuals.attackCooldownVisualMultiplier = 1.0f;
        config.combatVisuals.showAttackPulse = false;
        config.combatVisuals.attackPulseTicks = 4;
        config.combatVisuals.vignetteGradientPulse = true;
        config.combatVisuals.pulseColorRed = 150;
        config.combatVisuals.pulseColorGreen = 90;
        config.combatVisuals.pulseColorBlue = 200;
        config.combatVisuals.showCooldownBar = true;
        config.combatVisuals.comboTracker = true;
        config.combatVisuals.comboTimeoutMs = 1400;

        config.interactions.enabled = true;
        config.interactions.immediateLeftClickAnimation = true;
        config.interactions.immediateRightClickAnimation = false;
        config.interactions.inputBuffering = false;
        config.interactions.maxBufferedInputs = 1;
        config.interactions.holdClickAcceleration = false;
        config.interactions.holdAccelerationDelayMs = 150;
        config.interactions.trackInputLatency = true;
        config.interactions.smartDoubleClickDetection = true;
        config.interactions.doubleClickWindowMs = 210;
    }

    public static String formatPresetLabel(OptimizerPreset preset, boolean active) {
        String base = preset.getDisplayName();
        return active ? "[ACTIVE] " + base : base;
    }

    public static float clamp01(float value) {
        return MathHelper.clamp(value, 0.0f, 1.0f);
    }
}
