plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.openapi.generator") version "7.2.0"
    id("com.google.protobuf") version "0.9.4"
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
    
    // Protocol Buffers dependencies
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
    implementation("com.google.protobuf:protobuf-java:3.25.1")
}

kotlin {
    jvmToolchain(21)
}

// Protocol Buffers configuration
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

// OpenAPI generation (HTTP models)
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
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to "false",
        "supportingFiles" to "false"
    ))
}

// TypeScript generation for OpenAPI
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
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to "false",
        "supportingFiles" to "runtime.ts",
        "modelTests" to "false",
        "modelDocs" to "false"
    ))
}

// TypeScript generation for Protocol Buffers
val generateProtobufTypeScript by tasks.registering(Exec::class) {
    group = "protobuf"
    description = "Generate TypeScript files from Protocol Buffers"
    
    dependsOn("generateProto")
    
    doFirst {
        file("${layout.buildDirectory.get()}/generated/typescript-protobuf").mkdirs()
    }
    
    commandLine("protoc",
        "--proto_path=src/main/resources/proto",
        "--ts_out=${layout.buildDirectory.get()}/generated/typescript-protobuf",
        "src/main/resources/proto/sermo-protocol.proto"
    )
}

// Create combined TypeScript index file
val createCombinedIndexFile by tasks.registering {
    dependsOn(generateTypeScript, generateProtobufTypeScript)
    doLast {
        val httpModelsDir = file("${layout.buildDirectory.get()}/generated/typescript/src/models")
        val protobufDir = file("${layout.buildDirectory.get()}/generated/typescript-protobuf")
        val indexFile = file("${layout.buildDirectory.get()}/generated/typescript/src/index.ts")
        val protobufTargetDir = file("${layout.buildDirectory.get()}/generated/typescript/src/protobuf")

        // Create protobuf directory
        protobufTargetDir.mkdirs()

        val exports = mutableListOf<String>()

        // Export HTTP models
        if (httpModelsDir.exists()) {
            httpModelsDir.listFiles()?.filter { it.extension == "ts" }?.forEach { file ->
                val modelName = file.nameWithoutExtension
                exports.add("export * from './models/$modelName';")
            }
        }

        // Copy and export Protocol Buffer models
        if (protobufDir.exists()) {
            protobufDir.listFiles()?.filter { it.extension == "ts" }?.forEach { file ->
                val modelName = file.nameWithoutExtension
                file.copyTo(File(protobufTargetDir, file.name), overwrite = true)
                exports.add("export * from './protobuf/$modelName';")
            }
        }

        indexFile.writeText("""
// Auto-generated index file for Sermo models
// HTTP models (OpenAPI) + Real-time models (Protocol Buffers)

${exports.joinToString("\n")}
        """.trimIndent())
    }
}

// Configure source directories
sourceSets {
    main {
        proto {
            srcDir("src/main/resources/proto")
        }
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/source/proto/main/kotlin")
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin")
        }
    }
}

// Ensure generation happens before compilation
tasks.compileKotlin {
    dependsOn("generateProto", "openApiGenerate")
}

// Clean generated files
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