package com.kohs.optimizer.client.mixin;

import com.kohs.optimizer.client.KohsPlayerOptimizerClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void kohs$conditionallyCullInWallOverlay(CallbackInfo callbackInfo) {
        if (KohsPlayerOptimizerClient.shouldSuppressInWallOverlay()) {
            callbackInfo.cancel();
        }
    }
}
