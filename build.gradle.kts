allprojects {
    // https://docs.gradle.org/current/userguide/rich_versions.html

    group = "de.uniba.minf.fs"
    version = "0.1.2-beta2"
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
