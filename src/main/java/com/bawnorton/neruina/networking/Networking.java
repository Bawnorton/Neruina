package com.bawnorton.neruina.networking;

import com.bawnorton.neruina.Neruina;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class Networking {
    public static final Identifier BAD_BLOCK = new Identifier(Neruina.MOD_ID, "bad_block_entity");

    public static void sendBadBlockPacket(ServerPlayerEntity player, BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ServerPlayNetworking.send(player, BAD_BLOCK, buf);
    }
}
