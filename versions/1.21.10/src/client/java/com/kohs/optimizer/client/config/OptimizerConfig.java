package com.kohs.optimizer.client.config;

public final class OptimizerConfig {
    public RenderingSettings rendering = new RenderingSettings();
    public PlayerMovementSettings playerMovement = new PlayerMovementSettings();
    public BlockPlacementSettings blockPlacement = new BlockPlacementSettings();
    public CombatVisualSettings combatVisuals = new CombatVisualSettings();
    public InteractionSettings interactions = new InteractionSettings();
    public CompatibilitySettings compatibility = new CompatibilitySettings();
    public DebugSettings debug = new DebugSettings();

    public OptimizerConfig copy() {
        OptimizerConfig config = new OptimizerConfig();
        config.rendering = this.rendering.copy();
        config.playerMovement = this.playerMovement.copy();
        config.blockPlacement = this.blockPlacement.copy();
        config.combatVisuals = this.combatVisuals.copy();
        config.interactions = this.interactions.copy();
        config.compatibility = this.compatibility.copy();
        config.debug = this.debug.copy();
        return config;
    }

    public static final class RenderingSettings {
        public boolean enabled = true;
        public boolean suppressInWallOverlay = true;
        public float insideBlockOverlayAlpha = 0.05f;
        public boolean reduceOverlayWhenSodiumLoaded = true;

        private RenderingSettings copy() {
            RenderingSettings settings = new RenderingSettings();
            settings.enabled = this.enabled;
            settings.suppressInWallOverlay = this.suppressInWallOverlay;
            settings.insideBlockOverlayAlpha = this.insideBlockOverlayAlpha;
            settings.reduceOverlayWhenSodiumLoaded = this.reduceOverlayWhenSodiumLoaded;
            return settings;
        }
    }

    public static final class PlayerMovementSettings {
        public boolean enabled = true;
        public float bodyYawSmoothing = 0.35f;
        public float headYawSmoothing = 0.45f;
        public float pitchSmoothing = 0.35f;

        private PlayerMovementSettings copy() {
            PlayerMovementSettings settings = new PlayerMovementSettings();
            settings.enabled = this.enabled;
            settings.bodyYawSmoothing = this.bodyYawSmoothing;
            settings.headYawSmoothing = this.headYawSmoothing;
            settings.pitchSmoothing = this.pitchSmoothing;
            return settings;
        }
    }

    public static final class BlockPlacementSettings {
        public boolean enabled = true;
        public int predictionTimeoutMs = 180;
        public int placementDebounceMs = 70;
        public boolean clearPredictionsWhenWorldChanges = true;

        private BlockPlacementSettings copy() {
            BlockPlacementSettings settings = new BlockPlacementSettings();
            settings.enabled = this.enabled;
            settings.predictionTimeoutMs = this.predictionTimeoutMs;
            settings.placementDebounceMs = this.placementDebounceMs;
            settings.clearPredictionsWhenWorldChanges = this.clearPredictionsWhenWorldChanges;
            return settings;
        }
    }

    public static final class CombatVisualSettings {
        public boolean enabled = true;
        public float attackCooldownVisualMultiplier = 1.1f;
        public boolean showAttackPulse = true;
        public int attackPulseTicks = 5;

        private CombatVisualSettings copy() {
            CombatVisualSettings settings = new CombatVisualSettings();
            settings.enabled = this.enabled;
            settings.attackCooldownVisualMultiplier = this.attackCooldownVisualMultiplier;
            settings.showAttackPulse = this.showAttackPulse;
            settings.attackPulseTicks = this.attackPulseTicks;
            return settings;
        }
    }

    public static final class InteractionSettings {
        public boolean enabled = true;
        public boolean immediateLeftClickAnimation = true;
        public boolean immediateRightClickAnimation = false;

        private InteractionSettings copy() {
            InteractionSettings settings = new InteractionSettings();
            settings.enabled = this.enabled;
            settings.immediateLeftClickAnimation = this.immediateLeftClickAnimation;
            settings.immediateRightClickAnimation = this.immediateRightClickAnimation;
            return settings;
        }
    }

    public static final class CompatibilitySettings {
        public boolean autoDisableConflictingHooks = true;
        public boolean disableRenderingHooksWithSodium = false;
        public boolean disableMovementHooksWithLithium = false;
        public boolean disableUiHooksWithReeses = false;

        private CompatibilitySettings copy() {
            CompatibilitySettings settings = new CompatibilitySettings();
            settings.autoDisableConflictingHooks = this.autoDisableConflictingHooks;
            settings.disableRenderingHooksWithSodium = this.disableRenderingHooksWithSodium;
            settings.disableMovementHooksWithLithium = this.disableMovementHooksWithLithium;
            settings.disableUiHooksWithReeses = this.disableUiHooksWithReeses;
            return settings;
        }
    }

    public static final class DebugSettings {
        public boolean debugOverlay = false;
        public boolean logFeatureTimings = false;
        public boolean strictLegitMode = true;

        private DebugSettings copy() {
            DebugSettings settings = new DebugSettings();
            settings.debugOverlay = this.debugOverlay;
            settings.logFeatureTimings = this.logFeatureTimings;
            settings.strictLegitMode = this.strictLegitMode;
            return settings;
        }
    }
}
