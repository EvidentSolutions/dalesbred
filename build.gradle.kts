import com.sun.javafx.scene.CameraHelper.project
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import jdk.nashorn.internal.runtime.Debug.id
import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.sure
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

plugins {
    kotlin("jvm") version "1.2.0" apply false
    id("io.spring.dependency-management") version "1.0.3.RELEASE"
    id("org.asciidoctor.convert") version "1.5.7"
    id("pl.allegro.tech.build.axion-release") version "1.8.1"
    id("com.bmuschko.nexus") version "2.3.1" apply false
    id("io.codearte.nexus-staging") version "0.11.0"
}

scmVersion {
    localOnly = true
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
}

version = scmVersion.version

allprojects {
    apply {
        plugin("base")
        plugin("io.spring.dependency-management")
    }

    group = "org.dalesbred"
    version = rootProject.version

    repositories {
        jcenter()
    }

    configure<DependencyManagementExtension> {
        val kotlinVersion = "1.2.0"
        val springVersion = "5.0.2.RELEASE"
        val logbackVersion = "1.2.3"

        dependencies {
            dependency("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlinVersion")
            dependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            dependency("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

            dependency("org.slf4j:slf4j-api:1.7.25")
            dependency("joda-time:joda-time:2.9.9")
            dependency("org.threeten:threetenbp:1.3.6")
            dependency("org.springframework:spring-context:$springVersion")
            dependency("org.springframework:spring-jdbc:$springVersion")
            dependency("org.jetbrains:annotations:15.0")

            dependency("org.postgresql:postgresql:42.1.4")

            dependency("org.hsqldb:hsqldb:2.4.0")
            dependency("com.h2database:h2:1.4.196")
            dependency("mysql:mysql-connector-java:5.1.45")
            dependency("junit:junit:4.12")
            dependency("org.mockito:mockito-core:2.13.0")
            dependency("ch.qos.logback:logback-core:$logbackVersion")
            dependency("ch.qos.logback:logback-classic:$logbackVersion")
            dependency("javax.inject:javax.inject:1")
        }
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

        opts.links("https://docs.oracle.com/javase/8/docs/api/",
                "https://docs.spring.io/spring/docs/current/javadoc-api/",
                "https://www.joda.org/joda-time/apidocs/")
        opts.addStringOption("Xdoclint:none", "-quiet")
    }
}

