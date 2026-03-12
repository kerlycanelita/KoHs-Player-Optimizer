package com.kohs.optimizer.client.config;

import com.kohs.optimizer.client.compat.CompatibilityState;
import com.kohs.optimizer.client.gui.KohsButtonWidget;
import com.kohs.optimizer.client.gui.KohsGuiColors;
import com.kohs.optimizer.client.gui.KohsGuiUtils;
import com.kohs.optimizer.client.gui.KohsScrollPanel;
import com.kohs.optimizer.client.gui.KohsSliderWidget;
import com.kohs.optimizer.client.gui.KohsToggleWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class OptimizerConfigScreen extends Screen {
    private static final String[] TAB_NAMES = {
        "Presets", "Rendering", "Movement", "Placement", "Combat", "Interaction", "Compat", "Debug"
    };

    private final Screen parent;
    private final OptimizerConfig workingCopy;
    private final CompatibilityState compatibilityState;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int tabBarY;
    private int tabBarH;
    private int contentY;
    private int contentH;
    private int selectedTab;
    private float tabIndicatorX;
    private KohsScrollPanel scrollPanel;
    private boolean showRealtimePanelWarningModal;
    private boolean minimalUiHooks;

    private OptimizerConfigScreen(Screen parent) {
        super(Text.literal("KoHs Player Optimizer"));
        this.parent = parent;
        this.workingCopy = OptimizerConfigManager.getConfig().copy();
        this.workingCopy.normalize();
        PresetApplier.ensureCustomSnapshot(this.workingCopy);
        this.compatibilityState = CompatibilityState.detect();
        this.minimalUiHooks = this.workingCopy.compatibility.disableUiHooksWithReeses
            && compatibilityState.reesesSodiumOptionsLoaded();
    }

    public static Screen create(Screen parent) {
        return new OptimizerConfigScreen(parent);
    }

    @Override
    protected void init() {
        super.init();

        panelW = Math.min(540, width - 30);
        panelH = Math.min(380, height - 24);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        tabBarY = panelY + 28;
        tabBarH = 20;
        contentY = tabBarY + tabBarH + 4;
        contentH = panelH - (contentY - panelY) - 30;

        scrollPanel = new KohsScrollPanel(panelX + 6, contentY, panelW - 12, contentH);
        tabIndicatorX = panelX + 6 + selectedTab * ((float) (panelW - 12) / TAB_NAMES.length);
        populateTab(selectedTab);
    }

    private void populateTab(int tab) {
        scrollPanel.clearWidgets();
        int w = scrollPanel.getWidgetAreaWidth();

        switch (tab) {
            case 0 -> populatePresetsTab(w);
            case 1 -> populateRenderingTab(w);
            case 2 -> populateMovementTab(w);
            case 3 -> populatePlacementTab(w);
            case 4 -> populateCombatTab(w);
            case 5 -> populateInteractionTab(w);
            case 6 -> populateCompatTab(w);
            case 7 -> populateDebugTab(w);
            default -> {
            }
        }
    }

    private void populatePresetsTab(int w) {
        for (OptimizerPreset preset : OptimizerPreset.values()) {
            scrollPanel.addWidget(new KohsButtonWidget(
                0, 0, w, 18,
                () -> PresetApplier.formatPresetLabel(preset, workingCopy.presets.activePreset == preset),
                () -> {
                    PresetApplier.selectPreset(workingCopy, preset);
                    populateTab(selectedTab);
                },
                () -> workingCopy.presets.activePreset == preset
            ));
        }

        scrollPanel.addWidget(new KohsButtonWidget(
            0, 0, w, 18,
            () -> "Capture current settings into Custom snapshot",
            () -> {
                PresetApplier.captureCustomSnapshot(workingCopy);
                workingCopy.presets.customSnapshotInitialized = true;
                workingCopy.presets.activePreset = OptimizerPreset.CUSTOM;
                populateTab(selectedTab);
            }
        ));
    }

    private void populateRenderingTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Enable Rendering Optimizations", workingCopy.rendering.enabled,
            v -> workingCopy.rendering.enabled = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Cull In-Wall Overlay", workingCopy.rendering.suppressInWallOverlay,
            v -> workingCopy.rendering.suppressInWallOverlay = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Multi-Point Sampling", workingCopy.rendering.multiPointSampling,
            v -> workingCopy.rendering.multiPointSampling = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Smooth Alpha Transition", workingCopy.rendering.smoothAlphaTransition,
            v -> workingCopy.rendering.smoothAlphaTransition = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Alpha Transition Speed", 0.01f, 1.0f,
            workingCopy.rendering.alphaTransitionSpeed, 0.01f, false,
            v -> workingCopy.rendering.alphaTransitionSpeed = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Inside Block Overlay Alpha", 0.0f, 1.0f,
            workingCopy.rendering.insideBlockOverlayAlpha, 0.01f, false,
            v -> workingCopy.rendering.insideBlockOverlayAlpha = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Suppress Water Overlay", workingCopy.rendering.suppressWaterOverlay,
            v -> workingCopy.rendering.suppressWaterOverlay = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Suppress Lava Overlay", workingCopy.rendering.suppressLavaOverlay,
            v -> workingCopy.rendering.suppressLavaOverlay = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Sodium Safe Mode", workingCopy.rendering.reduceOverlayWhenSodiumLoaded,
            v -> workingCopy.rendering.reduceOverlayWhenSodiumLoaded = v));
    }

    private void populateMovementTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Enable Movement Smoothing", workingCopy.playerMovement.enabled,
            v -> workingCopy.playerMovement.enabled = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Use EMA Smoothing", workingCopy.playerMovement.useEmaSmoothing,
            v -> workingCopy.playerMovement.useEmaSmoothing = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "EMA Alpha", 0.05f, 1.0f,
            workingCopy.playerMovement.emaSmoothingAlpha, 0.01f, false,
            v -> workingCopy.playerMovement.emaSmoothingAlpha = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Body Yaw Smoothing", 0.0f, 1.0f,
            workingCopy.playerMovement.bodyYawSmoothing, 0.01f, false,
            v -> workingCopy.playerMovement.bodyYawSmoothing = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Head Yaw Smoothing", 0.0f, 1.0f,
            workingCopy.playerMovement.headYawSmoothing, 0.01f, false,
            v -> workingCopy.playerMovement.headYawSmoothing = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Pitch Smoothing", 0.0f, 1.0f,
            workingCopy.playerMovement.pitchSmoothing, 0.01f, false,
            v -> workingCopy.playerMovement.pitchSmoothing = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Distance-Adaptive Smoothing", workingCopy.playerMovement.distanceAdaptiveSmoothing,
            v -> workingCopy.playerMovement.distanceAdaptiveSmoothing = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Near Distance Threshold", 4.0f, 32.0f,
            workingCopy.playerMovement.nearDistanceThreshold, 1.0f, false,
            v -> workingCopy.playerMovement.nearDistanceThreshold = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Far Distance Threshold", 16.0f, 128.0f,
            workingCopy.playerMovement.farDistanceThreshold, 1.0f, false,
            v -> workingCopy.playerMovement.farDistanceThreshold = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Far Smoothing Multiplier", 1.0f, 5.0f,
            workingCopy.playerMovement.farSmoothingMultiplier, 0.1f, false,
            v -> workingCopy.playerMovement.farSmoothingMultiplier = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Jitter Filter", workingCopy.playerMovement.jitterFilter,
            v -> workingCopy.playerMovement.jitterFilter = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Jitter Dead Zone", 0.1f, 5.0f,
            workingCopy.playerMovement.jitterDeadZone, 0.1f, false,
            v -> workingCopy.playerMovement.jitterDeadZone = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Velocity Extrapolation", workingCopy.playerMovement.velocityExtrapolation,
            v -> workingCopy.playerMovement.velocityExtrapolation = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Extrapolation Strength", 0.0f, 0.5f,
            workingCopy.playerMovement.extrapolationStrength, 0.01f, false,
            v -> workingCopy.playerMovement.extrapolationStrength = v));
    }

    private void populatePlacementTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Enable Placement Prediction", workingCopy.blockPlacement.enabled,
            v -> workingCopy.blockPlacement.enabled = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Prediction Timeout (ms)", 50, 1000,
            workingCopy.blockPlacement.predictionTimeoutMs, 10, true,
            v -> workingCopy.blockPlacement.predictionTimeoutMs = v.intValue()));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Placement Debounce (ms)", 20, 500,
            workingCopy.blockPlacement.placementDebounceMs, 5, true,
            v -> workingCopy.blockPlacement.placementDebounceMs = v.intValue()));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Max Pending Predictions", 4, 128,
            workingCopy.blockPlacement.maxPendingPredictions, 1, true,
            v -> workingCopy.blockPlacement.maxPendingPredictions = v.intValue()));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Adaptive Timeout", workingCopy.blockPlacement.adaptiveTimeout,
            v -> workingCopy.blockPlacement.adaptiveTimeout = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Adaptive Timeout Multiplier", 1.0f, 4.0f,
            workingCopy.blockPlacement.adaptiveTimeoutMultiplier, 0.1f, false,
            v -> workingCopy.blockPlacement.adaptiveTimeoutMultiplier = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Velocity Tracking", workingCopy.blockPlacement.velocityTracking,
            v -> workingCopy.blockPlacement.velocityTracking = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Clear On World Change", workingCopy.blockPlacement.clearPredictionsWhenWorldChanges,
            v -> workingCopy.blockPlacement.clearPredictionsWhenWorldChanges = v));
    }

    private void populateCombatTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Enable Combat Visuals", workingCopy.combatVisuals.enabled,
            v -> workingCopy.combatVisuals.enabled = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Cooldown Visual Multiplier", 1.0f, 1.5f,
            workingCopy.combatVisuals.attackCooldownVisualMultiplier, 0.01f, false,
            v -> workingCopy.combatVisuals.attackCooldownVisualMultiplier = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Show Attack Pulse", workingCopy.combatVisuals.showAttackPulse,
            v -> workingCopy.combatVisuals.showAttackPulse = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Vignette Gradient Mode", workingCopy.combatVisuals.vignetteGradientPulse,
            v -> workingCopy.combatVisuals.vignetteGradientPulse = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Attack Pulse Duration", 2, 12,
            workingCopy.combatVisuals.attackPulseTicks, 1, true,
            v -> workingCopy.combatVisuals.attackPulseTicks = v.intValue()));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Pulse Color R", 0, 255,
            workingCopy.combatVisuals.pulseColorRed, 1, true,
            v -> workingCopy.combatVisuals.pulseColorRed = v.intValue()));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Pulse Color G", 0, 255,
            workingCopy.combatVisuals.pulseColorGreen, 1, true,
            v -> workingCopy.combatVisuals.pulseColorGreen = v.intValue()));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Pulse Color B", 0, 255,
            workingCopy.combatVisuals.pulseColorBlue, 1, true,
            v -> workingCopy.combatVisuals.pulseColorBlue = v.intValue()));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Show Cooldown Bar", workingCopy.combatVisuals.showCooldownBar,
            v -> workingCopy.combatVisuals.showCooldownBar = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Combo Tracker (entity hits)", workingCopy.combatVisuals.comboTracker,
            v -> workingCopy.combatVisuals.comboTracker = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Combo Timeout (ms)", 500, 5000,
            workingCopy.combatVisuals.comboTimeoutMs, 100, true,
            v -> workingCopy.combatVisuals.comboTimeoutMs = v.intValue()));
    }

    private void populateInteractionTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Enable Interaction Optimization", workingCopy.interactions.enabled,
            v -> workingCopy.interactions.enabled = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Immediate Left Click Animation", workingCopy.interactions.immediateLeftClickAnimation,
            v -> workingCopy.interactions.immediateLeftClickAnimation = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Immediate Right Click Animation", workingCopy.interactions.immediateRightClickAnimation,
            v -> workingCopy.interactions.immediateRightClickAnimation = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Input Buffering", workingCopy.interactions.inputBuffering,
            v -> workingCopy.interactions.inputBuffering = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Max Buffered Inputs", 1, 8,
            workingCopy.interactions.maxBufferedInputs, 1, true,
            v -> workingCopy.interactions.maxBufferedInputs = v.intValue()));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Hold-Click Acceleration", workingCopy.interactions.holdClickAcceleration,
            v -> workingCopy.interactions.holdClickAcceleration = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Hold Accel Delay (ms)", 50, 500,
            workingCopy.interactions.holdAccelerationDelayMs, 10, true,
            v -> workingCopy.interactions.holdAccelerationDelayMs = v.intValue()));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Track Input Latency", workingCopy.interactions.trackInputLatency,
            v -> workingCopy.interactions.trackInputLatency = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Smart Double-Click Detection", workingCopy.interactions.smartDoubleClickDetection,
            v -> workingCopy.interactions.smartDoubleClickDetection = v));
        scrollPanel.addWidget(new KohsSliderWidget(0, 0, w, "Double-Click Window (ms)", 80, 500,
            workingCopy.interactions.doubleClickWindowMs, 5, true,
            v -> workingCopy.interactions.doubleClickWindowMs = v.intValue()));
    }

    private void populateCompatTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Auto Disable Conflicting Hooks", workingCopy.compatibility.autoDisableConflictingHooks,
            v -> workingCopy.compatibility.autoDisableConflictingHooks = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Disable Rendering Hooks (Sodium)", workingCopy.compatibility.disableRenderingHooksWithSodium,
            v -> workingCopy.compatibility.disableRenderingHooksWithSodium = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Disable Movement Hooks (Lithium)", workingCopy.compatibility.disableMovementHooksWithLithium,
            v -> workingCopy.compatibility.disableMovementHooksWithLithium = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Disable UI Hooks (Reese's)", workingCopy.compatibility.disableUiHooksWithReeses,
            v -> {
                workingCopy.compatibility.disableUiHooksWithReeses = v;
                minimalUiHooks = v && compatibilityState.reesesSodiumOptionsLoaded();
            }));
    }

    private void populateDebugTab(int w) {
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Debug Overlay", workingCopy.debug.debugOverlay,
            v -> workingCopy.debug.debugOverlay = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Log Feature Timings", workingCopy.debug.logFeatureTimings,
            v -> workingCopy.debug.logFeatureTimings = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Strict Legit Mode", workingCopy.debug.strictLegitMode,
            v -> workingCopy.debug.strictLegitMode = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Enable Realtime Debug Panel", workingCopy.debug.realtimePanelEnabled,
            v -> {
                workingCopy.debug.realtimePanelEnabled = v;
                if (v && workingCopy.debug.realtimePanelWarningEnabled) {
                    showRealtimePanelWarningModal = true;
                }
            }));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Rendering Node", workingCopy.debug.realtimeShowRendering,
            v -> workingCopy.debug.realtimeShowRendering = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Movement Node", workingCopy.debug.realtimeShowMovement,
            v -> workingCopy.debug.realtimeShowMovement = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Placement Node", workingCopy.debug.realtimeShowPlacement,
            v -> workingCopy.debug.realtimeShowPlacement = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Interaction Node", workingCopy.debug.realtimeShowInteraction,
            v -> workingCopy.debug.realtimeShowInteraction = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Combat Node", workingCopy.debug.realtimeShowCombat,
            v -> workingCopy.debug.realtimeShowCombat = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Compat Node", workingCopy.debug.realtimeShowCompat,
            v -> workingCopy.debug.realtimeShowCompat = v));
        scrollPanel.addWidget(new KohsToggleWidget(0, 0, w, "Realtime: Timing Node", workingCopy.debug.realtimeShowTiming,
            v -> workingCopy.debug.realtimeShowTiming = v));
        scrollPanel.addWidget(new KohsButtonWidget(0, 0, w, 18,
            () -> "HUD Edit Position",
            () -> {
                if (client != null) {
                    client.setScreen(new DebugPanelHudEditorScreen(this, workingCopy));
                }
            }));
        scrollPanel.addWidget(new KohsButtonWidget(0, 0, w, 18,
            () -> "Reset Debug Panel Position",
            () -> {
                workingCopy.debug.realtimePanelX = 8;
                workingCopy.debug.realtimePanelY = 56;
            }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, KohsGuiColors.SCREEN_DIM);

        if (!minimalUiHooks) {
            KohsGuiUtils.drawGlowBorder(context, panelX, panelY, panelW, panelH, KohsGuiColors.ACCENT, 3);
        }
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, KohsGuiColors.PANEL_BG);
        KohsGuiUtils.drawBorder(context, panelX, panelY, panelW, panelH, KohsGuiColors.BORDER_GLOW);

        String titleStr = "KoHs Player Optimizer";
        int titleWidth = textRenderer.getWidth(titleStr);
        context.drawTextWithShadow(textRenderer, titleStr, panelX + (panelW - titleWidth) / 2, panelY + 8, KohsGuiColors.TEXT_BRIGHT);
        if (!minimalUiHooks) {
            KohsGuiUtils.drawGradientFill(context, panelX + 20, panelY + 22, panelW - 40, 1,
                KohsGuiColors.ACCENT, KohsGuiColors.ACCENT_SECONDARY);
        }

        renderTabBar(context, mouseX, mouseY);
        scrollPanel.render(context, mouseX, mouseY, delta);
        renderBottomButtons(context, mouseX, mouseY);

        if (showRealtimePanelWarningModal) {
            renderRealtimeWarningModal(context, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderTabBar(DrawContext context, int mouseX, int mouseY) {
        int tabCount = TAB_NAMES.length;
        int tabWidth = (panelW - 12) / tabCount;

        for (int i = 0; i < tabCount; i++) {
            int tx = panelX + 6 + i * tabWidth;
            int ty = tabBarY;
            boolean hovered = mouseX >= tx && mouseX < tx + tabWidth && mouseY >= ty && mouseY < ty + tabBarH;
            boolean active = i == selectedTab;

            if (active) {
                context.fill(tx, ty, tx + tabWidth, ty + tabBarH, KohsGuiColors.TAB_HOVER);
            } else if (hovered) {
                context.fill(tx, ty, tx + tabWidth, ty + tabBarH, KohsGuiColors.withAlpha(KohsGuiColors.TAB_HOVER, 0x1A));
            }

            String tabName = TAB_NAMES[i];
            int textColor = active ? KohsGuiColors.TEXT_BRIGHT : (hovered ? KohsGuiColors.TEXT_PRIMARY : KohsGuiColors.TEXT_SECONDARY);
            int textW = textRenderer.getWidth(tabName);
            context.drawTextWithShadow(textRenderer, tabName, tx + (tabWidth - textW) / 2, ty + 6, textColor);
        }

        int indicatorY = tabBarY + tabBarH - 2;
        int tabWidthForIndicator = (panelW - 12) / tabCount;
        float targetX = panelX + 6 + selectedTab * tabWidthForIndicator;
        if (minimalUiHooks) {
            tabIndicatorX = targetX;
        } else {
            tabIndicatorX += (targetX - tabIndicatorX) * 0.3f;
        }
        KohsGuiUtils.drawGradientFill(context, (int) tabIndicatorX, indicatorY, tabWidthForIndicator, 2,
            KohsGuiColors.ACCENT, KohsGuiColors.ACCENT_SECONDARY);
    }

    private void renderBottomButtons(DrawContext context, int mouseX, int mouseY) {
        int btnW = 78;
        int btnH = 16;
        int btnY = panelY + panelH - 22;

        int saveX = panelX + panelW / 2 - btnW - 8;
        boolean saveHover = mouseX >= saveX && mouseX <= saveX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        context.fill(saveX, btnY, saveX + btnW, btnY + btnH, saveHover ? KohsGuiColors.BUTTON_HOVER : KohsGuiColors.BUTTON_BG);
        KohsGuiUtils.drawBorder(context, saveX, btnY, btnW, btnH, KohsGuiColors.SUCCESS);
        context.drawTextWithShadow(textRenderer, "Save", saveX + 26, btnY + 4, KohsGuiColors.SUCCESS);

        int cancelX = panelX + panelW / 2 + 8;
        boolean cancelHover = mouseX >= cancelX && mouseX <= cancelX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        context.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, cancelHover ? KohsGuiColors.BUTTON_HOVER : KohsGuiColors.BUTTON_BG);
        KohsGuiUtils.drawBorder(context, cancelX, btnY, btnW, btnH, KohsGuiColors.BORDER);
        context.drawTextWithShadow(textRenderer, "Cancel", cancelX + 18, btnY + 4, KohsGuiColors.TEXT_SECONDARY);
    }

    private void renderRealtimeWarningModal(DrawContext context, int mouseX, int mouseY) {
        int w = 320;
        int h = 120;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        context.fill(x, y, x + w, y + h, 0xF010081A);
        KohsGuiUtils.drawBorder(context, x, y, w, h, KohsGuiColors.WARNING);

        context.drawTextWithShadow(textRenderer, "Realtime Debug Panel Warning", x + 12, y + 10, KohsGuiColors.TEXT_BRIGHT);
        context.drawTextWithShadow(textRenderer,
            "This panel is intended for debugging and can reduce FPS in some setups.",
            x + 12, y + 30, KohsGuiColors.TEXT_PRIMARY);
        context.drawTextWithShadow(textRenderer,
            "Use only while testing feature behavior.",
            x + 12, y + 42, KohsGuiColors.TEXT_PRIMARY);

        int okX = x + 38;
        int noWarnX = x + 174;
        int by = y + 84;
        int bw = 108;
        int bh = 16;

        boolean okHover = mouseX >= okX && mouseX <= okX + bw && mouseY >= by && mouseY <= by + bh;
        context.fill(okX, by, okX + bw, by + bh, okHover ? KohsGuiColors.BUTTON_HOVER : KohsGuiColors.BUTTON_BG);
        KohsGuiUtils.drawBorder(context, okX, by, bw, bh, KohsGuiColors.SUCCESS);
        context.drawTextWithShadow(textRenderer, "OK", okX + 45, by + 4, KohsGuiColors.SUCCESS);

        boolean noWarnHover = mouseX >= noWarnX && mouseX <= noWarnX + bw && mouseY >= by && mouseY <= by + bh;
        context.fill(noWarnX, by, noWarnX + bw, by + bh, noWarnHover ? KohsGuiColors.BUTTON_HOVER : KohsGuiColors.BUTTON_BG);
        KohsGuiUtils.drawBorder(context, noWarnX, by, bw, bh, KohsGuiColors.ACCENT);
        context.drawTextWithShadow(textRenderer, "Don't show again", noWarnX + 11, by + 4, KohsGuiColors.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean playSound) {
        double mouseX = click.x();
        double mouseY = click.y();

        if (showRealtimePanelWarningModal) {
            int w = 320;
            int h = 120;
            int x = (width - w) / 2;
            int y = (height - h) / 2;
            int by = y + 84;
            int bw = 108;
            int bh = 16;
            int okX = x + 38;
            int noWarnX = x + 174;

            if (isInside(mouseX, mouseY, okX, by, bw, bh)) {
                showRealtimePanelWarningModal = false;
                return true;
            }
            if (isInside(mouseX, mouseY, noWarnX, by, bw, bh)) {
                workingCopy.debug.realtimePanelWarningEnabled = false;
                showRealtimePanelWarningModal = false;
                return true;
            }
            return true;
        }

        int tabCount = TAB_NAMES.length;
        int tabWidth = (panelW - 12) / tabCount;
        if (mouseY >= tabBarY && mouseY < tabBarY + tabBarH) {
            for (int i = 0; i < tabCount; i++) {
                int tx = panelX + 6 + i * tabWidth;
                if (mouseX >= tx && mouseX < tx + tabWidth) {
                    if (i != selectedTab) {
                        selectedTab = i;
                        populateTab(i);
                    }
                    return true;
                }
            }
        }

        int btnW = 78;
        int btnH = 16;
        int btnY = panelY + panelH - 22;
        int saveX = panelX + panelW / 2 - btnW - 8;
        if (isInside(mouseX, mouseY, saveX, btnY, btnW, btnH)) {
            workingCopy.normalize();
            if (workingCopy.presets.activePreset == OptimizerPreset.CUSTOM) {
                PresetApplier.captureCustomSnapshot(workingCopy);
                workingCopy.presets.customSnapshotInitialized = true;
            }
            OptimizerConfigManager.setConfig(workingCopy);
            OptimizerConfigManager.save();
            close();
            return true;
        }

        int cancelX = panelX + panelW / 2 + 8;
        if (isInside(mouseX, mouseY, cancelX, btnY, btnW, btnH)) {
            close();
            return true;
        }

        if (scrollPanel.mouseClicked(click, playSound)) {
            return true;
        }
        return super.mouseClicked(click, playSound);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (showRealtimePanelWarningModal) {
            return true;
        }
        if (scrollPanel.mouseDragged(click, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (showRealtimePanelWarningModal) {
            return true;
        }
        scrollPanel.mouseReleased(click);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (showRealtimePanelWarningModal) {
            return true;
        }
        if (scrollPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
