package com.bawnorton.neruina.forge;

import com.bawnorton.neruina.Neruina;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Neruina.MOD_ID)
public class NeruinaForge {
    public NeruinaForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(NeruinaForge::init);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static void init(FMLCommonSetupEvent event) {
        Neruina.init();
    }
}
