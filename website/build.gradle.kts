plugins {
    id("org.ajoberstar.github-pages") version "1.6.0"
}

val copySources = task("copySources", type = Copy::class) {
    from("src")
    into("build/dalesbred")
}

val copyApi = task("copyApi", type = Copy::class) {
    from(files(tasks.findByPath(":dalesbred:javadoc")))
    into("build/dalesbred/docs/api/")
}

val copyReference = task("copyReference", type = Copy::class) {
    dependsOn(tasks.findByPath(":asciidoctor"))
    from("../build/asciidoc/html5/")
    into("build/dalesbred/docs/reference/")
}

val assemble by tasks
assemble.dependsOn(copySources, copyApi, copyReference)

val publishGhPages by tasks
publishGhPages.dependsOn(assemble)

githubPages {
    setRepoUri("git@github.com:EvidentSolutions/dalesbred.git")
    deleteExistingFiles = true

    pages(closureOf<CopySpec> {
        from(files("build/dalesbred"))
    })
}
