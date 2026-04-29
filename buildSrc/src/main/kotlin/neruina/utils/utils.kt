package neruina.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.util.function.BiConsumer
import java.util.function.Consumer

fun Project.deps(name: String): String? = findProperty("deps.${name}") as String?
fun Project.deps(name: String, consumer: (prop: String) -> Unit) = deps(name)?.let(consumer)

fun Project.mod(name: String): String? = findProperty("mod.${name}") as String?
fun Project.mod(name: String, consumer: (prop: String) -> Unit) = mod(name)?.let(consumer)

fun Project.applyMixinDebugSettings(vmArgConsumer: Consumer<String>, propertyConsumer: BiConsumer<String, String>) {
    propertyConsumer.accept("mixin.hotSwap", "true")
    propertyConsumer.accept("mixin.debug.export", "true")

    val files = configurations.named("runtimeClasspath").get().incoming.artifactView {
        componentFilter {
            it is ModuleComponentIdentifier && it.group == "net.fabricmc" && it.module == "sponge-mixin"
        }
    }.files
    val mixinJarFile = files.firstOrNull()?.absolutePath
    if (mixinJarFile == null) {
        logger.warn("Could not find sponge-mixin jar in runtimeClasspath for mixin debug settings.")
        return
    }
    vmArgConsumer.accept("-javaagent:$mixinJarFile")
    vmArgConsumer.accept("-XX:+AllowEnhancedClassRedefinition")
}