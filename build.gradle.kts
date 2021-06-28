plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("fabric-loom") version "0.8-SNAPSHOT"
    kotlin("jvm") version "1.5.10"
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
}

dependencies {
    val minecraftVersion: String by project
    val yarnVersion: String by project
    val floaderVersion: String by project
    val fapiVersion: String by project
    val flkVersion: String by project
    val geckolibVersion: String by project
    val rayonVersion: String by project
    val hermesVersion: String by project
    val libbulletjmeVersion: String by project

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion:v2")
    modImplementation("net.fabricmc:fabric-loader:$floaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

    modImplementation("net.fabricmc:fabric-language-kotlin:$flkVersion")
    modApi("software.bernie.geckolib:geckolib-fabric-1.17:$geckolibVersion:dev")

    // FIXME: 2xsaiko's fat dep (i.e. net.dblsaiko.rayon:rayon) doesn't show up correctly as separate modules in IDEA.
    implementation("com.github.stephengold:Libbulletjme:$libbulletjmeVersion")
    shadow("com.github.stephengold:Libbulletjme:$libbulletjmeVersion")

    modApi("net.dblsaiko.rayon:rayon-core:$rayonVersion")
    modApi("net.dblsaiko.rayon:rayon-entity:$rayonVersion")
    modRuntime("dev.inkwell:hermes:$hermesVersion")

    modImplementation("dev.lambdaurora:lambdynamiclights:2.0.1+1.17") {
        exclude(group = "com.google.guava")
    }

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