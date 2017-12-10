description = "Dalesbred JUnit-support"

val jetbrainsAnnotationsVersion: String by rootProject.extra
val hsqldbVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra

dependencies {
    compile(project(":dalesbred"))
    compile("junit:junit:$junitVersion")
    compile("javax.inject:javax.inject:1")

    compile("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")

    testCompile("org.hsqldb:hsqldb:$hsqldbVersion")
    testCompile("ch.qos.logback:logback-core:$logbackVersion")
    testCompile("ch.qos.logback:logback-classic:$logbackVersion")
}
