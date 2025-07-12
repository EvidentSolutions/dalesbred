plugins {
    id("org.asciidoctor.jvm.convert")
    id("dalesbred.common-conventions")
}

tasks.asciidoctor {
    baseDirFollowsSourceDir()
    attributes = mapOf("revnumber" to project.version.toString())
}
