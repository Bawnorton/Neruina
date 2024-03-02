package com.bawnorton.neruina;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.annotation.DevOnlyMixin;
import com.bawnorton.neruina.annotation.ModLoaderMixin;
import com.bawnorton.neruina.annotation.Version;
import com.bawnorton.neruina.platform.ModLoader;
import com.bawnorton.neruina.platform.Platform;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class NeruinaMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("NeruinaMixinPlugin");

    private static boolean anyModsLoaded(List<String> modids) {
        for (String modid : modids) {
            if (Platform.isModLoaded(modid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetName, String className) {
        try {
            List<AnnotationNode> annotationNodes = MixinService.getService()
                    .getBytecodeProvider()
                    .getClassNode(className).visibleAnnotations;
            if (annotationNodes == null) return true;

            boolean shouldApply = true;
            for (AnnotationNode node : annotationNodes) {
                if (node.desc.equals(Type.getDescriptor(ConditionalMixin.class))) {
                    List<String> modids = Annotations.getValue(node, "modids");
                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
                    if (anyModsLoaded(modids)) {
                        LOGGER.debug("%s is %sbeing applied because %s are loaded".formatted(className,
                                applyIfPresent ? "" : "not ",
                                modids
                        ));
                        shouldApply = applyIfPresent;
                    } else {
                        LOGGER.debug("%s is %sbeing applied because %s are not loaded".formatted(className,
                                !applyIfPresent ? "" : "not ",
                                modids
                        ));
                        shouldApply = !applyIfPresent;
                    }
                } else if (node.desc.equals(Type.getDescriptor(ModLoaderMixin.class))) {
                    List<ModLoader> modLoaders = Annotations.getValue(node, "value", true, ModLoader.class);
                    shouldApply = modLoaders.contains(Platform.getModLoader());
                    AnnotationNode versionNode = Annotations.getValue(node, "version", Version.class);
                    if (versionNode != null) {
                        String currentVersion = Platform.getVersion();
                        ComparableVersion comparableVersion = new ComparableVersion(currentVersion);
                        String min = Annotations.getValue(versionNode, "min", "");
                        String max = Annotations.getValue(versionNode, "max", "");
                        if (!min.isBlank()) {
                            shouldApply &= comparableVersion.compareTo(new ComparableVersion(min)) >= 0;
                        }
                        if (!max.isBlank()) {
                            shouldApply &= comparableVersion.compareTo(new ComparableVersion(max)) <= 0;
                        }
                        LOGGER.debug("%s is %sbeing applied because we are using %s %s".formatted(className,
                                shouldApply ? "" : "not ",
                                Platform.getModLoader(),
                                currentVersion
                        ));
                    } else {
                        LOGGER.debug("%s is %sbeing applied because we are using %s".formatted(className,
                                shouldApply ? "" : "not ",
                                Platform.getModLoader()
                        ));
                    }
                } else if (node.desc.equals(Type.getDescriptor(DevOnlyMixin.class))) {
                    shouldApply = Platform.isDev();
                    LOGGER.debug("%s is %sbeing applied because we are in a dev environment".formatted(className,
                            shouldApply ? "" : "not "
                    ));
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
}
