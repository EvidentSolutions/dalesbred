description = "Dalesbred - a database access library"

plugins {
    id("kotlin")
    id("osgi")
}

dependencies {
    compile("org.slf4j:slf4j-api")
    compile("joda-time:joda-time")
    compile("org.threeten:threetenbp")
    compile("org.springframework:spring-context")
    compile("org.springframework:spring-jdbc")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8")
    compile("org.jetbrains:annotations")

    compile("org.postgresql:postgresql")

    testCompile("org.jetbrains.kotlin:kotlin-reflect")
    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.hsqldb:hsqldb")
    testCompile("com.h2database:h2")
    testCompile("mysql:mysql-connector-java")
    testCompile("junit:junit")
    testCompile("org.mockito:mockito-core")
    testCompile("ch.qos.logback:logback-core")
    testCompile("ch.qos.logback:logback-classic")
}
