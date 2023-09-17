package com.bawnorton.neruina;

import com.bawnorton.neruina.annotation.ConditionalMixin;
import com.bawnorton.neruina.annotation.VersionedMixin;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            for(AnnotationNode node: annotationNodes) {
                if(node.desc.equals(Type.getDescriptor(ConditionalMixin.class))) {
                    String modid = Annotations.getValue(node, "modid");
                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
                    if(isModLoaded(modid)) {
                        Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is" + (applyIfPresent ? " " : " not ") + "being applied because " + modid + " is loaded");
                        return applyIfPresent;
                    } else {
                        Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is" + (!applyIfPresent ? " " : " not ") + "being applied because " + modid + " is not loaded");
                        return !applyIfPresent;
                    }
                }
                if(node.desc.equals(Type.getDescriptor(VersionedMixin.class))) {
                    String versionString = Annotations.getValue(node, "value");
                    Pattern pattern = Pattern.compile(".+?(?=[0-9])");
                    Matcher matcher = pattern.matcher(versionString);
                    if(!matcher.find()) throw new RuntimeException("Invalid version string: " + versionString);

                    String comparisonString = matcher.group(0);
                    VersionComparison comparison = VersionComparison.fromString(comparisonString);
                    String mcVersion = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
                    String[] versionParts = mcVersion.split("\\.");
                    String[] versionStringParts = versionString.substring(comparisonString.length()).split("\\.");

                    while (versionParts.length != versionStringParts.length) {
                        if(versionParts.length < versionStringParts.length) {
                            String[] newVersionParts = new String[versionParts.length + 1];
                            System.arraycopy(versionParts, 0, newVersionParts, 0, versionParts.length);
                            newVersionParts[versionParts.length] = "0";
                            versionParts = newVersionParts;
                        } else {
                            String[] newVersionStringParts = new String[versionStringParts.length + 1];
                            System.arraycopy(versionStringParts, 0, newVersionStringParts, 0, versionStringParts.length);
                            newVersionStringParts[versionStringParts.length] = "0";
                            versionStringParts = newVersionStringParts;
                        }
                    }

                    for(int i = 0; i < versionParts.length; i++) {
                        int versionPart = Integer.parseInt(versionParts[i]);
                        int versionStringPart = Integer.parseInt(versionStringParts[i]);
                        if(versionPart == versionStringPart) continue;
                        switch (comparison) {
                            case EQUALS -> {
                                Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is not being applied because " + mcVersion + " is " + versionString);
                                return false;
                            }
                            case GREATER_THAN -> {
                                boolean result = versionPart > versionStringPart;
                                Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is" + (result ? " " : " not ") + "being applied because " + mcVersion + " is" + (result ? " " : " not ") + versionString);
                                return result;
                            }
                            case LESS_THAN -> {
                                boolean result = versionPart < versionStringPart;
                                Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is" + (result ? " " : " not ") + "being applied because " + mcVersion + " is" + (result ? " " : " not ") + versionString);
                                return result;
                            }
                            case GREATER_THAN_OR_EQUAL_TO -> {
                                boolean result = versionPart >= versionStringPart;
                                Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is" + (result ? " " : " not ") + "being applied because " + mcVersion + " is" + (result ? " " : " not ") + versionString);
                                return result;
                            }
                            case LESS_THAN_OR_EQUAL_TO -> {
                                boolean result = versionPart <= versionStringPart;
                                Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is" + (result ? " " : " not ") + "being applied because " + mcVersion + " is" + (result ? " " : " not ") + versionString);
                                return result;
                            }
                        }
                    }
                    Neruina.LOGGER.info("NeruinaMixinPlugin: " + className + " is being applied because " + mcVersion + " is " + versionString);
                    return true;
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return true;
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

    private enum VersionComparison {
        EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        LESS_THAN_OR_EQUAL_TO;

        public static VersionComparison fromString(String group) {
            return switch (group) {
                case "=" -> EQUALS;
                case ">" -> GREATER_THAN;
                case "<" -> LESS_THAN;
                case ">=" -> GREATER_THAN_OR_EQUAL_TO;
                case "<=" -> LESS_THAN_OR_EQUAL_TO;
                default -> throw new RuntimeException("Invalid version comparison: " + group);
            };
        }
    }
}
