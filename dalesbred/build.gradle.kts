description = "Dalesbred - a database access library"

plugins {
    id("dalesbred.java-library-conventions")
    kotlin("jvm")
}

java {
    registerFeature("joda") {
        usingSourceSet(sourceSets["main"])
    }
    registerFeature("threeten") {
        usingSourceSet(sourceSets["main"])
    }
    registerFeature("spring") {
        usingSourceSet(sourceSets["main"])
    }
    registerFeature("kotlin") {
        usingSourceSet(sourceSets["main"])
    }
    registerFeature("annotations") {
        usingSourceSet(sourceSets["main"])
    }
    registerFeature("postgresql") {
        usingSourceSet(sourceSets["main"])
    }
    registerFeature("oracle") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api")
    "jodaImplementation"("joda-time:joda-time")
    "threetenImplementation"("org.threeten:threetenbp")
    "springImplementation"("org.springframework:spring-context")
    "springImplementation"("org.springframework:spring-jdbc")
    "kotlinImplementation"(kotlin("stdlib-jdk8"))
    "annotationsImplementation"("org.jetbrains:annotations")

    "postgresqlImplementation"("org.postgresql:postgresql")
    "oracleImplementation"("com.oracle.database.jdbc:ojdbc8")

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
