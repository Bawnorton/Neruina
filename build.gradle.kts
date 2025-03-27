@file:Suppress("UnstableApiUsage")

plugins {
    `maven-publish`
    kotlin("jvm") version "2.1.20"
    id("dev.architectury.loom") version "1.10-SNAPSHOT"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "0.5.+"
}

val mod = ModData(project)
val loader = LoaderData(project, loom.platform.get().name.lowercase())
val minecraftVersion = MinecraftVersionData(stonecutter)

version = "${mod.version}-$loader+$minecraftVersion"
group = mod.group
base.archivesName.set(mod.name)

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://cursemaven.com")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.bawnorton.com/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")

    include(implementation("org.kohsuke:github-api:${property("kohsuke_github")}")!!)
    include(implementation("org.apache.httpcomponents.core5:httpcore5:${property("httpcore5")}")!!)
    include(implementation("com.fasterxml.jackson.core:jackson-core:${property("jackson")}")!!)
    include(implementation("com.fasterxml.jackson.core:jackson-databind:${property("jackson")}")!!)
    include(implementation("com.fasterxml.jackson.core:jackson-annotations:${property("jackson")}")!!)
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }
}

tasks {
    withType<JavaCompile> {
        options.release = minecraftVersion.javaVersion()
    }

    processResources {
        val modMetadata = mapOf(
            "description" to mod.description,
            "version" to mod.version,
            "minecraft_dependency" to mod.minecraftDependency,
            "loader_version" to loader.getVersion()
        )

        inputs.properties(modMetadata)
        filesMatching("fabric.mod.json") { expand(modMetadata) }
        filesMatching("META-INF/neoforge.mods.toml") { expand(modMetadata) }
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(minecraftVersion.javaVersion())
    targetCompatibility = JavaVersion.toVersion(minecraftVersion.javaVersion())
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }
}

if(loader.isFabric) {
    dependencies {
        modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader")}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}")

        include(implementation("org.apache.maven:maven-artifact:${property("maven_artifact")}")!!)

        mappings("net.fabricmc:yarn:$minecraftVersion+build.${property("yarn_build")}:v2")
    }

    tasks {
        processResources {
            exclude("**/neoforge.mods.toml")
        }
    }
}

if (loader.isNeoForge) {
    dependencies {
        neoForge("net.neoforged:neoforge:${loader.getVersion()}")

        mappings(loom.layered {
            mappings("net.fabricmc:yarn:$minecraftVersion+build.${property("yarn_build")}:v2")
            if (minecraftVersion.lessThan("1.21")) {
                mappings("dev.architectury:yarn-mappings-patch-neoforge:1.20.5+build.3")
            } else if (minecraftVersion.lessThan("1.21.4")) {
                mappings("dev.architectury:yarn-mappings-patch-neoforge:1.21+build.4")
            } else {
                mappings("dev.architectury:yarn-mappings-patch-neoforge:1.21+build.4")
            }
        })
    }

    tasks {
        processResources {
            exclude("**/fabric.mod.json")
        }
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
            groupId = "${mod.group}.${mod.id}"
            artifactId = "${mod.id}-$loader"
            version = "${mod.version}+$minecraftVersion"

            from(components["java"])
        }
    }
}


publishMods {
    file = tasks.remapJar.get().archiveFile
    val tag = "$loader-${mod.version}+$minecraftVersion"
    changelog = "[Changelog](https://github.com/Bawnorton/Neruina/blob/stonecutter/CHANGELOG.md)"
    displayName = "${mod.name} ${loader.toString().replaceFirstChar { it.uppercase() }} ${mod.version} for $minecraftVersion"
    type = STABLE
    modLoaders.add(loader.toString())

    github {
        accessToken = providers.gradleProperty("GITHUB_TOKEN")
        repository = "Bawnorton/Neruina"
        commitish = "stonecutter"
        tagName = tag
    }

    modrinth {
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
        projectId = mod.modrinthProjId
        minecraftVersions.addAll(mod.supportedVersions.split(", "))
        if(loader.isFabric) {
            requires {
                slug = "fabric-api"
            }
        }
    }

    curseforge {
        accessToken = providers.gradleProperty("CURSEFORGE_TOKEN")
        projectId = mod.curseforgeProjId
        minecraftVersions.addAll(mod.supportedVersions.split(", "))
        if(loader.isFabric) {
            requires {
                slug = "fabric-api"
            }
        }
    }
}