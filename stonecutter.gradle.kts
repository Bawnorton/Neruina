plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21-fabric" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuildAndCollect", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}

stonecutter registerChiseled tasks.register("chiseledPublishMavenLocal", stonecutter.chiseled) {
    group = "publishing"
    ofTask("publishMavenPublicationToMavenLocal")
}

stonecutter registerChiseled tasks.register("chiseledPublishMavenRemote", stonecutter.chiseled) {
    group = "publishing"
    ofTask("publishMavenPublicationToBawnortonRepository")
}

stonecutter configureEach {
    val current = project.property("loom.platform")
    val platforms = listOf("fabric", "forge", "neoforge").map { it to (it == current) }
    consts(platforms)
}
