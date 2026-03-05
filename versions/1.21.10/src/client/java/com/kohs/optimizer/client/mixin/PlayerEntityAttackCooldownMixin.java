package com.kohs.optimizer.client.mixin;

import com.kohs.optimizer.client.KohsPlayerOptimizerClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttackCooldownMixin {
    @Inject(method = "getAttackCooldownProgress", at = @At("RETURN"), cancellable = true)
    private void kohs$accelerateClientCooldownVisuals(float baseTime, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientPlayerEntity)) {
            return;
        }

        float multiplier = KohsPlayerOptimizerClient.attackCooldownVisualMultiplier();
        if (multiplier <= 1.0f) {
            return;
        }

        float adjusted = MathHelper.clamp(cir.getReturnValue() * multiplier, 0.0f, 1.0f);
        cir.setReturnValue(adjusted);
    }
}
