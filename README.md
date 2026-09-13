# IDA Pro Mobile - Android Reverse Engineering Toolkit

A mobile reverse engineering toolkit inspired by IDA Pro, built with Jetpack Compose and C++ for binary analysis on Android devices.

## Features

- **Binary File Support**: Load and analyze various binary formats (ELF, PE, Mach-O)
- **Disassembly Viewer**: Interactive disassembly with syntax highlighting for x86, x86-64, and ARM architectures
- **Hexadecimal Viewer**: Raw binary data inspection with search capabilities
- **Function Detection**: Automatic detection and listing of functions in binary files
- **Annotation System**: Add comments and notes to assembly instructions
- **Material Design 3**: Modern, professional UI following Material Design principles
- **Cross-Platform**: Native C++ backend with Kotlin frontend for optimal performance

## Architecture

### Frontend (Kotlin + Jetpack Compose)
- **UI**: Modern Material Design 3 interface
- **Navigation**: Tab-based navigation between different analysis views
- **State Management**: ViewModel pattern with StateFlow for reactive UI updates
- **Database**: Room database for local data persistence

### Backend (C++ + NDK)
- **Binary Analysis**: Native C++ engine for binary parsing and analysis
- **Disassembly**: Custom disassembly engine supporting multiple architectures
- **Function Detection**: Pattern-based and symbol table-based function detection
- **JNI Bridge**: Seamless integration between Kotlin and C++ components

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android NDK 25.1.8937393
- JDK 17
- Minimum Android API level 24 (Android 7.0)

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/ida-pro-mobile.git
   cd ida-pro-mobile
   ```

2. **Install NDK**
   ```bash
   # Through Android Studio SDK Manager or command line
   sdkmanager "ndk;25.1.8937393"
   ```

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

## Usage

### Loading Binary Files

1. Open the app and navigate to the "Files" tab
2. Tap "Choose File" to select a binary file from your device
3. The app will analyze the file and extract basic information
4. Select the file from the list to begin analysis

### Disassembly Analysis

1. Switch to the "Disassembly" tab with a file selected
2. View assembly instructions with syntax highlighting
3. Tap on instructions to add comments and annotations
4. Navigate through the code using the address view

### Hex View

1. Use the "Hex View" tab to inspect raw binary data
2. Search for hex patterns or ASCII strings
3. View both hex and ASCII representations side by side

### Function Analysis

1. The "Functions" tab shows all detected functions
2. Filter functions by name, address, or type
3. View function statistics and signatures
4. Tap functions to navigate to their location in disassembly

## Supported File Formats

- **ELF**: Linux/Unix executables and shared libraries
- **PE**: Windows executables (.exe, .dll)
- **Mach-O**: macOS executables and dylibs
- **Raw Binary**: Generic binary files

## Supported Architectures

- **x86**: 32-bit Intel architecture
- **x86-64**: 64-bit Intel architecture  
- **ARM**: 32-bit ARM architecture
- **ARM64**: 64-bit ARM architecture (basic support)

## Development

### Project Structure

```
ida-pro-mobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/idapro/mobile/
│   │   │   │   ├── MainActivity.kt                    # Main activity with Compose setup
│   │   │   │   ├── IdaProMobileApplication.kt        # Application class with dependency injection
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/                      # Main UI screens
│   │   │   │   │   │   ├── MainScreen.kt             # Tab navigation and main layout
│   │   │   │   │   │   ├── FileUploadScreen.kt       # Binary file upload interface
│   │   │   │   │   │   ├── DisassemblyScreen.kt      # Disassembly viewer with syntax highlighting
│   │   │   │   │   │   ├── HexViewerScreen.kt        # Hex viewer with search capabilities
│   │   │   │   │   │   └── FunctionListScreen.kt     # Function detection and listing
│   │   │   │   │   ├── components/                   # Reusable UI components
│   │   │   │   │   │   ├── BinaryFileCard.kt         # File display card
│   │   │   │   │   │   ├── DisassemblyView.kt        # Assembly instruction rendering
│   │   │   │   │   │   ├── HexGrid.kt                # Hex data grid component
│   │   │   │   │   │   ├── FunctionItem.kt           # Function list item
│   │   │   │   │   │   └── AnnotationDialog.kt       # Comment annotation dialog
│   │   │   │   │   └── theme/                        # Material Design 3 theming
│   │   │   │   │       ├── Color.kt                  # Color palette
│   │   │   │   │       ├── Theme.kt                  # Theme configuration
│   │   │   │   │       └── Type.kt                   # Typography system
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/                     # Room database layer
│   │   │   │   │   │   ├── AppDatabase.kt            # Database configuration
│   │   │   │   │   │   ├── entities/                 # Database entities
│   │   │   │   │   │   │   ├── BinaryFileEntity.kt   # Binary file metadata
│   │   │   │   │   │   │   └── AnnotationEntity.kt   # User annotations
│   │   │   │   │   │   └── dao/                      # Data access objects
│   │   │   │   │   │       ├── BinaryFileDao.kt      # File operations
│   │   │   │   │   │       └── AnnotationDao.kt      # Annotation operations
│   │   │   │   │   ├── model/                        # Data models
│   │   │   │   │   │   ├── BinaryFile.kt             # Binary file representation
│   │   │   │   │   │   ├── DisassemblyInstruction.kt # Assembly instruction model
│   │   │   │   │   │   ├── Function.kt               # Function metadata
│   │   │   │   │   │   └── Annotation.kt             # User annotation model
│   │   │   │   │   └── repository/                   # Repository pattern implementation
│   │   │   │   │       └── BinaryAnalysisRepository.kt # Data management layer
│   │   │   │   ├── viewmodel/                        # ViewModel layer
│   │   │   │   │   ├── MainViewModel.kt              # Main screen state management
│   │   │   │   │   ├── FileUploadViewModel.kt        # File upload logic
│   │   │   │   │   ├── DisassemblyViewModel.kt       # Disassembly state management
│   │   │   │   │   ├── HexViewerViewModel.kt         # Hex viewer state
│   │   │   │   │   └── FunctionListViewModel.kt      # Function list state
│   │   │   │   ├── native/                           # JNI bridge layer
│   │   │   │   │   ├── NativeBinaryAnalyzer.kt       # JNI interface
│   │   │   │   │   ├── DisassemblyData.kt            # Data transfer objects
│   │   │   │   │   └── FunctionData.kt               # Function data DTOs
│   │   │   │   └── utils/                            # Utility classes
│   │   │   │       ├── BinaryUtils.kt                # Binary analysis utilities
│   │   │   │       ├── FileUtils.kt                  # File handling utilities
│   │   │   │       └── Constants.kt                  # App constants
│   │   │   ├── cpp/                                  # Native C++ backend
│   │   │   │   ├── binary_analyzer.cpp               # Main binary analysis engine
│   │   │   │   ├── disassembler.cpp                  # Disassembly engine
│   │   │   │   ├── function_detector.cpp             # Function detection algorithms
│   │   │   │   ├── jni_bridge.cpp                    # JNI integration layer
│   │   │   │   └── CMakeLists.txt                    # C++ build configuration
│   │   │   └── res/                                  # Android resources
│   │   │       ├── values/                           # Resource values
│   │   │       │   ├── colors.xml                    # Color definitions
│   │   │       │   ├── strings.xml                   # String resources
│   │   │       │   └── themes.xml                    # XML theme definitions
│   │   │       └── xml/
│   │   │           └── file_paths.xml                # File provider configuration
│   │   ├── test/                                     # Unit tests
│   │   │   └── java/com/idapro/mobile/
│   │   │       ├── BinaryAnalysisTest.kt             # Binary analysis unit tests
│   │   │       ├── viewmodel/                        # ViewModel tests
│   │   │       └── utils/                            # Utility function tests
│   │   └── androidTest/                              # Instrumented tests
│   │       └── java/com/idapro/mobile/
│   │           ├── DatabaseTest.kt                   # Room database tests
│   │           ├── ui/                               # UI integration tests
│   │           └── native/                           # Native code tests
│   ├── build.gradle.kts                             # App module build configuration
│   └── proguard-rules.pro                           # ProGuard obfuscation rules
├── .github/
│   └── workflows/
│       └── android.yml                              # GitHub Actions CI/CD pipeline
├── gradle/
│   └── wrapper/                                     # Gradle wrapper files
├── build.gradle.kts                                 # Root project build configuration
├── settings.gradle.kts                              # Gradle settings
├── gradle.properties                                # Gradle properties and optimizations
└── README.md                                        # Project documentation
```

## Key Technical Features

### 🔧 Native C++ Backend
- **High-Performance Analysis**: Custom C++ engine for binary parsing and disassembly
- **Multi-Architecture Support**: x86, x86-64, ARM, and ARM64 instruction sets
- **Memory Efficient**: Optimized algorithms for mobile devices with limited resources
- **JNI Integration**: Seamless bridge between Java/Kotlin and native code

### 📱 Modern Android Architecture
- **Jetpack Compose**: Fully declarative UI with Material Design 3
- **MVVM Pattern**: Clean separation of concerns with ViewModels and StateFlow
- **Room Database**: Type-safe local storage with automatic schema management
- **Repository Pattern**: Abstraction layer for data access and caching

### 🔍 Advanced Analysis Capabilities
- **Binary Format Support**: ELF, PE, Mach-O, and raw binary files
- **Intelligent Disassembly**: Context-aware instruction decoding with syntax highlighting
- **Function Detection**: Pattern-based and symbol table-based function identification
- **Interactive Annotations**: User-generated comments and notes system

### 🛡️ Security & Performance
- **ProGuard Optimization**: Code obfuscation and size reduction for release builds
- **NDK Integration**: Native development kit for optimal performance
- **Memory Management**: Efficient handling of large binary files
- **Secure Storage**: Encrypted local database for sensitive analysis data

## Testing Strategy

### Unit Tests (`app/src/test/`)
- **BinaryAnalysisTest.kt**: Tests for binary file analysis utilities including architecture detection, file type identification, hash calculation, and string extraction
- **ViewModel Tests**: Comprehensive testing of all ViewModels with mock repositories
- **Utility Tests**: Testing of binary utilities, file handling, and helper functions

### Instrumented Tests (`app/src/androidTest/`)
- **DatabaseTest.kt**: Room database integration tests including CRUD operations, foreign key constraints, and cascade deletions
- **UI Tests**: Jetpack Compose UI component testing with user interaction simulation
- **Native Integration Tests**: JNI bridge testing for C++ backend integration

### Continuous Integration
The GitHub Actions workflow includes:
- **Automated Testing**: Unit tests, instrumented tests, and lint checks
- **Code Quality**: ktlint formatting and detekt static analysis
- **Security Scanning**: Trivy vulnerability scanner integration
- **APK Building**: Debug and release APK generation with signing

## CI/CD Pipeline Features

### Automated Workflows
- **Multi-stage Pipeline**: Test → Build → Security Scan → Quality Check
- **Parallel Execution**: Independent jobs for faster feedback
- **Artifact Storage**: APK files and test reports automatically uploaded
- **Environment Setup**: Automatic NDK installation and caching

### Code Quality Gates
- **Kotlin Code Style**: Automated ktlint formatting verification
- **Static Analysis**: detekt rules for code quality and best practices
- **Vulnerability Scanning**: Trivy security scanner for dependency vulnerabilities
- **Test Coverage**: Unit and integration test execution with reporting

## Contributing

### Development Setup
1. Install Android Studio Arctic Fox or later
2. Install Android NDK 25.1.8937393 through SDK Manager
3. Clone repository and open in Android Studio
4. Sync project to download dependencies

### Code Style Guidelines
- Follow Kotlin coding conventions
- Use ktlint for automatic formatting
- Maintain comprehensive test coverage
- Document public APIs with KDoc
- Follow Material Design 3 principles for UI

### Pull Request Process
1. Create feature branch from `develop`
2. Implement changes with tests
3. Run local tests: `./gradlew test lint`
4. Submit PR targeting `develop` branch
5. Ensure CI pipeline passes
6. Request code review from maintainers

## Deployment

### GitHub Release Process
1. **Tag Creation**: Create version tag (e.g., `v1.0.0`)
2. **Automated Build**: GitHub Actions builds and signs release APK
3. **Release Notes**: Generate changelog from commit history
4. **Asset Upload**: Signed APK attached to GitHub release

### APK Signing Configuration
```bash
# Generate signing key
keytool -genkey -v -keystore release-key.keystore -alias ida-pro-mobile \
        -keyalg RSA -keysize 2048 -validity 10000

