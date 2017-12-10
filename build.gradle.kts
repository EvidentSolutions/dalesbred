import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

buildscript {
    val kotlinVersion by extra("1.2.0")
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.bmuschko:gradle-nexus-plugin:2.3.1")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.11.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    id("org.asciidoctor.convert") version "1.5.7"
    id("pl.allegro.tech.build.axion-release") version "1.8.1"
}

apply {
    plugin("io.codearte.nexus-staging")
}

val springVersion by extra("5.0.2.RELEASE")
val junitVersion by extra("4.12")
val jetbrainsAnnotationsVersion by extra("15.0")
val hsqldbVersion by extra("2.4.0")
val logbackVersion by extra("1.2.3")

scmVersion {
    localOnly = true
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
}

configure(allprojects) {
    apply {
        plugin("base")
    }
    group = "org.dalesbred"

    project.version = closureOf<String> { scmVersion.version }

    repositories {
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

val asciidoctor: AsciidoctorTask by tasks

asciidoctor.sourceDir = file("src/asciidoc")
asciidoctor.attributes = mapOf("revnumber" to project.version.toString())

task("publish") {
    description = "Publishes both the artifacts and the website"

    dependsOn(":website:publishGhPages",
            ":dalesbred:uploadArchives",
            ":dalesbred-junit:uploadArchives")
}

configure(listOf(project(":dalesbred"), project(":dalesbred-junit"))) {
    apply {
        plugin("java")
        plugin("com.bmuschko.nexus")
        from("${rootProject.projectDir}/gradle/publish-maven.gradle")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<Javadoc> {
        val opts = options as StandardJavadocDocletOptions

        opts.memberLevel = org.gradle.external.javadoc.JavadocMemberLevel.PROTECTED
        opts.header = project.name

        opts.links("http://docs.oracle.com/javase/8/docs/api/",
                "http://docs.spring.io/spring/docs/current/javadoc-api/",
                "http://www.joda.org/joda-time/apidocs/")
        opts.addStringOption("Xdoclint:none", "-quiet")
    }
}

