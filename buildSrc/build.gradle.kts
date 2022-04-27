import org.gradle.api.Project.GRADLE_PROPERTIES
import java.util.Properties

// We need this for getting properties from outer project
applyPropertiesFromParent()

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val targetKotlinVersion: String by project

dependencies {
    implementation(kotlin("gradle-plugin", targetKotlinVersion))
}


fun applyPropertiesFromParent(){
    val props = Properties()
    rootDir.toPath().resolveSibling(GRADLE_PROPERTIES).toFile().inputStream().use {
        props.load(it)
    }
    props.forEach { (k, v) ->
        project.ext[k as String] = v
    }
}


java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
