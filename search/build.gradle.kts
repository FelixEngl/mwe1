@file:Suppress("PropertyName", "LocalVariableName", "UNUSED_VARIABLE")

import java.text.SimpleDateFormat
import java.util.*
import cfg.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val serverApiVersion: String by project

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("configure-source-sets-of-search")
    id("kotlinx-atomicfu")
    application
    idea
    id("org.jetbrains.dokka")
    id("com.google.devtools.ksp")
}

// region Configurations

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

atomicfu {
    variantJVM = AtmoicFuJVMVariant.VH // JVM transformation variant: FU,VH, or BOTH
    transformJvm = false
}

configurations {
    all {
        exclude("log4j", "log4j")
        exclude("org.slf4j", "log4j-slf4j-impl")
        exclude("org.slf4j", "slf4j-log4j12")
    }
}

//endregion

kotlin {

    js(IR) {
        binaries.executable()

        browser {

            commonWebpackConfig {
                cssSupport.enabled = true
                sourceMaps = true
                mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.DEVELOPMENT
            }

            distribution {
                directory = File(buildDir, "jsDistribution")
            }


            runTask {
                outputFileName = "firmensuche.js"
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    //Kotlin
    val ktorVersion: String by project
    val htmlBuilderVersion: String by project
    val serialisationVersion: String by project
    val kotlinLoggingVersion: String by project
    val kotlinCoroutinesVersion: String by project
    val kotlinDate: String by project
    val mockkVersion: String by project

    // Custom
    val slimMongoVersion: String by project
    val kldaVersion: String by project
    val kotlinToolkitVersion: String by project
    val stringrectanglebuilderVersion: String by project
    val kTestFactoriesVersion: String by project

    // Kotlin JS
    val kotlinReactVersion: String by project
    val kotlinReactDomVersion: String by project
    val kotlinReactRouterDom: String by project
    val kotlinStyledVersion: String by project
    val kotlinStyledNextVersion: String by project
    val kotlinExtensionsVersion: String by project

    //Java
    val jsoupVersion: String by project
    val log4jVersion: String by project
    val luceneVersion: String by project
    val apacheMathVersion: String by project
    val geodesyVersion: String by project
    val boilerpipeVersion: String by project //sehr alt, neues framework finden! //boilerpipe v2 auch existent
    val tikaVersion: String by project
    val jUnitJupiterVersion: String by project

    // JS
    val reactWordcloudVersion: String by project
    val leafletVersion: String by project
    val leafletMarkerclusterVersion: String by project

    sourceSets {
        val commonMain by getting {

            dependencies {
                implementation(kotlin("reflect"))

                implementation(project(":pojos"))

                // Serialisation
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialisationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:$serialisationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialisationVersion")

                // Ktor
                implementation("io.ktor:ktor-io:$ktorVersion")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

                // Logging
                implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

                // Dates
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDate")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {

            dependencies {
                // react
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:$kotlinExtensionsVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$kotlinReactVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$kotlinReactDomVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:$kotlinReactRouterDom")

                // Styled
//                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:$kotlinStyledVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled-next:$kotlinStyledNextVersion")

                // Word Cloud
                // https://github.com/timdream/wordcloud2.js
                implementation(npm("react-wordcloud", reactWordcloudVersion))
                // Leaflet
                implementation(npm("leaflet", leafletVersion))
                implementation(npm("leaflet.markercluster", leafletMarkerclusterVersion))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val jvmMain by getting {

            dependencies {

                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))

                // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
                implementation("org.apache.commons:commons-math3:$apacheMathVersion")

                // Server
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
                implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-locations-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-compression-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-call-id-jvm:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$htmlBuilderVersion")
                implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
                implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

                // Logging
                // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
                implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
                implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
                implementation("org.apache.logging.log4j:log4j-1.2-api:$log4jVersion")


                // Lucene
                // https://mvnrepository.com/artifact/org.apache.lucene/lucene-queryparser
                implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
                // https://mvnrepository.com/artifact/org.apache.lucene/lucene-highlighter
                implementation("org.apache.lucene:lucene-highlighter:$luceneVersion")
                // https://mvnrepository.com/artifact/org.apache.lucene/lucene-analyzers-common
                implementation("org.apache.lucene:lucene-analyzers-common:$luceneVersion")
                // https://mvnrepository.com/artifact/org.apache.lucene/lucene-queryparser
                implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
                implementation("org.apache.lucene:lucene-facet:$luceneVersion")

                // Analysis
                // https://mvnrepository.com/artifact/org.jsoup/jsoup
                implementation("org.jsoup:jsoup:$jsoupVersion")
                // https://mvnrepository.com/artifact/org.gavaghan/geodesy
                implementation("org.gavaghan:geodesy:$geodesyVersion")
                // https://mvnrepository.com/artifact/de.l3s.boilerpipe/boilerpipe
                implementation("de.l3s.boilerpipe:boilerpipe:$boilerpipeVersion")
                // https://mvnrepository.com/artifact/org.apache.tika/tika-langdetect
                implementation("org.apache.tika:tika-langdetect:$tikaVersion")

            }

        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-params:$jUnitJupiterVersion")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitJupiterVersion")

                implementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                implementation("io.mockk:mockk:$mockkVersion")
            }
        }


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
                optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
            }
        }
    }
}