# Configure GitHub Secrets
SIGNING_KEY        # Base64 encoded keystore file
ALIAS              # Keystore alias name
KEY_STORE_PASSWORD # Keystore password
KEY_PASSWORD       # Key password
```

### Distribution Options
- **GitHub Releases**: Direct APK download from releases page
- **F-Droid**: Open source app store distribution
- **Internal Testing**: TestFlight-style internal distribution
- **Play Store**: Google Play Store publication (with developer account)

## Architecture Deep Dive

### Data Flow Architecture
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  Domain Layer    │    │   Data Layer    │
│  (Compose)      │◄──►│  (ViewModels)    │◄──►│ (Repository)    │
│                 │    │                  │    │                 │
│ • Screens       │    │ • State Mgmt     │    │ • Room DB       │
│ • Components    │    │ • Business Logic │    │ • Native JNI    │
│ • Navigation    │    │ • Data Transform │    │ • File System   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Native Integration Flow
```
┌──────────────┐    ┌─────────────┐    ┌──────────────────┐
│   Kotlin     │    │     JNI     │    │      C++         │
│   Frontend   │◄──►│   Bridge    │◄──►│    Backend       │
│              │    │             │    │                  │
│ • File Load  │    │ • Data      │    │ • Binary Parser  │
│ • UI Updates │    │   Transfer  │    │ • Disassembler   │
│ • User Input │    │ • Error     │    │ • Function       │
│              │    │   Handling  │    │   Detection      │
└──────────────┘    └─────────────┘    └──────────────────┘
```

## Security Considerations

### Data Protection
- **Local Storage Encryption**: SQLCipher integration for database encryption
- **Secure File Handling**: Temporary file cleanup and secure deletion
- **Memory Protection**: Sensitive data clearing after analysis
- **Permission Model**: Minimal required permissions for file access

### Binary Analysis Safety
- **Sandbox Execution**: Isolated analysis environment
- **Resource Limits**: Memory and CPU usage constraints
- **Input Validation**: Comprehensive file format validation
- **Error Handling**: Graceful handling of malformed binaries

## Performance Optimizations

### Mobile-Specific Optimizations
- **Lazy Loading**: On-demand binary parsing and disassembly
- **Memory Management**: Efficient handling of large files with pagination
- **Background Processing**: Coroutines for non-blocking analysis
- **Caching Strategy**: Intelligent caching of analysis results

### Native Performance
- **SIMD Instructions**: Vectorized operations for pattern matching
- **Multi-threading**: Parallel analysis for multi-core devices
- **Memory Mapping**: Efficient large file handling
- **Algorithmic Optimization**: Custom algorithms optimized for mobile CPUs

## License

MIT License - See [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by IDA Pro's powerful reverse engineering capabilities
- Built with modern Android development best practices
- Leverages the power of Jetpack Compose and Material Design 3
- C++ backend designed for mobile performance constraints

