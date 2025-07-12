rootProject.name = "dalesbred-root"
include(":dalesbred")
include(":website")

plugins {
    id("com.gradle.enterprise") version("3.7.2")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
