package com.bawnorton.neruina.platform;


/*? if fabric {*/
import com.bawnorton.neruina.Neruina;
import net.fabricmc.api.ModInitializer;

public class NeruinaWrapper implements ModInitializer {
    @Override
    public void onInitialize() {
        Neruina.init();
    }
}
/*? } elif forge {*//*
import com.bawnorton.neruina.Neruina;
import net.minecraftforge.fml.common.Mod;

@Mod(Neruina.MOD_ID)
public class NeruinaWrapper {
    public NeruinaWrapper() {
        Neruina.init();
    }
}
*//*? } elif neoforge {*//*
import com.bawnorton.neruina.Neruina;
import net.neoforged.fml.common.Mod;

@Mod(Neruina.MOD_ID)
public class NeruinaWrapper {
    public NeruinaWrapper() {
        Neruina.init();
    }
}
*//*? }*/
