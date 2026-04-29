@file:Suppress("UnstableApiUsage")

import neruina.utils.*

plugins {
    kotlin("jvm")
    `maven-publish`
    id("neruina.common")
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.15"
    id("dev.isxander.secrets") version "0.1.0"
}

repositories {
    mavenLocal()
    maven("https://maven.bawnorton.com/releases")
    maven("https://maven.parchmentmc.org")
    maven("https://repo.jenkins-ci.org/public/")
    maven("https://api.modrinth.com/maven")
    maven("https://cursemaven.com")
}

val minecraft: String by project
val loader: String by project

sc.properties.tags(minecraft)

base.archivesName = "${mod("id")}-${mod("version")}+$minecraft-$loader"

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")

    implementation("net.fabricmc:fabric-loader:0.19.2")
    implementation("net.fabricmc.fabric-api:fabric-api:${deps("fabric_api")}")

    deps("kohsuke_github") {
        include(implementation("org.kohsuke:github-api:$it")!!)
    }
    deps("http_core") {
        include(implementation("org.apache.httpcomponents:httpcore:$it")!!)
    }
    deps("http_client") {
        include(implementation("org.apache.httpcomponents:httpclient:$it")!!)
    }
    deps("configurable") {
        implementation(annotationProcessor("com.bawnorton.configurable:configurable-$loader:$it")!!)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
        appendProjectPathToConfigName = false
    }

    runConfigs["client"].apply {
        programArgs("--username=Bawnorton", "--uuid=17c06cab-bf05-4ade-a8d6-ed14aaf70545")
        name = "Fabric Client $minecraft"
    }

    runConfigs["server"].apply {
        name = "Fabric Server $minecraft"
    }

    afterEvaluate {
        runConfigs.configureEach {
            applyMixinDebugSettings(::vmArg, ::property)
        }
    }
}

fletchingTable {
    mixins.register("main") {
        mixin("default", "${mod("id")}.mixins.json")
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
    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${mod("version")}"))
        dependsOn("build")
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude("**/neoforge.mods.toml, **/mods.toml")
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
    additionalFiles.from(tasks.named<Jar>("sourcesJar").map { it.archiveFile.get() })

    displayName = "${mod("name")} Fabric ${mod("version")} for $minecraft"
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