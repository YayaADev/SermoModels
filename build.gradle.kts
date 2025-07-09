plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.openapi.generator") version "7.2.0"
    `maven-publish`
}

group = "com.sermo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

kotlin {
    jvmToolchain(21)
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi/sermo-api.json")
    outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
    packageName.set("com.sermo.models")
    modelPackage.set("com.sermo.models")
    apiPackage.set("com.sermo.models")

    configOptions.set(mapOf(
        "serializationLibrary" to "kotlinx_serialization",
        "library" to "jvm-ktor",
        "useCoroutines" to "true"
    ))

    // Only generate models, not client code
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to "false",
        "supportingFiles" to "false"
    ))
}

// Add generated sources to compilation
sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin")
        }
    }
}

// Ensure OpenAPI generation happens before compilation
tasks.compileKotlin {
    dependsOn("openApiGenerate")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}