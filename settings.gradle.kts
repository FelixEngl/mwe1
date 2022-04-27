pluginManagement {
    val targetKotlinVersion: String by settings
    val kspVersion: String by settings
    val atomicFUVersion: String by settings
    val dokkaVersion: String by settings
    val shadowVersion: String by settings

    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }

    resolutionStrategy {
        eachPlugin {
            when(requested.id.id) {
                "kotlinx-atomicfu" -> useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${atomicFUVersion}")
            }
        }
    }


    plugins {
        `maven-publish` apply false
        application apply false
        war apply false
        idea apply false
        id("configure-source-sets-of-search") apply false
        id("org.jetbrains.dokka") version dokkaVersion apply false
        id("com.github.johnrengelman.shadow") version shadowVersion apply false

        kotlin("multiplatform") version targetKotlinVersion apply false
        kotlin("plugin.serialization") version targetKotlinVersion apply false

        id("com.google.devtools.ksp") version kspVersion apply false
    }
}

rootProject.name = "firmensuche"

include("pojos")
include("search")
