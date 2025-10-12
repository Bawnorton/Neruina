package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.command.NeruinaCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment
@Mixin(Commands.class)
public abstract class CommandManagerMixin {
	@Shadow
	@Final
	private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void addCommands(CallbackInfo ci) {
		NeruinaCommandHandler.register(dispatcher);
	}
}
