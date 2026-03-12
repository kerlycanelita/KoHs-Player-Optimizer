package com.kohs.optimizer.client.config;

public final class OptimizerConfig {
    public PresetSettings presets = new PresetSettings();
    public RenderingSettings rendering = new RenderingSettings();
    public PlayerMovementSettings playerMovement = new PlayerMovementSettings();
    public BlockPlacementSettings blockPlacement = new BlockPlacementSettings();
    public CombatVisualSettings combatVisuals = new CombatVisualSettings();
    public InteractionSettings interactions = new InteractionSettings();
    public CompatibilitySettings compatibility = new CompatibilitySettings();
    public DebugSettings debug = new DebugSettings();

    public void normalize() {
        if (presets == null) presets = new PresetSettings();
        if (rendering == null) rendering = new RenderingSettings();
        if (playerMovement == null) playerMovement = new PlayerMovementSettings();
        if (blockPlacement == null) blockPlacement = new BlockPlacementSettings();
        if (combatVisuals == null) combatVisuals = new CombatVisualSettings();
        if (interactions == null) interactions = new InteractionSettings();
        if (compatibility == null) compatibility = new CompatibilitySettings();
        if (debug == null) debug = new DebugSettings();

        presets.normalize();
        presets.customSnapshot.normalize();
    }

    public OptimizerConfig copy() {
        OptimizerConfig config = new OptimizerConfig();
        config.presets = this.presets.copy();
        config.rendering = this.rendering.copy();
        config.playerMovement = this.playerMovement.copy();
        config.blockPlacement = this.blockPlacement.copy();
        config.combatVisuals = this.combatVisuals.copy();
        config.interactions = this.interactions.copy();
        config.compatibility = this.compatibility.copy();
        config.debug = this.debug.copy();
        return config;
    }

    public static final class PresetSettings {
        public OptimizerPreset activePreset = OptimizerPreset.CUSTOM;
        public boolean customSnapshotInitialized = false;
        public PresetSnapshot customSnapshot = new PresetSnapshot();

        PresetSettings copy() {
            PresetSettings s = new PresetSettings();
            s.activePreset = this.activePreset;
            s.customSnapshotInitialized = this.customSnapshotInitialized;
            s.customSnapshot = this.customSnapshot.copy();
            return s;
        }

        private void normalize() {
            if (activePreset == null) activePreset = OptimizerPreset.CUSTOM;
            if (customSnapshot == null) customSnapshot = new PresetSnapshot();
        }
    }

    public static final class PresetSnapshot {
        public RenderingSettings rendering = new RenderingSettings();
        public PlayerMovementSettings playerMovement = new PlayerMovementSettings();
        public BlockPlacementSettings blockPlacement = new BlockPlacementSettings();
        public CombatVisualSettings combatVisuals = new CombatVisualSettings();
        public InteractionSettings interactions = new InteractionSettings();
        public CompatibilitySettings compatibility = new CompatibilitySettings();
        public DebugSettings debug = new DebugSettings();

        PresetSnapshot copy() {
            PresetSnapshot s = new PresetSnapshot();
            s.rendering = this.rendering.copy();
            s.playerMovement = this.playerMovement.copy();
            s.blockPlacement = this.blockPlacement.copy();
            s.combatVisuals = this.combatVisuals.copy();
            s.interactions = this.interactions.copy();
            s.compatibility = this.compatibility.copy();
            s.debug = this.debug.copy();
            return s;
        }

        private void normalize() {
            if (rendering == null) rendering = new RenderingSettings();
            if (playerMovement == null) playerMovement = new PlayerMovementSettings();
            if (blockPlacement == null) blockPlacement = new BlockPlacementSettings();
            if (combatVisuals == null) combatVisuals = new CombatVisualSettings();
            if (interactions == null) interactions = new InteractionSettings();
            if (compatibility == null) compatibility = new CompatibilitySettings();
            if (debug == null) debug = new DebugSettings();
        }
    }

    public static final class RenderingSettings {
        public boolean enabled = true;
        public boolean suppressInWallOverlay = true;
        public float insideBlockOverlayAlpha = 0.05f;
        public boolean reduceOverlayWhenSodiumLoaded = true;
        public boolean multiPointSampling = true;
        public boolean smoothAlphaTransition = true;
        public float alphaTransitionSpeed = 0.12f;
        public boolean suppressWaterOverlay = false;
        public boolean suppressLavaOverlay = false;

