plugins {
    id("dalesbred.java-library-conventions")
}

description = "Dalesbred JUnit-support"

dependencies {
    api(project(":dalesbred"))
    api(libs.junit)
    api(libs.javax.inject)
    api(libs.jetbrains.annotations)

    testImplementation(libs.jdbc.hsqldb)
    testImplementation(libs.logback.core)
    testImplementation(libs.logback.classic)
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "org.dalesbred.junit"
    }
}
