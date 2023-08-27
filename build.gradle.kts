plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("fabric-loom") version "1.3-SNAPSHOT"
    id("io.github.juuxel.loom-vineflower") version "1.11.0"
}

version = property("mod_version")!!.toString()
val fabricKotlinVersion = property("fabric_kotlin_version")!!

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("fabric_kotlin_version", fabricKotlinVersion)

        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "fabric_kotlin_version" to fabricKotlinVersion,
            )
        }
    }

    withType<JavaCompile>().configureEach {
        options.release.set(17)
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    jar {
        from("LICENSE") {
            rename { "${name}_${base.archivesName.get()}" }
        }
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
