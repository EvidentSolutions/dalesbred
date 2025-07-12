plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.asciidoctor.gradlePlugin)
    implementation(libs.vanniktechMavenPublish.gradlePlugin)
}
