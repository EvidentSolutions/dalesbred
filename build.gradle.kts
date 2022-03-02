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
    description = "Publishes both the artifacts and the website"

    dependsOn(
        ":website:publishGhPages",
        ":dalesbred:publishAllPublicationsToSonatypeRepository",
        ":dalesbred-junit:publishAllPublicationsToSonatypeRepository"
    )
}
