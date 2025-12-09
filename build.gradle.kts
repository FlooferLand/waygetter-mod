plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.2"
    id("dev.kikugie.stonecutter")
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
    id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.22"
    id("fabric-loom") version "1.13-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "1.0.0"
}

repositories {
    mavenCentral()

    // Mappings
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }

    // GeckoLib
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") { name = "GeckoLib" }

    // Mod Menu
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")

    // Dev Auth
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    // Veil
    maven("https://maven.blamejared.com") { name = "BlameJared Maven (CrT / Bookshelf)" }
}

val minecraft = stonecutter.current.version
val modVersion = property("mod.version") as String
group = "com.flooferland"
version = modVersion
base {
    archivesName.set(property("mod.id") as String)
}
val isAlpha = "alpha" in modVersion
val isBeta = "beta" in modVersion

val java = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
    JavaVersion.VERSION_21 else JavaVersion.VERSION_17
val kotlinVersion = "2.2.20"
val loader = stonecutter.current.project.split("-").last()
val isFabric = loader == "fabric"
val isNeoforge = loader == "neoforge"

stonecutter {
    constants["fabric"] = isFabric
    constants["neoforge"] = isNeoforge
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("waygetter") {
            sourceSet("main")
            sourceSet("client")
        }
    }
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Shared run folder between versions
    }
}

fun versionedDep(name: String): String {
    val name = "deps.$minecraft.$name"
    return findProperty(name) as? String ?: error("Unable to find versioned property '$name' for Minecraft $minecraft")
}
fun dep(name: String): String = property("deps.${name}") as String
dependencies {
    @Suppress("UnstableApiUsage")
    mappings(loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraft:${versionedDep("parchment")}@zip")
    })
    minecraft("com.mojang:minecraft:$minecraft")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    if (isFabric) {
        if (dep("fabric_language_kotlin").split("+")[1] != "kotlin.$kotlinVersion") {
            error("Fabric Language Kotlin and Kotlin version do not match up")
        }
        modImplementation("net.fabricmc:fabric-loader:${dep("fabric_loader")}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${versionedDep("fabric_api")}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${dep("fabric_language_kotlin")}")
    }

    // GeckoLib
    modImplementation("software.bernie.geckolib:geckolib-${loader}-${minecraft}:${versionedDep("geckolib")}")

    // Veil
    modImplementation("foundry.veil:veil-$loader-$minecraft:${versionedDep("veil")}") {
        exclude(group = "maven.modrinth")
        exclude(group = "me.fallenbreath")
    }

    // Config
    implementation("com.moandjiezana.toml:toml4j:${dep("toml4j")}")
    include("com.moandjiezana.toml:toml4j:${dep("toml4j")}")
    modApi("com.terraformersmc:modmenu:${versionedDep("mod_menu")}")

    // Outside dependencies
    modRuntimeOnly("me.djtheredstoner:DevAuth-$loader:${dep("dev_auth")}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.WARN

    val fabricLanguageKotlin = "${dep("fabric_language_kotlin")}+kotlin.$kotlinVersion"

    val properties = mapOf(
        "minecraft_range" to versionedDep("minecraft_range"),
        "java" to java.toString(),
        "kotlin" to kotlinVersion,
        "fabric_loader" to dep("fabric_loader"),
        "fabric_language_kotlin" to fabricLanguageKotlin,
        "mod_menu" to versionedDep("mod_menu"),
        "geckolib" to versionedDep("geckolib"),
        "archivesName" to base.archivesName.get(),
        "archivesBaseName" to base.archivesName.get(),

        // Mod props
        "mod_id" to rootProject.property("mod.id") as String,
        "mod_name" to rootProject.property("mod.name") as String,
        "mod_description" to rootProject.property("mod.description") as String,
        "mod_license" to rootProject.property("mod.license") as String,
        "mod_version" to version as String,
    )
    properties.forEach() { (k, v) ->
        inputs.property(k, v)
    }

    exclude("**/*.lnk")
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
    filesMatching("${base.archivesName.get()}.client.mixins.json") {
        expand(properties)
    }
    filesMatching("${base.archivesName.get()}.main.mixins.json") {
        expand(properties)
    }
}

java {
    withSourcesJar()
    targetCompatibility = java
    sourceCompatibility = java
}

kotlin {
    jvmToolchain(java.ordinal + 1)
}

publishMods {
    // Utils
    fun versionList(prop: String) = versionedDep(prop)
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // Release
    val stableMcVersions = versionList("minecraft_lists")
    displayName.set("$modVersion for ${stableMcVersions.joinToString(", ") }")
    file.set(tasks.remapJar.get().archiveFile)
    changelog.set(
        rootProject.file("changelogs/$modVersion.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."
    )
    type.set(when {
        isAlpha -> ALPHA
        isBeta -> BETA
        else -> STABLE
    })
    modLoaders.add("fabric")
    // dryRun = true

    val modrinthId = property("mod.modrinthId") as String
    val modrinthToken = runCatching { System.getenv("tokens.modrinth") }
        .getOrElse { error("Modrinth publish token wasn't provided") }
    if (modrinthId.isNotBlank() && modrinthToken != null) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(modrinthToken)
            minecraftVersions.addAll(stableMcVersions)

            // TODO: Figure out a nice/safe way to add versions to the dependencies
            requires { slug.set("fabric-api") }
            requires { slug.set("fabric-language-kotlin") }
            requires { slug.set("simple-voice-chat") }
            optional { slug.set("modmenu") }
            optional { slug.set("figura") }
        }
    }

    val curseforgeId = property("mod.curseforgeId") as String
    val curseforgeToken = runCatching { System.getenv("tokens.curseforge") }
        .getOrElse { error("Curseforge publish token wasn't provided") }
    if (curseforgeId.isNotBlank() && curseforgeToken != null) {
        curseforge {
            projectId.set(curseforgeId)
            accessToken.set(curseforgeToken)
            minecraftVersions.addAll(stableMcVersions)

            // TODO: Figure out a nice/safe way to add versions to the dependencies
            requires { slug.set("fabric-api") }
            requires { slug.set("fabric-language-kotlin") }
            requires { slug.set("simple-voice-chat") }
            optional { slug.set("modmenu") }
            optional { slug.set("figura") }
        }
    }
}
