import neruina.utils.applyMixinDebugSettings
import neruina.utils.deps
import neruina.utils.mod

plugins {
    kotlin("jvm")
    `maven-publish`
    id("net.neoforged.moddev.legacyforge")
    id("neruina.common")
    id("me.modmuss50.mod-publish-plugin")
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table") version "0.1.0-alpha.15"
    id("dev.isxander.secrets") version "0.1.0"
}

repositories {
    mavenLocal()
    maven("https://maven.bawnorton.com/releases")
    maven("https://maven.parchmentmc.org")
    maven("https://repo.jenkins-ci.org/public/")
    maven("https://maven.minecraftforge.net")
}

val minecraft: String by project
val loader: String by project

sc.properties.tags(minecraft)

base.archivesName = "${mod("id")}-${mod("version")}+$minecraft-$loader"

dependencies {
    deps("kohsuke_github") {
        jarJar(implementation("org.kohsuke:github-api:$it") {
            exclude("commons-io", "commons-io")
            exclude("org.apache.commons", "commons-lang3")
            exclude("com.fasterxml.jackson.core", "jackson-databind")
            exclude("com.fasterxml.jackson.core", "jackson-annotations")
            exclude("com.fasterxml.jackson.core", "jackson-core")
        })
    }
    deps("http_core") {
        jarJar(implementation("org.apache.httpcomponents:httpcore:$it")!!)
    }
    deps("http_client") {
        jarJar(implementation("org.apache.httpcomponents:httpclient:$it")!!)
    }
    deps("configurable") {
        implementation(annotationProcessor("com.bawnorton.configurable:configurable-$loader:$it")!!)
    }

    implementation(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3")!!)
    jarJar(implementation("io.github.llamalad7:mixinextras-forge:0.5.3")!!)

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

legacyForge {
    version = deps("forge")

    mods {
        register(mod("id")!!) {
            sourceSet(sourceSets["main"])
        }
    }

    deps("parchment") {
        parchment {
            val (mc, version) = it.split(':')
            mappingsVersion = version
            minecraftVersion = mc
        }
    }

    runs {
        all {
            gameDirectory = rootProject.file("run")
        }

        register("client") {
            ideName = "Forge Client $minecraft"
            client()

            programArgument("--username=Bawnorton")
            programArgument("--uuid=17c06cab-bf05-4ade-a8d6-ed14aaf70545")

            systemProperty("terminal.ansi", "true")
        }
    }

    afterEvaluate {
        runs.configureEach {
            applyMixinDebugSettings(::jvmArgument, ::systemProperty)
        }
    }
}

mixin {
    add(sourceSets["main"], "${mod("id")}.refmap.json")
    config("${mod("id")}-forge.mixins.json")
}

fletchingTable {
    mixins.register("main") {
        mixin("default", "${mod("id")}-forge.mixins.json")
    }

    j52j.register("main") {
        extension("json", "*.json5")
    }
}

stonecutter {
    replacements.string(eval(current.version, ">=1.21.11")) {
        replace("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.Identifier")
    }
    replacements.string(eval(current.version, ">=1.21.11")) {
        replace("ResourceLocation", "Identifier")
    }
}

sourceSets {
    main {
        resources {
            srcDir("../1.20.1-fabric/src/main/generated")
        }
    }
}

tasks {
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(named<Jar>("reobfJar").map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }

    processResources {
        dependsOn(":1.20.1-fabric:runDatagen")
        exclude("fabric.mod.json", "**/neoforge.mods.toml", "neruina.mixins.json")
    }

    named<Jar>("sourcesJar") {
        dependsOn(":1.20.1-fabric:runDatagen")
    }

    named<Jar>("jar") {
        manifest {
            attributes(
                "Specification-Title" to mod("name")!!,
                "Specification-Vendor" to "${mod("author")}",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to mod("version")!!,
                "Implementation-Vendor" to "${mod("author")}",
                "MixinConfigs" to listOf(
                    "${mod("id")}-forge.mixins.json"
                ).joinToString(", ")
            )
        }
    }
}

val isPublishing = gradle.startParameter.taskNames.any {
    it.contains("publish", ignoreCase = true)
}

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "bawnorton"
            url = uri("https://maven.bawnorton.com/releases")

            if (isPublishing) {
                credentials {
                    username = onePassword["op://Private/Maven API Key/username"].get()
                    password = onePassword["op://Private/Maven API Key/credential"].get()
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "${mod("group")}.${mod("id")}"
            artifactId = "${mod("id")}-$loader"
            version = "${mod("version")}+$minecraft"

            from(components["java"])
        }
    }
}

publishMods {
    val mrTokenProvider = onePassword["op://Private/Modrinth API Key/credential"]
    val cfTokenProvider = onePassword["op://Private/Curseforge API Key/credential"]

    type = BETA
    file = tasks.named<Jar>("reobfJar").map { it.archiveFile.get() }
    additionalFiles.from(tasks.named<Jar>("sourcesJar").map { it.archiveFile.get() })

    displayName = "${mod("name")} Forge ${mod("version")} for $minecraft"
    version = mod("version")
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
    modLoaders.add(loader)

    val compatibleVersions = sc.properties.raw("mod", "compatible_versions").to<List<String>>()

    modrinth {
        projectId = property("publishing.modrinth") as String
        accessToken = mrTokenProvider
        minecraftVersions.addAll(compatibleVersions)
    }

    curseforge {
        projectId = property("publishing.curseforge") as String
        accessToken = cfTokenProvider
        server.set(true)
        minecraftVersions.addAll(compatibleVersions)
    }
}