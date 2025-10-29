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

### 4. Migración a Arquitectura con Inyección de Dependencias (Koin)

La migración del código de una app nativa de Android a Kotlin Multiplatform se realizó implementando **Koin** como framework de inyección de dependencias. Esto permite:

- **Separación de responsabilidades**: El ViewModel no se encarga de crear dependencias
- **Testabilidad**: Las dependencias pueden ser mockeadas fácilmente
- **Reutilización de código**: El mismo patrón de DI funciona en Android e iOS
- **Escalabilidad**: Fácil agregar nuevas dependencias sin modificar el código existente

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

expect fun buildPlatformSoundPlayer(): SoundPlayer
expect fun getPlatformModule(): Module
```

**Archivo: `shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/ClapViewModel.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClapViewModel(private val soundPlayer: SoundPlayer) {
    private val _clapCount = MutableStateFlow(0)
    val clapCount: StateFlow<Int> = _clapCount.asStateFlow()

    val isPlaying: StateFlow<Boolean> = soundPlayer.isPlaying

    fun onClapClick() {
        soundPlayer.playClapSound()
        _clapCount.value += 1
    }

    fun initialize() {
        // Platform-specific initialization is handled by SoundPlayer implementations
    }

    fun release() {
        soundPlayer.release()
    }
}
```

**Nota importante**: El `ClapViewModel` recibe `SoundPlayer` como dependencia en el constructor. Koin se encarga de proporcionar esta dependencia automáticamente.

#### 4.2 Configuración de Koin

**Archivo: `shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/KoinSetup.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import org.koin.core.module.Module

expect fun getPlatformModule(): Module
```

El archivo define el contrato que cada plataforma debe implementar. Cada plataforma proporciona su propia implementación con sus módulos específicos.

#### 4.3 Implementación Android

**Archivo: `shared/src/androidMain/kotlin/pe/devpicon/clapapp/shared/ResourceReader.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import android.content.Context

class ResourceReader(val context: Context) {
    fun getRawResourceId(resourceName: String): Int {
        return context.resources.getIdentifier(resourceName, "raw", context.packageName)
    }
}
```

**Archivo: `shared/src/androidMain/kotlin/pe/devpicon/clapapp/shared/PlatformSoundPlayer.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()

class AndroidSoundPlayer(private val resourceReader: ResourceReader) : SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)

    override val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        try {
            val resourceId = resourceReader.getRawResourceId("claps")
            mediaPlayer = MediaPlayer.create(resourceReader.context, resourceId)

            if (mediaPlayer == null) {
                Log.e("PlatformSoundPlayer", "MediaPlayer factory returned null. Check your resource.")
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e("PlatformSoundPlayer", "MediaPlayer error: what $what, extra $extra")
                _isPlaying.value = false
                true
            }

            mediaPlayer?.setOnCompletionListener {
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e("PlatformSoundPlayer", "Error creating MediaPlayer: ${e.message}", e)
            mediaPlayer = null
        }
    }

    override fun playClapSound() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
                player.seekTo(0)
                player.start()
                _isPlaying.value = true
            } catch (e: IllegalStateException) {
                Log.e("PlatformSoundPlayer", "Error playing sound: ${e.message}", e)
                mediaPlayer?.release()
                val resourceId = resourceReader.getRawResourceId("claps")
                mediaPlayer = MediaPlayer.create(resourceReader.context, resourceId)
                mediaPlayer?.start()
                _isPlaying.value = mediaPlayer != null
            }
        } ?: run {
            Log.e("PlatformSoundPlayer", "MediaPlayer is null, attempting to (re)create via factory.")
            val resourceId = resourceReader.getRawResourceId("claps")
            mediaPlayer = MediaPlayer.create(resourceReader.context, resourceId)
            mediaPlayer?.start()
            _isPlaying.value = mediaPlayer != null
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        Log.d("PlatformSoundPlayer", "MediaPlayer released")
    }
}
```

**Archivo: `shared/src/androidMain/kotlin/pe/devpicon/clapapp/shared/AndroidModule.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { ResourceReader(androidContext()) }
    single<SoundPlayer> { AndroidSoundPlayer(get()) }
    single { ClapViewModel(get()) }
}

