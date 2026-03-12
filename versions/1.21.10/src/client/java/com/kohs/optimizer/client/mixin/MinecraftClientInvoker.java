package com.kohs.optimizer.client.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientInvoker {
    @Invoker("doAttack")
    boolean kohs$invokeDoAttack();

    @Invoker("doItemUse")
    void kohs$invokeDoItemUse();

    @Accessor("itemUseCooldown")
    int kohs$getItemUseCooldown();
}
