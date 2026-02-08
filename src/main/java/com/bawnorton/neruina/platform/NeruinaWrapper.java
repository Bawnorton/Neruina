package com.bawnorton.neruina.platform;

import com.bawnorton.neruina.Neruina;

//? if fabric {
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint
public class NeruinaWrapper implements ModInitializer {
	@Override
	public void onInitialize() {
		Neruina.init();
	}
}
//?} elif neoforge {
/*import net.neoforged.fml.common.Mod;

@Mod(Neruina.MOD_ID)
public class NeruinaWrapper {
    public NeruinaWrapper() {
        Neruina.init();
    }
}
*///?} elif forge {
/*import net.minecraftforge.fml.common.Mod;

@Mod(Neruina.MOD_ID)
public class NeruinaWrapper {
		public NeruinaWrapper() {
			Neruina.init();
		}
}
*///?}
