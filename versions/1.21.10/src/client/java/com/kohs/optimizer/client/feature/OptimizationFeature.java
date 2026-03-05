package com.kohs.optimizer.client.feature;

import com.kohs.optimizer.client.config.OptimizerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;

public interface OptimizationFeature {
    default void onClientTick(MinecraftClient client, OptimizerConfig config) {
    }

    default void onWorldTick(ClientWorld world, MinecraftClient client, OptimizerConfig config) {
    }

    default void onHudRender(
        DrawContext drawContext,
        RenderTickCounter tickCounter,
        MinecraftClient client,
        OptimizerConfig config
    ) {
    }
}
