package com.bawnorton.neruina_test.forge;

import com.bawnorton.neruina_test.NeruinaTest;
import net.minecraftforge.fml.common.Mod;

@Mod(NeruinaTest.MOD_ID)
public class NeruinaTestForge {
    public NeruinaTestForge() {
        NeruinaTest.init();
    }
}
