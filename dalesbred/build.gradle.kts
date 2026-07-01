import org.gradle.process.CommandLineArgumentProvider

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
    "kotlinImplementation"(kotlin("stdlib"))
    "annotationsImplementation"(libs.jetbrains.annotations)

    "postgresqlImplementation"(libs.jdbc.postgresql)
    "oracleImplementation"(libs.jdbc.oracle)

    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.mariadb)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.jdbc.hsqldb)
    testImplementation(libs.jdbc.h2)
    testImplementation(libs.jdbc.mysql)
    testImplementation(libs.jdbc.mariadb)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.log4j.core)
    testImplementation(libs.log4j.slf4j)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.compileJava {
    // DatabaseExtensions.kt is compiled by kotlinc into a separate output directory, so javac
    // doesn't see org.dalesbred.integration.kotlin as populated when compiling module-info.java.
    // Patch the kotlin output in so the module compiler treats it as part of this module.
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        listOf("--patch-module", "org.dalesbred=${tasks.compileKotlin.get().destinationDirectory.get().asFile}")
    })
}
