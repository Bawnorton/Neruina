package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.handler.NeruinaTickHandler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At("HEAD"))
    private void captureServerInstance(CallbackInfo ci) {
        NeruinaTickHandler.setServer((MinecraftServer) (Object) this);
    }
}
