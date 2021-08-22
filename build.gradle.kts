plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("fabric-loom") version "0.8-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.3.0"
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.0"
    `maven-publish`
}

repositories {
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") { name = "GeckoLib" }
    maven("https://hephaestus.dev/release") { name = "Haven's Maven" }
    maven("https://maven.dblsaiko.net/") { name = "2xsaiko's Maven" }

    maven("https://maven.pkg.github.com/LambdAurora/LambDynamicLights") {
        name = "LambDynamicLights"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
    maven("https://maven.terraformersmc.com/releases/") { name = "Terraformers" }
    maven("https://aperlambda.github.io/maven") { name = "AperLambda" }
    maven("https://maven.shedaniel.me/") { name = "shedaniel's Maven" }
    maven("https://jitpack.io") { name = "JitPack" }

    mavenLocal()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.fabric.yarn){
        artifact { classifier = "v2" }
    }
    modImplementation(libs.bundles.fabric)
    modImplementation(libs.modmenu)
    modApi(libs.geckolib) {
        artifact { classifier = "dev" }
    }
    modApi(libs.clothconfiglite)
    include(libs.clothconfiglite)

    modApi(libs.rayon)

    modImplementation(libs.worldmesher)
    include(libs.worldmesher)
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
            //from(components["java"])
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