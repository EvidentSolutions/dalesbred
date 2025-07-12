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
    implementation(libs.slf4j.api)
    "jodaImplementation"(libs.jodaTime)
    "threetenImplementation"(libs.threeten)
    "springImplementation"(libs.spring.context)
    "springImplementation"(libs.spring.jdbc)
    "kotlinImplementation"(kotlin("stdlib-jdk8"))
    "annotationsImplementation"(libs.jetbrains.annotations)

    "postgresqlImplementation"(libs.jdbc.postgresql)
    "oracleImplementation"(libs.jdbc.oracle)

    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(libs.jdbc.hsqldb)
    testImplementation(libs.jdbc.h2)
    testImplementation(libs.jdbc.mysql)
    testImplementation(libs.jdbc.mariadb)
    testImplementation(libs.junit)
    testImplementation(libs.logback.core)
    testImplementation(libs.logback.classic)
}

tasks.jar {
    manifest {
        attributes["Automatic-Module-Name"] = "org.dalesbred"
    }
}
