package com.bawnorton.neruina.mixin.accessor;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(MutableText.class)
public interface MutableTextAccessor {
    /*? if >=1.20 {*/
    @Accessor
    @Mutable
    void setSiblings(List<Text> siblings);
    /*? } */
}
