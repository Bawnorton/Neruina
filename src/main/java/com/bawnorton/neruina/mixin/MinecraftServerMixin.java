package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.report.Storage;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"
            )
    )
    private void onServerStart(CallbackInfo ci) {
        MinecraftServer self = (MinecraftServer) (Object) this;
        Neruina.getInstance().getAutoReportHandler().init(self);
        Neruina.getInstance().getBlacklistHandler().init(self);
        Neruina.getInstance().getTickHandler().init();
        Neruina.getInstance().updateServerState(self);
        Storage.init(self);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void onServerStop(CallbackInfo ci) {
        Neruina.getInstance().updateServerState((MinecraftServer) (Object) this);
    }
}
