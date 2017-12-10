description = "Dalesbred - a database access library"

apply {
    plugin("kotlin")
    plugin("osgi")
}

val springVersion: String by rootProject.extra
val kotlinVersion: String by rootProject.extra
val jetbrainsAnnotationsVersion: String by rootProject.extra
val hsqldbVersion: String by rootProject.extra
val junitVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra

dependencies {
    compile("org.slf4j:slf4j-api:1.7.25")
    compile("joda-time:joda-time:2.9.9")
    compile("org.threeten:threetenbp:1.3.6")
    compile("org.springframework:spring-context:$springVersion")
    compile("org.springframework:spring-jdbc:$springVersion")
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")

    compile("org.postgresql:postgresql:42.1.4")

    testCompile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlinVersion")
    testCompile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testCompile("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testCompile("org.hsqldb:hsqldb:$hsqldbVersion")
    testCompile("com.h2database:h2:1.4.196")
    testCompile("mysql:mysql-connector-java:5.1.45")
    testCompile("junit:junit:$junitVersion")
    testCompile("org.mockito:mockito-core:2.13.0")
    testCompile("ch.qos.logback:logback-core:$logbackVersion")
    testCompile("ch.qos.logback:logback-classic:$logbackVersion")
}
