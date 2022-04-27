import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
}

kotlin {

    // We need this for applying the kspPlugin-plugin
    js(IR) {
        configure(listOf(compilations["main"], compilations["test"])) {}
    }

    jvm {
        withJava()

        configure(listOf(compilations["main"], compilations["test"])) {}
    }

    // This does not work... Why?
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }

    targets.all {
        compilations["main"].kotlinOptions {
            allWarningsAsErrors = false
        }
    }
}


tasks {
    getByName<KotlinCompile>("compileKotlinJvm"){
        targetCompatibility = "11"
        sourceCompatibility = "11"
        kotlinOptions.jvmTarget = "11"
    }

    getByName<JavaCompile>("compileJava"){
        targetCompatibility = "11"
        sourceCompatibility = "11"
    }

    getByName<KotlinCompile>("compileTestKotlinJvm"){
        targetCompatibility = "11"
        sourceCompatibility = "11"
        kotlinOptions.jvmTarget = "11"
    }

    getByName<JavaCompile>("compileTestJava"){
        targetCompatibility = "11"
        sourceCompatibility = "11"
    }
}