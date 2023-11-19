package com.bawnorton.neruina_test.forge;

import com.bawnorton.neruina_test.NeruinaTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NeruinaTest.MOD_ID)
public class NeruinaTestForge {
    public NeruinaTestForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(NeruinaTestForge::init);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static void init(FMLCommonSetupEvent event) {
        NeruinaTest.init();
    }
}
