plugins {
    id("org.asciidoctor.jvm.convert")
    id("pl.allegro.tech.build.axion-release")
    id("dalesbred.common-conventions")
}

scmVersion {
    localOnly = true
    tag.prefix = "v"
    tag.versionSeparator = ""
}

version = scmVersion.version

tasks.asciidoctor {
    baseDirFollowsSourceDir()
    attributes = mapOf("revnumber" to project.version.toString())
}

task("publish") {
    description = "Publishes both the artifacts and the website"

    dependsOn(":website:publishGhPages",
            ":dalesbred:uploadArchives",
            ":dalesbred-junit:uploadArchives")
}
