pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

sourceControl {
    gitRepository(uri("https://github.com.cnpmjs.org/LeoCTH/Rayon.git")) {
        producesModule("com.github.LazuriteMC:Rayon")
    }
}

rootProject.name = "gravity-guns"