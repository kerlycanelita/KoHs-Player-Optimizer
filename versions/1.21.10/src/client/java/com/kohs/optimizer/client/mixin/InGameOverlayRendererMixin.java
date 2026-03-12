package com.kohs.optimizer.client.mixin;

import com.kohs.optimizer.client.KohsPlayerOptimizerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void kohs$conditionallyCullInWallOverlay(
        Sprite sprite,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        CallbackInfo callbackInfo
    ) {
        if (KohsPlayerOptimizerClient.shouldSuppressInWallOverlay()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(
        method = "renderInWallOverlay",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ColorHelper;fromFloats(FFFF)I")
    )
    private static int kohs$adjustInWallOverlayAlpha(float alpha, float red, float green, float blue) {
        float effectiveAlpha = KohsPlayerOptimizerClient.inWallOverlayAlpha(alpha);
        return ColorHelper.fromFloats(effectiveAlpha, red, green, blue);
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void kohs$conditionallyCullWaterOverlay(
        MinecraftClient client,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        CallbackInfo callbackInfo
    ) {
        if (KohsPlayerOptimizerClient.shouldSuppressWaterOverlay()) {
            callbackInfo.cancel();
        }
    }

    @ModifyConstant(method = "renderUnderwaterOverlay", constant = @Constant(floatValue = 0.1f))
    private static float kohs$adjustUnderwaterOverlayAlpha(float original) {
        return KohsPlayerOptimizerClient.underwaterOverlayAlpha(original);
    }

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void kohs$conditionallyCullFireOverlay(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        Sprite sprite,
        CallbackInfo callbackInfo
    ) {
        if (KohsPlayerOptimizerClient.shouldSuppressLavaOverlay()) {
            callbackInfo.cancel();
        }
    }

    @ModifyConstant(method = "renderFireOverlay", constant = @Constant(floatValue = 0.9f))
    private static float kohs$adjustFireOverlayAlpha(float original) {
        return KohsPlayerOptimizerClient.lavaOverlayAlpha(original);
    }
}
