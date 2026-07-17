plugins {
    kotlin("jvm") version "2.2.0" apply false
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.1.1" apply false
}

stonecutter active "26.2-fabric"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
}

stonecutter tasks {
    val ordering = versionComparator.thenComparingInt {
        if (it.metadata.project.contains("fabric")) 1
        else if (it.metadata.project.contains("neoforge")) 2
        else if (it.metadata.project.contains("forge")) 3
        else 0
    }

    order("publishModrinth", ordering)
    order("publishCurseforge", ordering)
}


for (version in stonecutter.versions.map { it.version }.distinct()) tasks.register("publish$version") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishMods") { metadata.version == version })
}

for (version in stonecutter.versions.map { it.version }.distinct()) tasks.register("publishAll${version}FabricPublicationsToBawnortonRepository") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishAllPublicationsToBawnortonRepository") {
        metadata.version == version && metadata.project.contains("fabric")
    })
}

for (version in stonecutter.versions.map { it.version }.distinct()) tasks.register("publishAll${version}NeoforgePublicationsToBawnortonRepository") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishAllPublicationsToBawnortonRepository") {
        metadata.version == version && metadata.project.contains("neoforge")
    })
}