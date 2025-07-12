import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("dalesbred.common-conventions")
    id("io.spring.dependency-management")
    id("com.vanniktech.maven.publish")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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

dependencyManagement {
    val springVersion = "5.3.10"
    val logbackVersion = "1.2.6"

    dependencies {
        dependency("org.slf4j:slf4j-api:1.7.32")
        dependency("joda-time:joda-time:2.10.12")
        dependency("org.threeten:threetenbp:1.5.1")
        dependency("org.springframework:spring-context:$springVersion")
        dependency("org.springframework:spring-jdbc:$springVersion")
        dependency("org.jetbrains:annotations:22.0.0")

        dependency("org.postgresql:postgresql:42.2.24")
        dependency("org.hsqldb:hsqldb:2.4.0")
        dependency("com.h2database:h2:1.4.200")
        dependency("mysql:mysql-connector-java:8.0.26")
        dependency("org.mariadb.jdbc:mariadb-java-client:3.4.1")
        dependency("com.oracle.database.jdbc:ojdbc8:21.3.0.0")
        dependency("junit:junit:4.13.2")
        dependency("ch.qos.logback:logback-core:$logbackVersion")
        dependency("ch.qos.logback:logback-classic:$logbackVersion")
        dependency("javax.inject:javax.inject:1")
    }
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
