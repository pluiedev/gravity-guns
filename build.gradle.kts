plugins {
    id("fabric-loom") version "0.8-SNAPSHOT"
    kotlin("jvm") version "1.5.10"
    `maven-publish`
}

repositories {

}

dependencies {
    val minecraftVersion: String by project
    val yarnVersion: String by project
    val floaderVersion: String by project
    val fapiVersion: String by project
    val flkVersion: String by project

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion:v2")
    modImplementation("net.fabricmc:fabric-loader:$floaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

    modImplementation("net.fabricmc:fabric-language-kotlin:$flkVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16

    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"

        // Minecraft 1.17 (21w19a) upwards uses Java 16.
        options.release.set(16)
    }

    //compileKotlin.kotlinOptions.jvmTarget = "16"
    jar {
        from("LICENSE") {
            val archivesBaseName: String by rootProject
            rename { "${it}_$archivesBaseName"}
        }
    }

    processResources {
        inputs.property("version", rootProject.version)

        filesMatching("fabric.mod.json") {
            expand("version" to rootProject.version)
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            // add all the jars that should be included when publishing to maven

            artifact(tasks.remapJar) {
                classifier = null
            }
            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
                builtBy(tasks.remapSourcesJar)
            }
        }
    }
    repositories {
        mavenLocal()
    }
}