        RenderingSettings copy() {
            RenderingSettings s = new RenderingSettings();
            s.enabled = this.enabled;
            s.suppressInWallOverlay = this.suppressInWallOverlay;
            s.insideBlockOverlayAlpha = this.insideBlockOverlayAlpha;
            s.reduceOverlayWhenSodiumLoaded = this.reduceOverlayWhenSodiumLoaded;
            s.multiPointSampling = this.multiPointSampling;
            s.smoothAlphaTransition = this.smoothAlphaTransition;
            s.alphaTransitionSpeed = this.alphaTransitionSpeed;
            s.suppressWaterOverlay = this.suppressWaterOverlay;
            s.suppressLavaOverlay = this.suppressLavaOverlay;
            return s;
        }
    }

    public static final class PlayerMovementSettings {
        public boolean enabled = true;
        public float bodyYawSmoothing = 0.35f;
        public float headYawSmoothing = 0.45f;
        public float pitchSmoothing = 0.35f;
        public boolean useEmaSmoothing = true;
        public float emaSmoothingAlpha = 0.25f;
        public boolean distanceAdaptiveSmoothing = true;
        public float nearDistanceThreshold = 8.0f;
        public float farDistanceThreshold = 32.0f;
        public float farSmoothingMultiplier = 2.0f;
        public boolean jitterFilter = true;
        public float jitterDeadZone = 0.5f;
        public boolean velocityExtrapolation = true;
        public float extrapolationStrength = 0.15f;

        PlayerMovementSettings copy() {
            PlayerMovementSettings s = new PlayerMovementSettings();
            s.enabled = this.enabled;
            s.bodyYawSmoothing = this.bodyYawSmoothing;
            s.headYawSmoothing = this.headYawSmoothing;
            s.pitchSmoothing = this.pitchSmoothing;
            s.useEmaSmoothing = this.useEmaSmoothing;
            s.emaSmoothingAlpha = this.emaSmoothingAlpha;
            s.distanceAdaptiveSmoothing = this.distanceAdaptiveSmoothing;
            s.nearDistanceThreshold = this.nearDistanceThreshold;
            s.farDistanceThreshold = this.farDistanceThreshold;
            s.farSmoothingMultiplier = this.farSmoothingMultiplier;
            s.jitterFilter = this.jitterFilter;
            s.jitterDeadZone = this.jitterDeadZone;
            s.velocityExtrapolation = this.velocityExtrapolation;
            s.extrapolationStrength = this.extrapolationStrength;
            return s;
        }
    }

    public static final class BlockPlacementSettings {
        public boolean enabled = true;
        public int predictionTimeoutMs = 180;
        public int placementDebounceMs = 70;
        public boolean clearPredictionsWhenWorldChanges = true;
        public int maxPendingPredictions = 64;
        public boolean adaptiveTimeout = true;
        public float adaptiveTimeoutMultiplier = 1.5f;
        public boolean velocityTracking = true;

        BlockPlacementSettings copy() {
            BlockPlacementSettings s = new BlockPlacementSettings();
            s.enabled = this.enabled;
            s.predictionTimeoutMs = this.predictionTimeoutMs;
            s.placementDebounceMs = this.placementDebounceMs;
            s.clearPredictionsWhenWorldChanges = this.clearPredictionsWhenWorldChanges;
            s.maxPendingPredictions = this.maxPendingPredictions;
            s.adaptiveTimeout = this.adaptiveTimeout;
            s.adaptiveTimeoutMultiplier = this.adaptiveTimeoutMultiplier;
            s.velocityTracking = this.velocityTracking;
            return s;
        }
    }

    public static final class CombatVisualSettings {
        public boolean enabled = true;
        public float attackCooldownVisualMultiplier = 1.1f;
        public boolean showAttackPulse = true;
        public int attackPulseTicks = 5;
        public boolean vignetteGradientPulse = true;
        public int pulseColorRed = 120;
        public int pulseColorGreen = 30;
        public int pulseColorBlue = 180;
        public boolean showCooldownBar = false;
        public boolean comboTracker = true;
        public int comboTimeoutMs = 1500;

