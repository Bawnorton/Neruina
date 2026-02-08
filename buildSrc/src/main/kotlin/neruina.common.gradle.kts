import neruina.utils.mod

plugins {
    idea
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val split = name.lastIndexOf('-')
val minecraft: String = name.substring(0, split)
val loader = name.substring(split + 1)

tasks {
    named<ProcessResources>("processResources") {
        exclude {
            val awExclude = it.name.endsWith(".accesswidener") && it.name != "$minecraft.accesswidener"
            val atExclude = it.name.endsWith("-accesstransformer.cfg") && it.name != "$minecraft-accesstransformer.cfg"
            awExclude || atExclude
        }
        val compatibleVersionString = mod("compatible_versions")!!
        val compatibleVersions = compatibleVersionString.split(",").map { it.trim() }

        val props = mapOf(
            "mod_id" to mod("id"),
            "mod_name" to mod("name"),
            "mod_version" to mod("version"),
            "mod_description" to mod("description"),
            "mod_license" to mod("license"),
            "minecraft_version" to minecraft,
            "minecraft_dependency" to compatibleVersions.first(),
            "pack_format" to 71
        )

        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml", "pack.mcmeta")) {
            expand(props)
        }
    }
}

apply {
    project.extensions.extraProperties.set("minecraft", minecraft)
    project.extensions.extraProperties.set("loader", loader)
}
