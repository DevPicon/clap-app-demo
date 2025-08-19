# Guía de Migración a Kotlin Multiplatform

Esta guía explica cómo se migró el proyecto Clap App de Android puro a Kotlin Multiplatform (KMP).

## 📋 Resumen de la Migración

### Antes (Android Puro)

```
clap-app-demo/
├── app/
│   ├── src/main/java/
│   │   ├── MainActivity.kt
│   │   ├── SoundPlayer.kt
│   │   └── ui/compose/
│   └── build.gradle.kts
└── build.gradle.kts
```

### Después (Kotlin Multiplatform)

```
clap-app-demo/
├── app/                    # Módulo Android
├── shared/                # Módulo compartido KMP
│   ├── src/
│   │   ├── commonMain/    # Código común
│   │   ├── androidMain/   # Implementación Android
│   │   └── iosMain/       # Implementación iOS
│   └── build.gradle.kts
├── iosApp/                # Proyecto iOS
└── build.gradle.kts
```

## 🔄 Pasos de la Migración

### 1. Configuración del Proyecto Raíz

**Archivo: `build.gradle.kts` (raíz)**

```kotlin
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.multiplatform") version "1.9.23" apply false  // ✅ Nuevo
    id("com.android.library") version "8.3.0" apply false                  // ✅ Nuevo
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}
```

**Archivo: `settings.gradle.kts`**

```kotlin
rootProject.name = "clap-app-demo"
include(":app")
include(":shared")  // ✅ Nuevo módulo
```

### 2. Configuración de Gradle Properties

**Archivo: `gradle.properties`**

```properties
# KMP Configuration
kotlin.mpp.androidGradlePluginCompatibility.nowarn=true
kotlin.mpp.applyDefaultHierarchyTemplate=false
android.suppressUnsupportedCompileSdk=35
```

### 3. Creación del Módulo Compartido

**Archivo: `shared/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "pe.devpicon.clapapp.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.jetbrains.kotlinx.coroutines.android)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
```

### 4. Migración del Código

#### 4.1 Interfaz Común

**Archivo: `shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/SoundPlayer.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.StateFlow

interface SoundPlayer {
    fun playClapSound()
    fun release()
    val isPlaying: StateFlow<Boolean>
}

expect class PlatformSoundPlayer() : SoundPlayer
```

#### 4.2 Implementación Android

**Archivo: `shared/src/androidMain/kotlin/pe/devpicon/clapapp/shared/PlatformSoundPlayer.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class PlatformSoundPlayer : SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)

    override val isPlaying: StateFlow<Boolean> = _isPlaying

    fun initialize(context: Context) {
        // Implementación específica de Android
    }

    override fun playClapSound() {
        // Implementación específica de Android
    }

    override fun release() {
        // Implementación específica de Android
    }
}
```

#### 4.3 Implementación iOS

**Archivo: `shared/src/iosMain/kotlin/pe/devpicon/clapapp/shared/PlatformSoundPlayer.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class PlatformSoundPlayer : SoundPlayer {
    private val _isPlaying = MutableStateFlow(false)

    override val isPlaying: StateFlow<Boolean> = _isPlaying

    override fun playClapSound() {
        // Implementación específica de iOS
    }

    override fun release() {
        // Implementación específica de iOS
    }
}
```

#### 4.4 ViewModel Compartido

**Archivo: `shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/ClapViewModel.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.StateFlow

expect class ClapViewModel() {
    val clapCount: StateFlow<Int>
    val isPlaying: StateFlow<Boolean>
    fun onClapClick()
    fun initialize()
    fun release()
}
```

### 5. Actualización del Módulo Android

**Archivo: `app/build.gradle.kts`**

```kotlin
dependencies {
    implementation(project(":shared"))  // ✅ Nueva dependencia
    // ... resto de dependencias
}
```

**Archivo: `app/src/main/java/pe/devpicon/android/clapapp/MainActivity.kt`**

```kotlin
import pe.devpicon.clapapp.shared.ClapViewModel  // ✅ Import del módulo shared

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ClapViewModel  // ✅ Usa ViewModel compartido

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ClapViewModel()
        viewModel.initialize(this)  // ✅ Inicialización específica de Android

        setContent {
            MainScreen(
                onLaunchClick = {
                    viewModel.onClapClick()  // ✅ Usa lógica compartida
                }
            )
        }
    }
}
```

### 6. Configuración iOS

**Archivo: `iosApp/iosApp/ContentView.swift`**

```swift
import SwiftUI
import shared  // ✅ Import del framework compartido

struct ContentView: View {
    @StateObject private var viewModel = ClapViewModel()  // ✅ Usa ViewModel compartido

    var body: some View {
        VStack {
            Button(action: {
                viewModel.onClapClick()  // ✅ Usa lógica compartida
            }) {
                Image(systemName: "hand.clap.fill")
            }
        }
        .onAppear {
            viewModel.initialize()  // ✅ Inicialización
        }
    }
}
```

## 🛠️ Comandos de Construcción

### Construir Módulo Compartido

```bash
./gradlew :shared:build
```

### Construir Aplicación Android

```bash
./gradlew :app:assembleDebug
```

### Construir Framework iOS

```bash
./gradlew :shared:linkReleaseFrameworkIosArm64
```

### Construir Todo

```bash
./gradlew build
```

### Usar Script de Construcción

```bash
./build.sh android    # Solo Android
./build.sh ios        # Solo iOS
./build.sh all        # Todo
./build.sh clean      # Limpiar
```

## 📊 Beneficios Obtenidos

### ✅ Código Compartido

- **SoundPlayer**: Interfaz común con implementaciones específicas
- **ClapViewModel**: Lógica de negocio compartida
- **Estado**: StateFlow compartido entre plataformas

### ✅ Mantenimiento

- Un solo lugar para cambios en la lógica de negocio
- Consistencia entre Android e iOS
- Menos duplicación de código

### ✅ Arquitectura

- Separación clara entre código común y específico
- Patrón expect/actual para implementaciones de plataforma
- ViewModel compartido con StateFlow

## 🔧 Solución de Problemas

### Error de Compilación JVM Target

```kotlin
// En shared/build.gradle.kts
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}
```

### Warnings de expect/actual

```properties
# En gradle.properties
kotlin.mpp.androidGradlePluginCompatibility.nowarn=true
kotlin.mpp.applyDefaultHierarchyTemplate=false
```

### Compatibilidad Android Gradle Plugin

```properties
# En gradle.properties
android.suppressUnsupportedCompileSdk=35
```

## 🚀 Próximos Pasos

1. **Implementar audio nativo para iOS** usando AVAudioPlayer
2. **Agregar tests** para el código compartido
3. **Implementar Compose Multiplatform** para UI compartida
4. **Agregar más funcionalidades** compartidas
5. **Configurar CI/CD** para ambas plataformas

## 📚 Recursos Adicionales

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/lp/mobile/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
