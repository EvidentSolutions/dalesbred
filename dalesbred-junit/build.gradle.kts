description = "Dalesbred JUnit-support"

dependencies {
    compile(project(":dalesbred"))
    compile("junit:junit")
    compile("javax.inject:javax.inject")

    compile("org.jetbrains:annotations")

    testCompile("org.hsqldb:hsqldb")
    testCompile("ch.qos.logback:logback-core")
    testCompile("ch.qos.logback:logback-classic")
}
