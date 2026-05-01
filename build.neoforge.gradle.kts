import neruina.utils.applyMixinDebugSettings
import neruina.utils.deps
import neruina.utils.mod

plugins {
    kotlin("jvm")
    `maven-publish`
    id("net.neoforged.moddev")
    id("neruina.common")
    id("me.modmuss50.mod-publish-plugin")
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.15"
    id("dev.isxander.secrets") version "0.1.0"
}

repositories {
    mavenLocal()
    maven("https://maven.bawnorton.com/releases")
    maven("https://maven.parchmentmc.org")
    maven("https://repo.jenkins-ci.org/public/")
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
}

java {
    withSourcesJar()
    if (stonecutter.eval(minecraft, "<=1.21.11")) {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    } else {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

neoForge {
    version = deps("neoforge")

    deps("parchment") {
        if (stonecutter.eval(stonecutter.current.version, "<=1.21.11")) {
            parchment {
                val (mc, version) = it.split(':')
                mappingsVersion = version
                minecraftVersion = mc
            }
        }
    }

    runs {
        all {
            gameDirectory = rootProject.file("run")
        }

        register("client") {
            ideName = "NeoForge Client $minecraft"
            client()

            programArgument("--username=Bawnorton")
            programArgument("--uuid=17c06cab-bf05-4ade-a8d6-ed14aaf70545")
        }

        register("server") {
            ideName = "NeoForge Server $minecraft"
            server()
        }

        register("data") {
            ideName = "NeoForge Data Generation $minecraft"

            if (stonecutter.eval(minecraft, ">1.21.1")) {
                serverData()
            } else {
                data()
            }
            programArguments.addAll(
                "--mod", "${mod("id")}",
                "--output", project.file("src/main/generated").toString()
            )
        }

    }

    afterEvaluate {
        runs.configureEach {
            applyMixinDebugSettings(::jvmArgument, ::systemProperty)
        }
    }

    mods {
        create("${mod("id")}", Action {
            sourceSet(sourceSets.main.get())
        })
    }
}

fletchingTable {
    mixins.register("main") {
        mixin("default", "neruina.mixins.json")
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

tasks {
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    build {
        dependsOn("runData")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }

    processResources {
        exclude("fabric.mod.json", "META-INF/mods.toml", "neruina-forge.mixins.json")
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

    type = STABLE
    file = tasks.jar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })

    displayName = "${mod("name")} Neoforge ${mod("version")} for $minecraft"
    version = mod("version")
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
    modLoaders.add(loader)

    val compatibleVersions = sc.properties.raw("mod", "compatible_versions").to<List<String>>()

    modrinth {
        projectId = property("publishing.modrinth") as String
        accessToken = mrTokenProvider
        minecraftVersions.addAll(compatibleVersions)
        requires("configurable")
    }

    curseforge {
        projectId = property("publishing.curseforge") as String
        accessToken = cfTokenProvider
        minecraftVersions.addAll(compatibleVersions)
        requires("configurable")
    }
}