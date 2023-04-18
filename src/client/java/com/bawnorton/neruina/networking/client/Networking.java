package com.bawnorton.neruina.networking.client;

import com.bawnorton.neruina.render.overlay.Colour;
import com.bawnorton.neruina.render.overlay.Cube;
import com.bawnorton.neruina.render.overlay.RenderManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;

public class Networking {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(com.bawnorton.neruina.networking.Networking.BAD_BLOCK_ENTITY, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> RenderManager.addRenderer(pos, new Cube(pos, Colour.fromHex(0xFF0000))));
        });
    }
}