actual fun getPlatformModule() = androidModule
```

El módulo Android define todas las dependencias necesarias:
- `ResourceReader`: Accede a recursos raw del APK
- `SoundPlayer`: Implementación Android que usa `MediaPlayer`
- `ClapViewModel`: ViewModel que depende de `SoundPlayer`

#### 4.4 Implementación iOS

**Archivo: `shared/src/iosMain/kotlin/pe/devpicon/clapapp/shared/PlatformSoundPlayer.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()

/**
 * Native iOS SoundPlayer implementation.
 *
 * NOTE: Full AVAudioPlayer implementation is pending due to platform interop configuration.
 * This is a stub implementation that allows the framework to compile.
 *
 * TODO: Configure proper AVFoundation linkage in build.gradle.kts
 * TODO: Implement audio playback using AVAudioPlayer
 */
class NativeSoundPlayer : SoundPlayer {
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        println("NativeSoundPlayer initialized (stub implementation)")
    }

    override fun playClapSound() {
        println("NativeSoundPlayer: playClapSound called (stub - no audio yet)")
        _isPlaying.value = true
        // Simulate playback completion after a short delay
        GlobalScope.launch {
            delay(1000)
            _isPlaying.value = false
        }
    }

    override fun release() {
        println("NativeSoundPlayer: release called")
        _isPlaying.value = false
    }
}
```

**Archivo: `shared/src/iosMain/kotlin/pe/devpicon/clapapp/shared/IosModule.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import org.koin.dsl.module

val iosModule = module {
    single<SoundPlayer> { NativeSoundPlayer() }
    single { ClapViewModel(get()) }
}

actual fun getPlatformModule() = iosModule
```

El módulo iOS es más simple por ahora, ya que la implementación de audio es un stub. En el futuro, cuando se configure AVFoundation correctamente, se reemplazará `NativeSoundPlayer` con una implementación real usando `AVAudioPlayer`.

#### 4.5 Inicialización de la Aplicación (CRÍTICO)

**Archivo: `app/src/main/java/pe/devpicon/android/clapapp/ClapApplication.kt`**

```kotlin
package pe.devpicon.android.clapapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pe.devpicon.clapapp.shared.getPlatformModule

class ClapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ClapApplication)
            modules(getPlatformModule())
        }
    }
}
```

**Archivo: `app/src/main/AndroidManifest.xml`**

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".ClapApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <!-- Activities aquí -->
    </application>

</manifest>
```

**Puntos importantes**:
- `ClapApplication` hereda de `Application` y se ejecuta una sola vez
- En `onCreate()` se llama a `startKoin` para inicializar el contenedor de DI
- Se proporciona el contexto de Android y los módulos de Koin
- El atributo `android:name=".ClapApplication"` en el manifest vincula la clase

**Para iOS**, el inicialización es diferente:

**Archivo: `shared/src/iosMain/kotlin/pe/devpicon/clapapp/shared/KoinHelper.kt`**

```kotlin
package pe.devpicon.clapapp.shared

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

/**
 * Helper class for iOS to interact with Koin dependency injection.
 * iOS SwiftUI code can use this to initialize Koin and retrieve dependencies.
 */
class KoinHelper : KoinComponent {

    companion object {
        /**
         * Initialize Koin with iOS platform module.
         * Call this once in your iOS app initialization (e.g., in App init or AppDelegate).
         */
        fun doInitKoin() {
            startKoin {
                modules(getPlatformModule())
            }
        }
    }

    /**
     * Get ClapViewModel instance from Koin.
     * Usage from Swift: KoinHelper().getClapViewModel()
     */
    fun getClapViewModel(): ClapViewModel = get()

    /**
     * Get SoundPlayer instance from Koin.
     * Usage from Swift: KoinHelper().getSoundPlayer()
     */
    fun getSoundPlayer(): SoundPlayer = get()
}
```

