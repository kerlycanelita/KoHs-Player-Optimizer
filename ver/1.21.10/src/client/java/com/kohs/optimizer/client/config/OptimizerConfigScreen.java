package com.kohs.optimizer.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class OptimizerConfigScreen {
    private OptimizerConfigScreen() {
    }

    public static Screen create(Screen parent) {
        OptimizerConfig workingCopy = OptimizerConfigManager.getConfig().copy();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("KoHs Player Optimizer").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
            .setSavingRunnable(() -> {
                OptimizerConfigManager.setConfig(workingCopy);
                OptimizerConfigManager.save();
            });

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory rendering = builder.getOrCreateCategory(title("Rendering Optimizations"));
        rendering.addEntry(entries.startBooleanToggle(label("Enable Rendering Optimizations"), workingCopy.rendering.enabled)
            .setDefaultValue(true)
            .setTooltip(tooltip("Master toggle for block-interior rendering optimizations."))
            .setSaveConsumer(value -> workingCopy.rendering.enabled = value)
            .build());
        rendering.addEntry(entries.startBooleanToggle(label("Cull In-Wall Overlay"), workingCopy.rendering.suppressInWallOverlay)
            .setDefaultValue(true)
            .setTooltip(tooltip("Hides the in-wall overlay when your camera is inside opaque blocks."))
            .setSaveConsumer(value -> workingCopy.rendering.suppressInWallOverlay = value)
            .build());
        rendering.addEntry(entries.startFloatField(label("Inside Block Overlay Alpha"), workingCopy.rendering.insideBlockOverlayAlpha)
            .setDefaultValue(0.05f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(tooltip("Reserved alpha value for fallback transparent in-wall overlays."))
            .setSaveConsumer(value -> workingCopy.rendering.insideBlockOverlayAlpha = value)
            .build());
        rendering.addEntry(entries.startBooleanToggle(label("Sodium Safe Overlay Mode"), workingCopy.rendering.reduceOverlayWhenSodiumLoaded)
            .setDefaultValue(true)
            .setTooltip(tooltip("Automatically disables this tweak if Sodium-safe mode is enabled."))
            .setSaveConsumer(value -> workingCopy.rendering.reduceOverlayWhenSodiumLoaded = value)
            .build());

        ConfigCategory movement = builder.getOrCreateCategory(title("Player Movement"));
        movement.addEntry(entries.startBooleanToggle(label("Enable Player Movement Smoothing"), workingCopy.playerMovement.enabled)
            .setDefaultValue(true)
            .setTooltip(tooltip("Improves remote player rotation consistency with lightweight interpolation."))
            .setSaveConsumer(value -> workingCopy.playerMovement.enabled = value)
            .build());
        movement.addEntry(entries.startFloatField(label("Body Yaw Smoothing"), workingCopy.playerMovement.bodyYawSmoothing)
            .setDefaultValue(0.35f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(tooltip("Higher values react faster; lower values look smoother."))
            .setSaveConsumer(value -> workingCopy.playerMovement.bodyYawSmoothing = value)
            .build());
        movement.addEntry(entries.startFloatField(label("Head Yaw Smoothing"), workingCopy.playerMovement.headYawSmoothing)
            .setDefaultValue(0.45f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(tooltip("Controls the interpolation strength for remote head rotation."))
            .setSaveConsumer(value -> workingCopy.playerMovement.headYawSmoothing = value)
            .build());
        movement.addEntry(entries.startFloatField(label("Pitch Smoothing"), workingCopy.playerMovement.pitchSmoothing)
            .setDefaultValue(0.35f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(tooltip("Smooths pitch transitions to reduce visual jitter in close combat."))
            .setSaveConsumer(value -> workingCopy.playerMovement.pitchSmoothing = value)
            .build());

        ConfigCategory placement = builder.getOrCreateCategory(title("Block Placement"));
        placement.addEntry(entries.startBooleanToggle(label("Enable Placement Prediction"), workingCopy.blockPlacement.enabled)
            .setDefaultValue(true)
            .setTooltip(tooltip("Tracks pending placements client-side to improve placement responsiveness."))
            .setSaveConsumer(value -> workingCopy.blockPlacement.enabled = value)
            .build());
        placement.addEntry(entries.startIntField(label("Prediction Timeout (ms)"), workingCopy.blockPlacement.predictionTimeoutMs)
            .setDefaultValue(180)
            .setMin(50)
            .setMax(1000)
            .setTooltip(tooltip("How long predicted placements are kept before being considered stale."))
            .setSaveConsumer(value -> workingCopy.blockPlacement.predictionTimeoutMs = value)
            .build());
        placement.addEntry(entries.startIntField(label("Placement Debounce (ms)"), workingCopy.blockPlacement.placementDebounceMs)
            .setDefaultValue(70)
            .setMin(20)
            .setMax(500)
            .setTooltip(tooltip("Blocks duplicate placement spam against the same target in a short window."))
            .setSaveConsumer(value -> workingCopy.blockPlacement.placementDebounceMs = value)
            .build());
        placement.addEntry(entries.startBooleanToggle(label("Clear Predictions On World Change"), workingCopy.blockPlacement.clearPredictionsWhenWorldChanges)
            .setDefaultValue(true)
            .setTooltip(tooltip("Flushes pending placement states after world transitions."))
            .setSaveConsumer(value -> workingCopy.blockPlacement.clearPredictionsWhenWorldChanges = value)
            .build());

        ConfigCategory combat = builder.getOrCreateCategory(title("Combat Visuals"));
        combat.addEntry(entries.startBooleanToggle(label("Enable Combat Visual Optimization"), workingCopy.combatVisuals.enabled)
            .setDefaultValue(true)
            .setTooltip(tooltip("Master toggle for attack cooldown and hit feedback visual improvements."))
            .setSaveConsumer(value -> workingCopy.combatVisuals.enabled = value)
            .build());
        combat.addEntry(entries.startFloatField(label("Attack Cooldown Visual Multiplier"), workingCopy.combatVisuals.attackCooldownVisualMultiplier)
            .setDefaultValue(1.1f)
            .setMin(1.0f)
            .setMax(1.5f)
            .setTooltip(tooltip("Only affects client-side cooldown visuals; server combat mechanics are unchanged."))
            .setSaveConsumer(value -> workingCopy.combatVisuals.attackCooldownVisualMultiplier = value)
            .build());
        combat.addEntry(entries.startBooleanToggle(label("Show Attack Pulse"), workingCopy.combatVisuals.showAttackPulse)
            .setDefaultValue(true)
            .setTooltip(tooltip("Adds a subtle purple pulse at screen edges on attack input."))
            .setSaveConsumer(value -> workingCopy.combatVisuals.showAttackPulse = value)
            .build());
        combat.addEntry(entries.startIntSlider(label("Attack Pulse Duration"), workingCopy.combatVisuals.attackPulseTicks, 2, 12)
            .setDefaultValue(5)
            .setTooltip(tooltip("Duration of the attack pulse in client ticks."))
            .setSaveConsumer(value -> workingCopy.combatVisuals.attackPulseTicks = value)
            .build());

        ConfigCategory interaction = builder.getOrCreateCategory(title("Interaction Optimizations"));
        interaction.addEntry(entries.startBooleanToggle(label("Enable Interaction Optimizations"), workingCopy.interactions.enabled)
            .setDefaultValue(true)
            .setTooltip(tooltip("Master toggle for input feedback optimizations."))
            .setSaveConsumer(value -> workingCopy.interactions.enabled = value)
            .build());
        interaction.addEntry(entries.startBooleanToggle(label("Immediate Left Click Animation"), workingCopy.interactions.immediateLeftClickAnimation)
            .setDefaultValue(true)
            .setTooltip(tooltip("Shows immediate left-click arm swing for snappier visual feedback."))
            .setSaveConsumer(value -> workingCopy.interactions.immediateLeftClickAnimation = value)
            .build());
        interaction.addEntry(entries.startBooleanToggle(label("Immediate Right Click Animation"), workingCopy.interactions.immediateRightClickAnimation)
            .setDefaultValue(false)
            .setTooltip(tooltip("Shows immediate right-click arm swing while preserving legit networking behavior."))
            .setSaveConsumer(value -> workingCopy.interactions.immediateRightClickAnimation = value)
            .build());

        ConfigCategory compatibility = builder.getOrCreateCategory(title("Compatibility Settings"));
        compatibility.addEntry(entries.startBooleanToggle(label("Auto Disable Conflicting Hooks"), workingCopy.compatibility.autoDisableConflictingHooks)
            .setDefaultValue(true)
            .setTooltip(tooltip("Automatically applies safe-mode guards when known optimization mods are loaded."))
            .setSaveConsumer(value -> workingCopy.compatibility.autoDisableConflictingHooks = value)
            .build());
        compatibility.addEntry(entries.startBooleanToggle(label("Disable Rendering Hooks With Sodium"), workingCopy.compatibility.disableRenderingHooksWithSodium)
            .setDefaultValue(false)
            .setTooltip(tooltip("Avoids rendering-level hooks when Sodium is present."))
            .setSaveConsumer(value -> workingCopy.compatibility.disableRenderingHooksWithSodium = value)
            .build());
        compatibility.addEntry(entries.startBooleanToggle(label("Disable Movement Hooks With Lithium"), workingCopy.compatibility.disableMovementHooksWithLithium)
            .setDefaultValue(false)
            .setTooltip(tooltip("Disables movement interpolation hooks if Lithium compatibility issues are observed."))
            .setSaveConsumer(value -> workingCopy.compatibility.disableMovementHooksWithLithium = value)
            .build());
        compatibility.addEntry(entries.startBooleanToggle(label("Disable UI Hooks With Reese's"), workingCopy.compatibility.disableUiHooksWithReeses)
            .setDefaultValue(false)
            .setTooltip(tooltip("Disables optional UI effects while Reese's Sodium Options is loaded."))
            .setSaveConsumer(value -> workingCopy.compatibility.disableUiHooksWithReeses = value)
            .build());

        ConfigCategory debug = builder.getOrCreateCategory(title("Debug / Advanced Options"));
        debug.addEntry(entries.startBooleanToggle(label("Debug Overlay"), workingCopy.debug.debugOverlay)
            .setDefaultValue(false)
            .setTooltip(tooltip("Shows runtime feature status and pending prediction statistics on HUD."))
            .setSaveConsumer(value -> workingCopy.debug.debugOverlay = value)
            .build());
        debug.addEntry(entries.startBooleanToggle(label("Log Feature Timings"), workingCopy.debug.logFeatureTimings)
            .setDefaultValue(false)
            .setTooltip(tooltip("Enables periodic timing logs for feature update loops."))
            .setSaveConsumer(value -> workingCopy.debug.logFeatureTimings = value)
            .build());
        debug.addEntry(entries.startBooleanToggle(label("Strict Legit Mode"), workingCopy.debug.strictLegitMode)
            .setDefaultValue(true)
            .setTooltip(tooltip("Keeps all optimizations strictly client-side and multiplayer-safe."))
            .setSaveConsumer(value -> workingCopy.debug.strictLegitMode = value)
            .build());

        return builder.build();
    }

    private static Text title(String text) {
        return Text.literal(text).formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
    }

    private static Text label(String text) {
        return Text.literal(text).formatted(Formatting.LIGHT_PURPLE);
    }

    private static Text[] tooltip(String text) {
        return new Text[] {Text.literal(text).formatted(Formatting.GRAY)};
    }
}
