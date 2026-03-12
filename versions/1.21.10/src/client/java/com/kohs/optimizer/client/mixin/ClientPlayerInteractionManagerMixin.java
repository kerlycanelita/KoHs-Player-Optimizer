package com.kohs.optimizer.client.mixin;

import com.kohs.optimizer.client.KohsPlayerOptimizerClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.network.ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void kohs$trackConfirmedEntityAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        KohsPlayerOptimizerClient.onEntityHit(target);
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void kohs$debouncePlacementInteract(
        ClientPlayerEntity player,
        Hand hand,
        BlockHitResult hitResult,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!KohsPlayerOptimizerClient.shouldAllowBlockInteract(player, hand, hitResult)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
