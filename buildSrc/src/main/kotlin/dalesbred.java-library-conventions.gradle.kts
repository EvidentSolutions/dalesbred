import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dalesbred.common-conventions")
    id("io.spring.dependency-management")
    `java-library`
    `signing`
    `maven-publish`
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "8"
    targetCompatibility = "8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
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
        dependency("com.oracle.database.jdbc:ojdbc8:21.3.0.0")
        dependency("junit:junit:4.13.2")
        dependency("ch.qos.logback:logback-core:$logbackVersion")
        dependency("ch.qos.logback:logback-classic:$logbackVersion")
        dependency("javax.inject:javax.inject:1")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://dalesbred.org/")
                inceptionYear.set("2012")
                packaging = "jar"

                organization {
                    name.set("Evident Solutions")
                    url.set("https://www.evident.fi")
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/mit-license.php")
                        distribution.set("repo")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/EvidentSolutions/dalesbred.git")
                    developerConnection.set("scm:git:git@github.com:EvidentSolutions/dalesbred.git")
                    url.set("https://github.com/EvidentSolutions/dalesbred")
                }

                developers {
                    developer {
                        id.set("komu")
                        name.set("Juha Komulainen")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/EvidentSolutions/dalesbred/issues")
                }
            }
        }
    }

    if (hasProperty("sonatypeUsername") || System.getenv("MAVEN_USERNAME") != null) {
        repositories {
            maven {
                name = "sonatype"

                url = if (version.toString().endsWith("-SNAPSHOT"))
                    uri("https://oss.sonatype.org/content/repositories/snapshots/")
                else
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = System.getenv("MAVEN_USERNAME") ?: (property("sonatypeUsername") as String)
                    password = System.getenv("MAVEN_PASSWORD") ?: (property("sonatypePassword") as String)
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])

    val signingKey = System.getenv("SIGNING_KEY")
    if (signingKey != null) {
        val signingPassword = System.getenv("SIGNING_PASSWORD") ?: error("Environment variable SIGNING_PASSWORD is missing")
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