//region Tasks for everywhere

val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")

val jsBrowserDevelopmentWebpack = tasks.getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    this.mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.DEVELOPMENT
    this.sourceMaps = true
}
//endregion

//region Custom Metadata
tasks {
    val generateCustomMetadata = createCustomMetadataTask {

        hasBuildTimeProperty = true

        metadataFor(kotlin.sourceSets.commonMain){
            createMetadataMap = true
            add("SOFTWARE_VERSION" metaStringF {"v${project.version}-${buildTime.toStamp()}"})
            add("SOFTWARE_SERVER_REST_API_VERSION" meta serverApiVersion)
            add("SOFTWARE_CREATED_BY" meta "Lehrstuhl fuer Medieninformatik")
            add("MINF" meta true)
        }

        metadataFor(kotlin.sourceSets.jvmMain){
            createMetadataMap = true
            createMetadataValueSequence = true

            add("MAIN_CLASS" meta application.mainClass.get())
            add("SOFTWARE_NAME" meta "Firmensuche - Server")
            add(("BUILD_TIMESTAMP" metaDateF {buildTime}).instant.kt)
            add("BUILD_GRADLE_VERSION" meta gradle.gradleVersion)
            add("BUILD_JDK" meta "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})")
            add("BUILD_OS" meta "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}")
        }

        metadataFor(kotlin.sourceSets.jsMain){
            createMetadataMap = true
            add("SOFTWARE_NAME" meta "Firmensuche - Client")
        }
    }

    getByName<KotlinCompile>("compileKotlinJvm").dependsOn(generateCustomMetadata)


    getByName("compileKotlinJs").dependsOn(generateCustomMetadata)
}
//endregion

//region Dokka
tasks {
    dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("javadoc"))

        dokkaSourceSets {

            configureEach {
                includeNonPublic.set(true)
                skipDeprecated.set(false)
                reportUndocumented.set(true)
            }

            val commonMain by getting {}

            val jvmMain by getting {
                includes.from(files("packages.md"))
                dependsOn(commonMain)
            }
        }
    }
}
//endregion

//region Set name of js-file
tasks {
    withType<KotlinWebpack> {
        //Put the JS-Mess there
        outputFileName = "firmensuche.js"
    }
}
//endregion

//region Unit Tests
tasks {

    val jsBrowserDevelopmentWebpack = getByName<KotlinWebpack>("jsBrowserDevelopmentWebpack")

    val jvmProcessResources = getByName<CopySpec>("jvmProcessResources") {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    val jvmTestProcessResources = getByName<CopySpec>("jvmTestProcessResources") {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    // Tests
    val copyToTestData = register<Copy>("copyToTestData") {
        dependsOn(jsBrowserDevelopmentWebpack)
        group = "copy"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        destinationDir = File(projectDir, "tests")

        from(jsBrowserDevelopmentWebpack.destinationDirectory){
            exclude("**/*.tar", "**/*.zip")
            into("served")
        }

        from("additional_data")
    }

    val jvmTest = named<Test>("jvmTest") {
        dependsOn(copyToTestData)
        useJUnitPlatform()
        maxHeapSize = "48g"
        reports {
            html.required.set(true)
            html.outputLocation.set(File(project.buildDir, "junit/reports/html"))
        }
        testLogging {
            events.add(TestLogEvent.PASSED)
            events.add(TestLogEvent.FAILED)
        }
    }
}
//endregion

//region Distribution Configs


tasks.startScripts {
    dependsOn(jsBrowserProductionWebpack)
    classpath = files("\$APP_HOME/lib/*")
}

distributions {
    main {

        contents {
            from(jsBrowserProductionWebpack.destinationDirectory){
                into("served")
            }

            from("additional_data")
        }
    }
}
//endregion

//region JS Development Help

tasks {

    val installDist = getByName<Sync>("installDist")
    val updateInstallationWithBrowserDevelopmentWebpack = create<Copy>("updateInstallationWithBrowserDevelopmentWebpack") {
        group = "react development helpers"

        dependsOn(jsBrowserDevelopmentWebpack)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        destinationDir = installDist.destinationDir

        from(jsBrowserDevelopmentWebpack.destinationDirectory){
            into("served")
        }
    }

    val updateInstallationWithBrowserProductionWebpack = create<Copy>("updateInstallationWithBrowserProductionWebpack") {
        group = "react development helpers"

        dependsOn(jsBrowserProductionWebpack)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        destinationDir = installDist.destinationDir

        from(jsBrowserProductionWebpack.destinationDirectory){
            into("served")
        }
    }
}

//endregion

//region Helpers
fun Project.atomicfu(block: kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension.() -> Unit) =
    extensions.getByName<kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension>("atomicfu").apply(block)

operator fun File.div(part: String) = File(this, part)

enum class AtmoicFuJVMVariant {
    FU, VH, BOTH
}

var kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension.variantJVM: AtmoicFuJVMVariant
    get() = AtmoicFuJVMVariant.valueOf(variant)
    set(value){ variant = value.name }


fun org.gradle.plugins.ide.idea.model.IdeaModule.excludeFile(file: File){
    excludeDirs = excludeDirs.plus(file)
}

fun Date.toStamp() = SimpleDateFormat("yyMMddHHmmss").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(this)
fun Date.toStringRep() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(this)
//endregion
