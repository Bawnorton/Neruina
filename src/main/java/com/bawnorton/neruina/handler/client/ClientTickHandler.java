package com.bawnorton.neruina.handler.client;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.version.Texter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class ClientTickHandler {
	public static void handleTickingClient(Player player, Throwable e) {
		if (player instanceof LocalPlayer clientPlayer) {
			Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
			clientPlayer.connection.getConnection().disconnect(Component.translatable("neruina.toast.desc"));
			Minecraft client = Minecraft.getInstance();
			client.disconnect(new GenericMessageScreen(Texter.translatable("menu.savingLevel")), false);
			client.setScreen(new TitleScreen());
			//? if 1.21.1 {
			/*client.getToasts()
			 *///?} else {
			client.getToastManager()
					//?}
					.addToast(SystemToast.multiline(client,
							SystemToast.SystemToastId.WORLD_ACCESS_FAILURE,
							Texter.translatable("neruina.toast.title"),
							Texter.translatable("neruina.toast.desc")
					));
		}
	}
}