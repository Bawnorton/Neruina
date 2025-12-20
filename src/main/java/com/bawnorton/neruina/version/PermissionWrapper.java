package com.bawnorton.neruina.version;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}

public interface PermissionWrapper {
	//? if >=1.21.11 {
	static boolean hasPermission(Player player, int permission) {
		return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(permission)));
	}

	static boolean hasPermission(CommandSourceStack sourceStack, int permission) {
		return sourceStack.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(permission)));
	}
	//?} else {
	/*static boolean hasPermission(Player player, int permission) {
		return player.hasPermissions(permission);
	}

	static boolean hasPermission(CommandSourceStack sourceStack, int permission) {
		return sourceStack.hasPermission(permission);
	}
	*///?}
}
