package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;

public final class CombatVisualFeature implements OptimizationFeature {
    private boolean previousAttackPressed;
    private int attackPulseTicks;

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        if (!config.combatVisuals.enabled || client.player == null || client.currentScreen != null) {
            previousAttackPressed = client.options.attackKey.isPressed();
            attackPulseTicks = 0;
            return;
        }

        boolean attackPressed = client.options.attackKey.isPressed();
        if (attackPressed && !previousAttackPressed) {
            attackPulseTicks = Math.max(1, config.combatVisuals.attackPulseTicks);
        }
        previousAttackPressed = attackPressed;

        if (attackPulseTicks > 0) {
            attackPulseTicks--;
        }
    }

    @Override
    public void onHudRender(
        DrawContext drawContext,
        RenderTickCounter tickCounter,
        MinecraftClient client,
        OptimizerConfig config
    ) {
        if (!config.combatVisuals.enabled || !config.combatVisuals.showAttackPulse || attackPulseTicks <= 0) {
            return;
        }

        int maxTicks = Math.max(1, config.combatVisuals.attackPulseTicks);
        float progress = MathHelper.clamp((float) attackPulseTicks / (float) maxTicks, 0.0f, 1.0f);
        int alpha = MathHelper.clamp((int) (95.0f * progress), 0, 255);
        int pulseColor = (alpha << 24) | 0x2D0850;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        drawContext.fill(0, 0, width, 2, pulseColor);
        drawContext.fill(0, height - 2, width, height, pulseColor);
    }

    public float attackCooldownVisualMultiplier(OptimizerConfig config) {
        if (!config.combatVisuals.enabled) {
            return 1.0f;
        }
        return MathHelper.clamp(config.combatVisuals.attackCooldownVisualMultiplier, 1.0f, 1.5f);
    }
}