        CombatVisualSettings copy() {
            CombatVisualSettings s = new CombatVisualSettings();
            s.enabled = this.enabled;
            s.attackCooldownVisualMultiplier = this.attackCooldownVisualMultiplier;
            s.showAttackPulse = this.showAttackPulse;
            s.attackPulseTicks = this.attackPulseTicks;
            s.vignetteGradientPulse = this.vignetteGradientPulse;
            s.pulseColorRed = this.pulseColorRed;
            s.pulseColorGreen = this.pulseColorGreen;
            s.pulseColorBlue = this.pulseColorBlue;
            s.showCooldownBar = this.showCooldownBar;
            s.comboTracker = this.comboTracker;
            s.comboTimeoutMs = this.comboTimeoutMs;
            return s;
        }
    }

    public static final class InteractionSettings {
        public boolean enabled = true;
        public boolean immediateLeftClickAnimation = true;
        public boolean immediateRightClickAnimation = false;
        public boolean inputBuffering = true;
        public int maxBufferedInputs = 3;
        public boolean holdClickAcceleration = true;
        public int holdAccelerationDelayMs = 150;
        public boolean trackInputLatency = true;
        public boolean smartDoubleClickDetection = true;
        public int doubleClickWindowMs = 220;

        InteractionSettings copy() {
            InteractionSettings s = new InteractionSettings();
            s.enabled = this.enabled;
            s.immediateLeftClickAnimation = this.immediateLeftClickAnimation;
            s.immediateRightClickAnimation = this.immediateRightClickAnimation;
            s.inputBuffering = this.inputBuffering;
            s.maxBufferedInputs = this.maxBufferedInputs;
            s.holdClickAcceleration = this.holdClickAcceleration;
            s.holdAccelerationDelayMs = this.holdAccelerationDelayMs;
            s.trackInputLatency = this.trackInputLatency;
            s.smartDoubleClickDetection = this.smartDoubleClickDetection;
            s.doubleClickWindowMs = this.doubleClickWindowMs;
            return s;
        }
    }

    public static final class CompatibilitySettings {
        public boolean autoDisableConflictingHooks = true;
        public boolean disableRenderingHooksWithSodium = false;
        public boolean disableMovementHooksWithLithium = false;
        public boolean disableUiHooksWithReeses = false;

        CompatibilitySettings copy() {
            CompatibilitySettings s = new CompatibilitySettings();
            s.autoDisableConflictingHooks = this.autoDisableConflictingHooks;
            s.disableRenderingHooksWithSodium = this.disableRenderingHooksWithSodium;
            s.disableMovementHooksWithLithium = this.disableMovementHooksWithLithium;
            s.disableUiHooksWithReeses = this.disableUiHooksWithReeses;
            return s;
        }
    }

    public static final class DebugSettings {
        public boolean debugOverlay = false;
        public boolean logFeatureTimings = false;
        public boolean strictLegitMode = true;
        public boolean realtimePanelEnabled = false;
        public boolean realtimePanelWarningEnabled = true;
        public int realtimePanelX = 8;
        public int realtimePanelY = 56;
        public boolean realtimeShowRendering = true;
        public boolean realtimeShowMovement = true;
        public boolean realtimeShowPlacement = true;
        public boolean realtimeShowInteraction = true;
        public boolean realtimeShowCombat = true;
        public boolean realtimeShowCompat = true;
        public boolean realtimeShowTiming = true;

        DebugSettings copy() {
            DebugSettings s = new DebugSettings();
            s.debugOverlay = this.debugOverlay;
            s.logFeatureTimings = this.logFeatureTimings;
            s.strictLegitMode = this.strictLegitMode;
            s.realtimePanelEnabled = this.realtimePanelEnabled;
            s.realtimePanelWarningEnabled = this.realtimePanelWarningEnabled;
            s.realtimePanelX = this.realtimePanelX;
            s.realtimePanelY = this.realtimePanelY;
            s.realtimeShowRendering = this.realtimeShowRendering;
            s.realtimeShowMovement = this.realtimeShowMovement;
            s.realtimeShowPlacement = this.realtimeShowPlacement;
            s.realtimeShowInteraction = this.realtimeShowInteraction;
            s.realtimeShowCombat = this.realtimeShowCombat;
            s.realtimeShowCompat = this.realtimeShowCompat;
            s.realtimeShowTiming = this.realtimeShowTiming;
            return s;
        }
    }
}
