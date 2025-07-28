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

// Existing HTTP API Kotlin generation
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi/main-api.json")
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

// WebSocket models Kotlin generation
val generateWebSocketModels by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi/websocket-models.json")
    outputDir.set("${layout.buildDirectory.get()}/generated/websocket")
    packageName.set("com.sermo.websocket.models")
    modelPackage.set("com.sermo.websocket.models")
    apiPackage.set("com.sermo.websocket.models")
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

// Existing HTTP API TypeScript generation
val generateTypeScript by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("typescript-fetch")
    inputSpec.set("$projectDir/src/main/resources/openapi/main-api.json")
    outputDir.set("${layout.buildDirectory.get()}/generated/typescript")
    packageName.set("sermo-models")

    configOptions.set(mapOf(
        "npmName" to "sermo-models",
        "npmVersion" to project.version.toString(),
        "typescriptThreePlus" to "true",
        "supportsES6" to "true",
        "enumPropertyNaming" to "PascalCase"
    ))
    // Only generate models, not API client
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to "false",
        "supportingFiles" to "runtime.ts",
        "modelTests" to "false",
        "modelDocs" to "false"
    ))
}

// WebSocket models TypeScript generation
val generateWebSocketTypeScript by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("typescript-fetch")
    inputSpec.set("$projectDir/src/main/resources/openapi/websocket-models.json")
    outputDir.set("${layout.buildDirectory.get()}/generated/typescript-websocket")
    packageName.set("sermo-websocket-models")

    configOptions.set(mapOf(
        "npmName" to "sermo-websocket-models",
        "npmVersion" to project.version.toString(),
        "typescriptThreePlus" to "true",
        "supportsES6" to "true",
        "enumPropertyNaming" to "PascalCase"
    ))
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to "false",
        "supportingFiles" to "runtime.ts",
        "modelTests" to "false",
        "modelDocs" to "false"
    ))
}

// Auto-discover and create combined index file
val createCombinedIndexFile by tasks.registering {
    dependsOn(generateTypeScript, generateWebSocketTypeScript)
    doLast {
        val httpModelsDir = file("${layout.buildDirectory.get()}/generated/typescript/src/models")
        val wsModelsDir = file("${layout.buildDirectory.get()}/generated/typescript-websocket/src/models")
        val indexFile = file("${layout.buildDirectory.get()}/generated/typescript/src/index.ts")
        val wsTargetDir = file("${layout.buildDirectory.get()}/generated/typescript/src/websocket")

        // Create websocket directory in typescript output
        wsTargetDir.mkdirs()

        val exports = mutableListOf<String>()

        // Auto-discover HTTP models
        if (httpModelsDir.exists()) {
            httpModelsDir.listFiles()?.filter { it.extension == "ts" }?.forEach { file ->
                val modelName = file.nameWithoutExtension
                exports.add("export * from './models/$modelName';")
            }
        }

        // Copy WebSocket models to typescript output and create exports
        if (wsModelsDir.exists()) {
            wsModelsDir.listFiles()?.filter { it.extension == "ts" }?.forEach { file ->
                val modelName = file.nameWithoutExtension
                // Copy the file to the websocket subdirectory
                file.copyTo(File(wsTargetDir, file.name), overwrite = true)
                exports.add("export * from './websocket/$modelName';")
            }
        }

        indexFile.writeText("""
// Auto-generated index file for Sermo API models
// Do not edit this file manually

${exports.joinToString("\n")}
        """.trimIndent())
    }
}

// Add generated sources to compilation
sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin")
            srcDir("${layout.buildDirectory.get()}/generated/websocket/src/main/kotlin")
        }
    }
}

// Ensure all generation happens before compilation
tasks.compileKotlin {
    dependsOn("openApiGenerate", "generateWebSocketModels")
}

// Clean task to remove generated files
tasks.clean {
    delete("${layout.buildDirectory.get()}/generated")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}