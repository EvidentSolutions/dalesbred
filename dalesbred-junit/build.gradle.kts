plugins {
    java
    `java-library`
}

description = "Dalesbred JUnit-support"

dependencies {
    api(project(":dalesbred"))
    api("junit:junit")
    api("javax.inject:javax.inject")
    api("org.jetbrains:annotations")

    testImplementation("org.hsqldb:hsqldb")
    testImplementation("ch.qos.logback:logback-core")
    testImplementation("ch.qos.logback:logback-classic")
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "org.dalesbred.junit"
    }
}