### 5. Actualización del Módulo Android

**Archivo: `app/build.gradle.kts`**

```kotlin
dependencies {
    implementation(project(":shared"))  // ✅ Dependencia del módulo compartido

    // Koin for dependency injection
    implementation(libs.koin.android)

    // ... resto de dependencias
}
```

**Archivo: `app/src/main/java/pe/devpicon/android/clapapp/MainActivity.kt`**

```kotlin
package pe.devpicon.android.clapapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.android.inject
import pe.devpicon.android.clapapp.ui.compose.MainScreen
import pe.devpicon.clapapp.shared.ClapViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ClapViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(
                onLaunchClick = { viewModel.onClapClick() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}
```

**Explicación del patrón DI**:
- `by inject()` es una propiedad delegada que obtiene la instancia de Koin
- No es necesario llamar a `ClapViewModel()` manualmente
- Koin automáticamente resuelve la cadena de dependencias (ClapViewModel → SoundPlayer → ResourceReader)
- La instancia es un singleton (single en el módulo), por lo que es la misma durante toda la vida de la app

### 6. Configuración iOS

**Archivo: `iosApp/iosApp/ContentView.swift`**

```swift
import SwiftUI
import shared

struct ContentView: View {
    @State private var clapCount = 0
    @State private var isPlaying = false
    private let viewModel: ClapViewModel

    init() {
        // Get ViewModel from Koin dependency injection
        self.viewModel = KoinHelper().getClapViewModel()
    }

    var body: some View {
        VStack {
            Text("Clap App")
                .font(.largeTitle)
                .padding()

            Button(action: {
                viewModel.onClapClick()
                clapCount += 1
            }) {
                Image(systemName: "hand.raised.fill")
                    .font(.system(size: 100))
                    .foregroundColor(.blue)
            }
            .padding()

            Text("Claps: \(clapCount)")
                .font(.title2)
                .padding()

            if isPlaying {
                Text("🔊 Playing...")
                    .foregroundColor(.green)
                    .padding()
            }
        }
        .onAppear {
            viewModel.initialize()
        }
        .onDisappear {
            viewModel.release()
        }
    }
}

#Preview {
    ContentView()
}
```

**Puntos importantes**:
- En el `init()` de SwiftUI se obtiene el ViewModel de Koin usando `KoinHelper().getClapViewModel()`
- El ViewModel se almacena en una propiedad privada (no en @StateObject porque es una clase Kotlin)
- Se llama a `viewModel.initialize()` en `onAppear` y `release()` en `onDisappear`

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

### Completados
- ✅ **Arquitectura Koin DI**: Implementación completa de inyección de dependencias en ambas plataformas
- ✅ **Android MediaPlayer**: SoundPlayer funcional con manejo de errores robusto
- ✅ **Inicialización ClapApplication**: Pattern correcto en Android
- ✅ **KoinHelper para iOS**: Acceso a dependencias desde Swift

### Por Hacer
1. **CRÍTICO - Arreglar iOS AVAudioPlayer**: Configurar AVFoundation correctamente en build.gradle.kts
   - Implementar audio nativo real usando AVAudioPlayer
   - Reemplazar NativeSoundPlayer stub con implementación completa
   - Configurar linkage de AVFoundation

2. **Agregar tests** para el código compartido
   - Tests unitarios para ClapViewModel
   - Tests de las implementaciones de SoundPlayer

3. **Implementar Compose Multiplatform** para UI compartida
   - Migrar MainActivity composable a shared
   - Compartir UI entre Android e iOS

4. **Agregar más funcionalidades** compartidas
   - Persistencia de estado
   - Analytics
   - Logging

5. **Configurar CI/CD** para ambas plataformas
   - GitHub Actions
   - Tests automáticos
   - Build y deployment automático

## 📚 Recursos Adicionales

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/lp/mobile/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
