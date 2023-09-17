package com.bawnorton.neruina;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.annotation.VersionedMixin;
import com.bawnorton.neruina.version.VersionString;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class NeruinaMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetName, String className) {
        return testClass(className);
    }

    public static boolean testClass(String className) {
        try {
            List<AnnotationNode> annotationNodes = MixinService.getService().getBytecodeProvider().getClassNode(className).visibleAnnotations;
            if(annotationNodes == null) return true;

            boolean shouldApply = true;
            for(AnnotationNode node: annotationNodes) {
                if(node.desc.equals(Type.getDescriptor(ConditionalMixin.class))) {
                    String modid = Annotations.getValue(node, "modid");
                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
                    if(isModLoaded(modid)) {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is" + (applyIfPresent ? " " : " not ") + "being applied because " + modid + " is loaded");
                        shouldApply = applyIfPresent;
                    } else {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is" + (!applyIfPresent ? " " : " not ") + "being applied because " + modid + " is not loaded");
                        shouldApply = !applyIfPresent;
                    }
                }
                if(!shouldApply) return false;

                if(node.desc.equals(Type.getDescriptor(VersionedMixin.class))) {
                    String versionString = Annotations.getValue(node, "value");
                    VersionString version = new VersionString(versionString);
                    String mcVersion = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
                    if(version.isVersionValid(mcVersion)) {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is being applied because " + mcVersion + " is " + versionString);
                    } else {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is not being applied because " + mcVersion + " is not " + versionString);
                        shouldApply = false;
                    }
                }
            }
            return shouldApply;
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }
}
