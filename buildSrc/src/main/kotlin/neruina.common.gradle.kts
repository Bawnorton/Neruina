import neruina.utils.deps
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

        fun listProp(name: String): List<String> {
            var index = 0;
            var list = mutableListOf<String>()
            while (true) {
                val element = project.findProperty("$name.$index") ?: break
                list.add(element as String)
                index++
            }
            return list
        }

        val configurableDep = if (loader == "fabric") deps("configurable")?.let {
            "\", \"configurable\": \"~${it.split("+")[0]}"
        } ?: "" else ""

        val props = mapOf(
            "mod_id" to mod("id"),
            "mod_name" to mod("name"),
            "mod_version" to mod("version"),
            "mod_description" to mod("description"),
            "mod_license" to mod("license"),
            "minecraft_version" to minecraft,
            "minecraft_dependency" to listProp("mod.compatible_versions").first() + configurableDep,
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
