description = "Dalesbred - a database access library"

plugins {
    kotlin("jvm")
    `java-library`
    osgi
}

dependencies {
    compile("org.slf4j:slf4j-api")
    compile("joda-time:joda-time")
    compile("org.threeten:threetenbp")
    compile("org.springframework:spring-context")
    compile("org.springframework:spring-jdbc")
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains:annotations")

    compile("org.postgresql:postgresql")
    compile("com.oracle.ojdbc:ojdbc8")

    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("org.hsqldb:hsqldb")
    testImplementation("com.h2database:h2")
    testImplementation("mysql:mysql-connector-java")
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("ch.qos.logback:logback-core")
    testImplementation("ch.qos.logback:logback-classic")
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "org.dalesbred"
    }
}
