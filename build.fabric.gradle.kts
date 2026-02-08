@file:Suppress("UnstableApiUsage")

import neruina.utils.applyMixinDebugSettings
import neruina.utils.deps
import neruina.utils.mod

plugins {
    kotlin("jvm")
    `maven-publish`
    id("neruina.common")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.15"
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
base.archivesName = "${mod("id")}-${mod("version")}+$minecraft-$loader"

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        deps("parchment") {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }
    })

    modImplementation("net.fabricmc:fabric-loader:0.18.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps("fabric_api")}")

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
        modImplementation(annotationProcessor("com.bawnorton.configurable:configurable-$loader:$it")!!)
    }
}

java {
    withSourcesJar()
    if(stonecutter.eval(minecraft, "<=1.20.1")) {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    } else {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
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

    mixin {
        useLegacyMixinAp = true
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
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude("**/neoforge.mods.toml, **/mods.toml")
    }
}

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "bawnorton"
            url = uri("https://maven.bawnorton.com/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
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
    val mrToken = providers.gradleProperty("MODRINTH_TOKEN")
    val cfToken = providers.gradleProperty("CURSEFORGE_TOKEN")

    type = STABLE
    file = tasks.remapJar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })

    displayName = "${mod("name")} Fabric ${mod("version")} for $minecraft"
    version = mod("version")
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }
    modLoaders.add(loader)

    val compatibleVersionString = mod("compatible_versions")!!
    val compatibleVersions = compatibleVersionString.split(",").map { it.trim() }

    modrinth {
        projectId = property("publishing.modrinth") as String
        accessToken = mrToken
        minecraftVersions.addAll(compatibleVersions)
        requires("configurable")
    }

    curseforge {
        projectId = property("publishing.curseforge") as String
        accessToken = cfToken
        minecraftVersions.addAll(compatibleVersions)
        requires("configurable")
    }
}