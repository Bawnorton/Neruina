package com.bawnorton.neruina.handler.client;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.version.Texter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

//? if >1.20.1 {
import net.minecraft.client.gui.screens.GenericMessageScreen;
//?}

public final class ClientTickHandler {
	public static void handleTickingClient(Player player, Throwable e) {
		if (player instanceof LocalPlayer clientPlayer) {
			Neruina.LOGGER.warn("Neruina caught an exception, see below for cause", e);
			clientPlayer.connection.getConnection().disconnect(Component.translatable("neruina.toast.desc"));
			Minecraft client = Minecraft.getInstance();
			//? if >1.20.1 {
			client.disconnect(new GenericMessageScreen(Texter.translatable("menu.savingLevel")), false);
			//?} else {
			/*client.getConnection().onDisconnect(Texter.translatable("menu.savingLevel"));
			*///?}
			//$ if >26.1 'setScreenAndShow' else 'setScreen'
			setScreenAndShow
			//? if <=1.21.1 {
			/*client.getToasts()
			 *///?} else {
			//$ if >26.1 'gui.toastManager' else 'getToastManager'
			gui.toastManager
			//?}
			//$ if >26.1 'new SystemToast' else 'SystemToast.multiline'
					new SystemToast
							//? if <26.2 {
							/*client,
							*///?}
							//? if >1.20.1 {
							SystemToast.SystemToastId.WORLD_ACCESS_FAILURE,
							//?} else {
							/*SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE,
							*///?}
							Texter.translatable("neruina.toast.title"),
							Texter.translatable("neruina.toast.desc")
					));
		}
	}
}