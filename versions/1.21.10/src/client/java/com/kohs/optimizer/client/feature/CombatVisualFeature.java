package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public final class CombatVisualFeature implements OptimizationFeature {
    private boolean previousAttackPressed;
    private int attackPulseTicks;
    private int maxPulseTicks;
    private int comboCount;
    private long lastHitMs;
    private float displayedCooldown;
    private int registeredHits;

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        if (!config.combatVisuals.enabled || client.player == null || client.currentScreen != null) {
            previousAttackPressed = client.options.attackKey.isPressed();
            attackPulseTicks = 0;
            return;
        }

        boolean attackPressed = client.options.attackKey.isPressed();
        if (attackPressed && !previousAttackPressed) {
            maxPulseTicks = Math.max(1, config.combatVisuals.attackPulseTicks);
            attackPulseTicks = maxPulseTicks;
        }
        previousAttackPressed = attackPressed;

        if (attackPulseTicks > 0) {
            attackPulseTicks--;
        }

        if (config.combatVisuals.showCooldownBar) {
            float actual = client.player.getAttackCooldownProgress(0.0f);
            float smoothSpeed = 0.15f;
            if (actual > displayedCooldown) {
                displayedCooldown = Math.min(displayedCooldown + smoothSpeed, actual);
            } else {
                displayedCooldown = actual;
            }
        }

        if (config.combatVisuals.comboTracker && lastHitMs > 0) {
            if (System.currentTimeMillis() - lastHitMs > config.combatVisuals.comboTimeoutMs) {
                comboCount = 0;
                lastHitMs = 0;
            }
        }
    }

    @Override
    public void onHudRender(
        DrawContext drawContext,
        RenderTickCounter tickCounter,
        MinecraftClient client,
        OptimizerConfig config
    ) {
        if (!config.combatVisuals.enabled) {
            return;
        }

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        if (config.combatVisuals.showAttackPulse && attackPulseTicks > 0) {
            float progress = MathHelper.clamp((float) attackPulseTicks / (float) maxPulseTicks, 0.0f, 1.0f);
            float eased = easeOutCubic(progress);

            int r = MathHelper.clamp(config.combatVisuals.pulseColorRed, 0, 255);
            int g = MathHelper.clamp(config.combatVisuals.pulseColorGreen, 0, 255);
            int b = MathHelper.clamp(config.combatVisuals.pulseColorBlue, 0, 255);

            if (config.combatVisuals.vignetteGradientPulse) {
                drawVignetteGradient(drawContext, screenW, screenH, eased, r, g, b);
            } else {
                int alpha = MathHelper.clamp((int) (95.0f * eased), 0, 255);
                int color = (alpha << 24) | (r << 16) | (g << 8) | b;
                drawContext.fill(0, 0, screenW, 2, color);
                drawContext.fill(0, screenH - 2, screenW, screenH, color);
            }
        }

        if (config.combatVisuals.showCooldownBar && client.player != null) {
            drawCooldownBar(drawContext, screenW, screenH, config);
        }

        if (config.combatVisuals.comboTracker && comboCount > 1 && client.textRenderer != null) {
            drawComboCounter(drawContext, screenW, screenH, client);
        }
    }

    public void onEntityHit(OptimizerConfig config, Entity target) {
        if (!config.combatVisuals.enabled || !config.combatVisuals.comboTracker) {
            return;
        }
        if (!(target instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }

        long now = System.currentTimeMillis();
        int comboTimeout = Math.max(250, config.combatVisuals.comboTimeoutMs);
        if (lastHitMs > 0 && now - lastHitMs <= comboTimeout) {
            comboCount++;
        } else {
            comboCount = 1;
        }
        lastHitMs = now;
        registeredHits++;
    }

    private void drawVignetteGradient(DrawContext ctx, int w, int h, float intensity, int r, int g, int b) {
        int layers = 6;
        int edgeSize = Math.min(w, h) / 8;

        for (int i = 0; i < layers; i++) {
            float layerProgress = (float) i / layers;
            int alpha = MathHelper.clamp((int) (80.0f * intensity * (1.0f - layerProgress)), 0, 255);
            if (alpha <= 0) continue;
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            int offset = (int) (edgeSize * layerProgress);

            ctx.fill(offset, offset, w - offset, offset + 1, color);
            ctx.fill(offset, h - offset - 1, w - offset, h - offset, color);
            ctx.fill(offset, offset, offset + 1, h - offset, color);
            ctx.fill(w - offset - 1, offset, w - offset, h - offset, color);
        }
    }

    private void drawCooldownBar(DrawContext ctx, int w, int h, OptimizerConfig config) {
        int barWidth = 40;
        int barHeight = 2;
        int x = (w - barWidth) / 2;
        int y = h / 2 + 12;

        ctx.fill(x, y, x + barWidth, y + barHeight, 0x40FFFFFF);

        int fillWidth = (int) (barWidth * MathHelper.clamp(displayedCooldown, 0.0f, 1.0f));
        int fillColor;
        if (displayedCooldown >= 1.0f) {
            fillColor = 0xCC00FF88;
        } else {
            int r = MathHelper.clamp(config.combatVisuals.pulseColorRed, 0, 255);
            int g = MathHelper.clamp(config.combatVisuals.pulseColorGreen, 0, 255);
            int b = MathHelper.clamp(config.combatVisuals.pulseColorBlue, 0, 255);
            fillColor = (0xBB << 24) | (r << 16) | (g << 8) | b;
        }
        if (fillWidth > 0) {
            ctx.fill(x, y, x + fillWidth, y + barHeight, fillColor);
        }
    }

    private void drawComboCounter(DrawContext ctx, int w, int h, MinecraftClient client) {
        String comboText = comboCount + "x COMBO";
        float comboAge = (System.currentTimeMillis() - lastHitMs) / 1000.0f;
        int alpha = MathHelper.clamp((int) (255 * (1.0f - comboAge * 0.8f)), 40, 255);
        int color = (alpha << 24) | 0xDAB3FF;

        int textWidth = client.textRenderer.getWidth(comboText);
        int x = (w - textWidth) / 2;
        int y = h / 2 + 20;
        ctx.drawTextWithShadow(client.textRenderer, comboText, x, y, color);
    }

    public float attackCooldownVisualMultiplier(OptimizerConfig config) {
        if (!config.combatVisuals.enabled || config.debug.strictLegitMode) {
            return 1.0f;
        }
        return MathHelper.clamp(config.combatVisuals.attackCooldownVisualMultiplier, 1.0f, 1.5f);
    }

    public int getComboCount() {
        return comboCount;
    }

    public float getDisplayedCooldown() {
        return displayedCooldown;
    }

    public int getRegisteredHits() {
        return registeredHits;
    }

    private static float easeOutCubic(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }
}
