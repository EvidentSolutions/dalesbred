plugins {
    id("org.asciidoctor.jvm.convert")
    id("dalesbred.common-conventions")
}

version = "1.3.5"

tasks.asciidoctor {
    baseDirFollowsSourceDir()
    attributes = mapOf("revnumber" to project.version.toString())
}

task("publish") {
    description = "Publishes the artifacts to Maven Central"

    dependsOn(
        ":dalesbred:publishAllPublicationsToSonatypeRepository",
        ":dalesbred-junit:publishAllPublicationsToSonatypeRepository"
    )
}

