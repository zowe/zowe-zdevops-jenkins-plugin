import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = findProperty("kotlinVersion")

plugins {
    kotlin("jvm") version ("1.5.10")
    kotlin("kapt") version ("1.5.10")

    id("org.jenkins-ci.jpi") version ("0.43.0")
}

repositories {
    mavenCentral()
    maven {
        url = uri("http://10.221.23.186:8082/repository/internal/")
        isAllowInsecureProtocol = true
        credentials {
            username = "admin"
            password = "password123"
        }
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
}

dependencies {
    kotlin("stdlib", kotlinVersion as String)

    // Retrofit and r2z is used to run z/OSMF REST API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("eu.ibagroup:r2z:1.2.0-rc.3")

    // Jenkins development related plugins
    implementation("org.jenkins-ci.plugins.workflow:workflow-step-api:2.23")
    implementation("org.jenkins-ci.plugins.workflow:workflow-aggregator:581.v0c46fa_697ffd")

    // SezPoz is used to process @hudson.Extension and other annotations
    kapt("net.java.sezpoz:sezpoz:1.13")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

kapt {
    correctErrorTypes = true
}

jenkinsPlugin {
    jenkinsVersion.set("2.319.2")
    displayName = "zOS DevOps Plugin"
    shortName = "z-devops"
    gitHubUrl = "https://github.com/===TBD==="  // TODO add real repo

    compatibleSinceVersion = jenkinsVersion.get()
    fileExtension = "jpi"
    pluginFirstClassLoader = true
}

tasks.withType(KotlinCompile::class.java).all {
    dependsOn("localizer")
}
