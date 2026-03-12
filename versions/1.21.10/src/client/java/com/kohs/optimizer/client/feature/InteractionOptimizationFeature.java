package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import com.kohs.optimizer.client.mixin.MinecraftClientInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayDeque;
import java.util.Deque;

public final class InteractionOptimizationFeature implements OptimizationFeature {
    private boolean previousAttackPressed;
    private boolean previousUsePressed;
    private long lastLeftClickAtMs;
    private long lastRightClickAtMs;
    private long lastAttackPressAtMs;
    private long lastUsePressAtMs;

    private final Deque<BufferedInput> attackBuffer = new ArrayDeque<>();
    private final Deque<BufferedInput> useBuffer = new ArrayDeque<>();

    private long attackHoldStart;
    private long useHoldStart;
    private boolean attackHolding;
    private boolean useHolding;

    private float averageInputLatencyMs;
    private int latencySamples;
    private float latencyAccumulatorMs;

    private int replayedAttackInputs;
    private int replayedUseInputs;
    private int droppedBufferedInputs;
    private int doubleClickCount;

    @Override
    public void onClientTick(MinecraftClient client, OptimizerConfig config) {
        boolean attackPressed = client.options.attackKey.isPressed();
        boolean usePressed = client.options.useKey.isPressed();

        if (!config.interactions.enabled || client.player == null || client.currentScreen != null) {
            previousAttackPressed = attackPressed;
            previousUsePressed = usePressed;
            attackHolding = false;
            useHolding = false;
            attackBuffer.clear();
            useBuffer.clear();
            return;
        }

        long now = System.currentTimeMillis();
        MinecraftClientInvoker invoker = (MinecraftClientInvoker) client;

        if (attackPressed && !previousAttackPressed) {
            lastLeftClickAtMs = now;

            if (config.interactions.smartDoubleClickDetection) {
                int window = MathHelper.clamp(config.interactions.doubleClickWindowMs, 80, 500);
                if (lastAttackPressAtMs > 0 && now - lastAttackPressAtMs <= window) {
                    doubleClickCount++;
                }
                lastAttackPressAtMs = now;
            }

            if (config.interactions.immediateLeftClickAnimation) {
                client.player.swingHand(Hand.MAIN_HAND);
            }

            attackHoldStart = now;
            attackHolding = true;

            if (config.interactions.inputBuffering && !config.debug.strictLegitMode) {
                if (client.player.getAttackCooldownProgress(0.0f) < 0.92f) {
                    bufferInput(attackBuffer, now, config.interactions.maxBufferedInputs);
                }
            }
        } else if (!attackPressed && attackHolding) {
            attackHolding = false;
        }

        if (usePressed && !previousUsePressed) {
            lastRightClickAtMs = now;

            if (config.interactions.smartDoubleClickDetection) {
                int window = MathHelper.clamp(config.interactions.doubleClickWindowMs, 80, 500);
                if (lastUsePressAtMs > 0 && now - lastUsePressAtMs <= window) {
                    doubleClickCount++;
                }
                lastUsePressAtMs = now;
            }

            if (config.interactions.immediateRightClickAnimation) {
                client.player.swingHand(Hand.MAIN_HAND);
            }

            useHoldStart = now;
            useHolding = true;

            if (config.interactions.inputBuffering && !config.debug.strictLegitMode) {
                if (invoker.kohs$getItemUseCooldown() > 0) {
                    bufferInput(useBuffer, now, config.interactions.maxBufferedInputs);
                }
            }
        } else if (!usePressed && useHolding) {
            useHolding = false;
        }

        if (config.interactions.holdClickAcceleration) {
            if (attackHolding && attackPressed && now - attackHoldStart > config.interactions.holdAccelerationDelayMs) {
                if (client.player.handSwingTicks <= 0) {
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }
            if (useHolding && usePressed && now - useHoldStart > config.interactions.holdAccelerationDelayMs) {
                if (client.player.handSwingTicks <= 0) {
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }

        if (config.interactions.inputBuffering && !config.debug.strictLegitMode) {
            replayBufferedInputs(client, invoker, attackBuffer, true, now);
            replayBufferedInputs(client, invoker, useBuffer, false, now);
        }

        previousAttackPressed = attackPressed;
        previousUsePressed = usePressed;

        if (config.interactions.trackInputLatency) {
            updateLatencyAverage(0.0f);
        }
    }

    private void bufferInput(Deque<BufferedInput> buffer, long timestampMs, int maxSize) {
        int boundedSize = MathHelper.clamp(maxSize, 1, 8);
        while (buffer.size() >= boundedSize) {
            buffer.pollFirst();
            droppedBufferedInputs++;
        }
        buffer.addLast(new BufferedInput(timestampMs));
    }

    private void replayBufferedInputs(
        MinecraftClient client,
        MinecraftClientInvoker invoker,
        Deque<BufferedInput> buffer,
        boolean isAttack,
        long nowMs
    ) {
        if (buffer.isEmpty() || client.player == null) {
            return;
        }

        while (!buffer.isEmpty() && nowMs - buffer.peekFirst().timestampMs > 500L) {
            buffer.pollFirst();
            droppedBufferedInputs++;
        }

        while (!buffer.isEmpty()) {
            BufferedInput buffered = buffer.peekFirst();
            boolean ready;
            boolean executed = false;

            if (isAttack) {
                ready = client.player.getAttackCooldownProgress(0.0f) >= 0.95f;
                if (!ready) {
                    break;
                }
                executed = invoker.kohs$invokeDoAttack();
                if (executed) {
                    replayedAttackInputs++;
                }
            } else {
                ready = invoker.kohs$getItemUseCooldown() <= 0;
                if (!ready) {
                    break;
                }
                invoker.kohs$invokeDoItemUse();
                executed = true;
                replayedUseInputs++;
            }

            if (!executed) {
                break;
            }

            buffer.pollFirst();
            updateLatencyAverage(nowMs - buffered.timestampMs);
        }
    }

    private void updateLatencyAverage(float sampleMs) {
        latencyAccumulatorMs += sampleMs;
        latencySamples++;
        if (latencySamples >= 20) {
            averageInputLatencyMs = latencyAccumulatorMs / latencySamples;
            latencyAccumulatorMs = 0.0f;
            latencySamples = 0;
        }
    }

    public long getLastLeftClickAtMs() {
        return lastLeftClickAtMs;
    }

    public long getLastRightClickAtMs() {
        return lastRightClickAtMs;
    }

    public float getAverageInputLatencyMs() {
        return averageInputLatencyMs;
    }

    public int getAttackBufferSize() {
        return attackBuffer.size();
    }

    public int getUseBufferSize() {
        return useBuffer.size();
    }

    public int getReplayedAttackInputs() {
        return replayedAttackInputs;
    }

    public int getReplayedUseInputs() {
        return replayedUseInputs;
    }

    public int getDroppedBufferedInputs() {
        return droppedBufferedInputs;
    }

    public int getDoubleClickCount() {
        return doubleClickCount;
    }

    private record BufferedInput(long timestampMs) {
    }
}
