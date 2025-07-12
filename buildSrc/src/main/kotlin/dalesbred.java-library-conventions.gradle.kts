import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("dalesbred.common-conventions")
    id("com.vanniktech.maven.publish")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}

tasks.withType<Javadoc> {
    val opts = options as StandardJavadocDocletOptions

    opts.memberLevel = JavadocMemberLevel.PROTECTED
    opts.header = project.name

    opts.links("https://docs.oracle.com/javase/8/docs/api/",
        "https://docs.spring.io/spring-framework/docs/current/javadoc-api/",
        "https://www.joda.org/joda-time/apidocs/")
    opts.addStringOption("Xdoclint:html,syntax,reference", "-quiet")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "org.dalesbred",
        artifactId = project.name,
        version = project.findProperty("projectVersion") as String? ?: project.version.toString()
    )

    pom {
        name = provider { project.name }
        description = provider { project.description }
        url = "https://dalesbred.org/"
        inceptionYear = "2012"

        organization {
            name = "Evident Solutions"
            url = "https://www.evident.fi"
        }

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/mit-license.php"
                distribution = "repo"
            }
        }

        scm {
            connection = "scm:git:https://github.com/EvidentSolutions/dalesbred.git"
            developerConnection = "scm:git:git@github.com:EvidentSolutions/dalesbred.git"
            url = "https://github.com/EvidentSolutions/dalesbred"
        }

        developers {
            developer {
                id = "komu"
                name = "Juha Komulainen"
                url = "https://github.com/komu"
            }
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/EvidentSolutions/dalesbred/issues"
        }
    }
}
