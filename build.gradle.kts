import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("fabric-loom") version "1.7-SNAPSHOT"
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
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    modImplementation("dev.isxander:yet-another-config-lib:${property("yacl_version")}")
    modApi("com.terraformersmc:modmenu:${property("modmenu_version")}") {
        exclude("net.fabricmc")
    }
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${property("devauth_version")}")

    implementation("ca.weblite:java-objc-bridge:1.2")
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
        options.release.set(21)
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
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

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
