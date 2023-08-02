package com.bawnorton.neruina;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.annotation.MultiConditionMixin;
import dev.architectury.injectables.annotations.ExpectPlatform;
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
    public void onLoad(String s) {

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

            for(AnnotationNode node: annotationNodes) {
                if(node.desc.equals(Type.getDescriptor(ConditionalMixin.class))) {
                    String modid = Annotations.getValue(node, "modid");
                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
                    if(isModLoaded(modid)) {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is" + (applyIfPresent ? " " : " not ") + "being applied because " + modid + " is loaded");
                        return applyIfPresent;
                    } else {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is" + (!applyIfPresent ? " " : " not ") + "being applied because " + modid + " is not loaded");
                        return !applyIfPresent;
                    }
                } else if (node.desc.equals(Type.getDescriptor(MultiConditionMixin.class))) {
                    List<AnnotationNode> conditions = Annotations.getValue(node, "conditions");
                    boolean shouldApply = true;
                    Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is being tested for multiple conditions");
                    for(AnnotationNode condition: conditions) {
                        String modid = Annotations.getValue(condition, "modid");
                        boolean applyIfPresent = Annotations.getValue(condition, "applyIfPresent", Boolean.TRUE);
                        if(isModLoaded(modid)) {
                            if(!applyIfPresent) {
                                Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is not being applied because " + modid + " is loaded");
                                shouldApply = false;
                            }
                        } else {
                            if(applyIfPresent) {
                                Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is not being applied because " + modid + " is not loaded");
                                shouldApply = false;
                            }
                        }
                    }
                    if(shouldApply) {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is being applied because all conditions are met");
                        return true;
                    } else {
                        Neruina.LOGGER.debug("NeruinaMixinPlugin: " + className + " is not being applied because not all conditions are met");
                        return false;
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
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
