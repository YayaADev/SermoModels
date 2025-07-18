# Sermo Models

Auto-generated API models for the Sermo language learning platform, available in both Kotlin and TypeScript/JavaScript.

##  NPM Package

[![npm version](https://badge.fury.io/js/@yayaadev%2Fsermo-models.svg)](https://www.npmjs.com/package/@yayaadev/sermo-models)

```bash
npm install @yayaadev/sermo-models
```

##  Usage

### TypeScript/JavaScript

```typescript
import { 
  TranscriptionRequest, 
  TranscriptionResponse, 
  SynthesisRequest, 
  SynthesisResponse,
  ApiError,
  ErrorResponse 
} from '@yayaadev/sermo-models';

// Create a transcription request
const request: TranscriptionRequest = {
  audio: "base64-encoded-audio-data",
  language: "ar-EG",
  context: "daily conversation practice"
};

// Use JSON conversion utilities
import { TranscriptionRequestToJSON, TranscriptionResponseFromJSON } from '@yayaadev/sermo-models';

const jsonData = TranscriptionRequestToJSON(request);
const response = TranscriptionResponseFromJSON(apiResponse);
```

### Kotlin

```kotlin
import com.sermo.models.*

val request = TranscriptionRequest(
    audio = "base64-encoded-audio-data",
    language = "ar-EG",
    context = "daily conversation practice"
)
```

##  Development

### Building Kotlin Models

```bash
# Generate Kotlin models from OpenAPI spec
gradle openApiGenerate

# Build the entire project
gradle build

# Clean generated files
gradle clean
```

### Building NPM Package

```bash
# Generate TypeScript models and build npm package
npm run build

# Generate TypeScript models only
gradle generateTypeScript

# Compile TypeScript to JavaScript
npm run compile

# Clean generated files
npm run clean
```

##  Project Structure

```
SermoModels/
├── src/main/resources/openapi/
│   └── sermo-api.json              # OpenAPI specification (source of truth)
├── build/generated/
│   ├── openapi/                    # Generated Kotlin models
│   └── typescript/                 # Generated TypeScript models
├── dist/                           # Compiled npm package
├── package.json                    # NPM package configuration
├── build.gradle.kts               # Gradle build configuration
└── .github/workflows/
    ├── build-and-push.yml         # Docker image publishing
    └── publish-npm.yml            # NPM package publishing
```

##  Available Models

All models are generated from the OpenAPI specification and include:

### Core Models
- **TranscriptionRequest** - Speech-to-text request with audio data and language settings
- **TranscriptionResponse** - Speech transcription results with confidence scores
- **SynthesisRequest** - Text-to-speech request with voice and audio settings  
- **SynthesisResponse** - Generated audio data and metadata
- **ApiError** - Standard error structure with code, message, and details
- **ErrorResponse** - API error response wrapper

### Features
- **Type Safety** - Full TypeScript definitions and Kotlin data classes
- **JSON Serialization** - Built-in JSON conversion utilities
- **Validation** - Runtime type checking and validation functions
- **Documentation** - Auto-generated from OpenAPI comments

##  Automatic Updates

The models are automatically updated when:
1. The OpenAPI spec (`sermo-api.json`) changes
2. Changes are pushed to the main branch
3. GitHub Actions workflows regenerate and publish new versions

### Manual Publishing

```bash
# Increment version in package.json first
npm version patch  # or minor, major

# Build and publish
npm run build
npm publish --access public
```

## 🛠 Configuration

### Gradle Configuration
The Kotlin generation is configured in `build.gradle.kts` with:
- **Generator**: `kotlin`
- **Library**: `jvm-ktor` with `kotlinx_serialization`
- **Output**: `build/generated/openapi/`

### TypeScript Configuration  
The TypeScript generation uses:
- **Generator**: `typescript-fetch`
- **Target**: ES2020 with CommonJS modules
- **Output**: `build/generated/typescript/` → `dist/`

##  Requirements

### Development
- **Java 21+** for Gradle builds
- **Node.js 18+** for npm package builds
- **Gradle** for Kotlin model generation

### Runtime
- **TypeScript/JavaScript**: No runtime dependencies
- **Kotlin**: `kotlinx-serialization-json`
