plugins {
    java
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
}

/* Project Properties */
val modGroup    = project.property("mod_group")     as String
val modVersion  = project.property("mod_version")   as String

group = modGroup
version = modVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

spotless {
    java {
        palantirJavaFormat()
    }
}

repositories {
    mavenCentral()
    maven("https://maven.hytale.com/release")
}

dependencies {
    compileOnly(libs.hytale)

    compileOnly(libs.luckperms.api)
}

tasks {
    processResources {
        inputs.property("version", version)
        filteringCharset = "UTF-8"

        filesMatching("manifest.json") {
            expand(
                "version" to version
            )
        }
    }

    compileJava {
        dependsOn(spotlessApply)
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}
