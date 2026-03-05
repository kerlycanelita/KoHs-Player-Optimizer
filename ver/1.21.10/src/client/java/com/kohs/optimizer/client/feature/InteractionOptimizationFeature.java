package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

public final class InteractionOptimizationFeature implements OptimizationFeature {
    private boolean previousAttackPressed;
    private boolean previousUsePressed;
    private long lastLeftClickAtMs;
    private long lastRightClickAtMs;

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        boolean attackPressed = client.options.attackKey.isPressed();
        boolean usePressed = client.options.useKey.isPressed();

        if (!config.interactions.enabled || client.player == null || client.currentScreen != null) {
            previousAttackPressed = attackPressed;
            previousUsePressed = usePressed;
            return;
        }

        long now = System.currentTimeMillis();

        if (attackPressed && !previousAttackPressed) {
            lastLeftClickAtMs = now;
            if (config.interactions.immediateLeftClickAnimation) {
                client.player.swingHand(Hand.MAIN_HAND);
            }
        }

        if (usePressed && !previousUsePressed) {
            lastRightClickAtMs = now;
            if (config.interactions.immediateRightClickAnimation) {
                client.player.swingHand(Hand.MAIN_HAND);
            }
        }

        previousAttackPressed = attackPressed;
        previousUsePressed = usePressed;
    }

    public long getLastLeftClickAtMs() {
        return lastLeftClickAtMs;
    }

    public long getLastRightClickAtMs() {
        return lastRightClickAtMs;
    }
}
