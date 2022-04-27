import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("configure-source-sets-of-search")
    id("com.google.devtools.ksp")
}

kotlin {

    val serialisationVersion: String by project
    val kotlinLoggingVersion: String by project
    val kotlinDate: String by project

    js(IR) {
        browser {
            commonWebpackConfig {
                mode = Mode.PRODUCTION
                sourceMaps = true
                outputFileName = "pojos.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialisationVersion")
                implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDate")
            }
        }
        val jvmMain by getting {

        }
        val jsMain by getting {

        }
    }

    sourceSets {
        all {
            languageSettings {
                progressiveMode = true

                optIn("kotlin.Experimental")
                optIn("kotlin.ExperimentalMultiplatform")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
        }
    }
}
