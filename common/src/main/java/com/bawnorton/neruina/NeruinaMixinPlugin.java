package com.bawnorton.neruina;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class NeruinaMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String s) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetName, String className) {
        if(className.equals("com.bawnorton.neruina.mixin.WorldMixin")) {
            return !(isModLoaded("itshallnottick")  || isModLoaded("noseenotick"));
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }
}
