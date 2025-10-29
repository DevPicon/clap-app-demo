# Clap App Demo - Kotlin Multiplatform

Esta es una aplicación de demostración que reproduce un sonido de aplausos cuando se toca la pantalla. El proyecto ha sido migrado a **Kotlin Multiplatform (KMP)** para compartir código entre Android e iOS.

## 🚀 Características

- **Kotlin Multiplatform**: Código compartido entre Android e iOS
- **Jetpack Compose**: UI moderna para Android
- **SwiftUI**: UI nativa para iOS
- **Reproducción de audio**: Funcionalidad compartida entre plataformas
- **Arquitectura MVVM**: ViewModel compartido

## 📱 Plataformas Soportadas

- **Android**: API 21+ (Android 5.0+)
- **iOS**: iOS 17.0+

## 🏗️ Estructura del Proyecto

```
clap-app-demo/
├── app/                    # Módulo Android
│   ├── src/main/
│   │   ├── java/          # Código específico de Android
│   │   └── res/           # Recursos Android
│   └── build.gradle.kts   # Configuración Android
├── shared/                # Módulo compartido KMP
│   ├── src/
│   │   ├── commonMain/    # Código común
│   │   ├── androidMain/   # Implementación Android
│   │   └── iosMain/       # Implementación iOS
│   └── build.gradle.kts   # Configuración KMP
├── iosApp/                # Proyecto iOS
│   ├── iosApp/           # Código Swift
│   └── iosApp.xcodeproj/ # Proyecto Xcode
└── build.gradle.kts       # Configuración raíz
```

## 🛠️ Configuración

### Prerrequisitos

- **Android Studio** (última versión)
- **Xcode** 15.0+ (para iOS)
- **Kotlin** 1.9.23+
- **Gradle** 8.0+

### Configuración de Koin

**Koin** es un framework ligero de inyección de dependencias para Kotlin. Se usa en este proyecto para desacoplar los módulos Android e iOS de sus implementaciones específicas.

#### Estructura de Koin en el Proyecto

- **`KoinSetup.kt`** (commonMain): Define la función `expect fun getPlatformModule(): Module`
- **`AndroidModule.kt`** (androidMain): Define el módulo Android con todas las dependencias
- **`IosModule.kt`** (iosMain): Define el módulo iOS con todas las dependencias
- **`ClapApplication.kt`** (Android): Inicializa Koin en la aplicación

#### Ejemplo de Módulo Android

```kotlin
// shared/src/androidMain/kotlin/AndroidModule.kt
val androidModule = module {
    single { ResourceReader(androidContext()) }
    single<SoundPlayer> { AndroidSoundPlayer(get()) }
    single { ClapViewModel(get()) }
}

actual fun getPlatformModule() = androidModule
```

### Configuración Android

1. Abre el proyecto en Android Studio
2. Sincroniza el proyecto con Gradle
3. Ejecuta la aplicación en un emulador o dispositivo

```bash
# Desde la línea de comandos
./gradlew assembleDebug
```

#### Inicialización de Koin en Android

Crea una clase `Application` personalizada que inicialice Koin:

```kotlin
// app/src/main/java/pe/devpicon/android/clapapp/ClapApplication.kt
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

Declara la aplicación en `AndroidManifest.xml`:

```xml
<application
    android:name=".ClapApplication"
    ...>
```

#### Uso de Inyección en MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel: ClapViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(onLaunchClick = { viewModel.onClapClick() })
        }
    }
}
```

### Configuración iOS

1. Abre `iosApp/iosApp.xcodeproj` en Xcode
2. Selecciona tu dispositivo o simulador
3. Ejecuta la aplicación

```bash
# Construir el framework compartido
./gradlew :shared:linkReleaseFrameworkIosArm64
```

#### Inicialización de Koin en iOS

Crea un `KoinHelper` para facilitar el acceso a las dependencias en Swift:

```kotlin
// shared/src/iosMain/kotlin/KoinHelper.kt
class KoinHelper {
    fun getClapViewModel(): ClapViewModel {
        return get()
    }
}
```

En tu vista principal de SwiftUI:

```swift
// iosApp/iosApp/ContentView.swift
import SwiftUI
import shared

struct ContentView: View {
    private let viewModel: ClapViewModel

    init() {
        // Obtén el ViewModel desde Koin
        self.viewModel = KoinHelper().getClapViewModel()
    }

    var body: some View {
        VStack {
            Button(action: {
                viewModel.onClapClick()
            }) {
                Image(systemName: "hand.raised.fill")
                    .font(.system(size: 100))
                    .foregroundColor(.blue)
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
```

## 🔧 Desarrollo

### Código Compartido

El código compartido se encuentra en el módulo `shared/`:

- **`SoundPlayer.kt`**: Interfaz común para reproducción de audio
- **`ClapViewModel.kt`**: ViewModel compartido con la lógica de negocio
- **`PlatformSoundPlayer.kt`**: Implementaciones específicas por plataforma

### Agregar Nueva Funcionalidad

1. **Código común**: Agrega en `shared/src/commonMain/`
2. **Android específico**: Agrega en `shared/src/androidMain/`
3. **iOS específico**: Agrega en `shared/src/iosMain/`

### Ejemplo de expect/actual con Koin

El proyecto usa **Koin** para inyección de dependencias en lugar de instanciar directamente. La interfaz `SoundPlayer` se implementa de manera específica por plataforma:

```kotlin
// commonMain/SoundPlayer.kt
interface SoundPlayer {
    fun playClapSound()
    fun release()
    val isPlaying: StateFlow<Boolean>
}

// Función expect para obtener la plataforma
expect fun buildPlatformSoundPlayer(): SoundPlayer

// androidMain/PlatformSoundPlayer.kt
actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()

class AndroidSoundPlayer(private val resourceReader: ResourceReader) : SoundPlayer {
    // Implementación Android usando MediaPlayer
}

// iosMain/PlatformSoundPlayer.kt
actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()

class NativeSoundPlayer : SoundPlayer {
    // Implementación iOS (stub actualmente)
}
```

## 📦 Dependencias

### Compartidas
- `kotlinx-coroutines-core`: Corrutinas para código asíncrono
- `kotlinx-coroutines-android`: Corrutinas para Android
- `kotlinx-coroutines-ios`: Corrutinas para iOS
- `koin-core`: Framework de inyección de dependencias multiplataforma

### Android
- `koin-android`: Extensiones de Koin para Android
- `androidx.compose`: UI moderna
- `androidx.activity`: Componentes de actividad
- `androidx.lifecycle`: Componentes de ciclo de vida

### iOS
- `SwiftUI`: Framework de UI nativo
- `AVFoundation`: Reproducción de audio (pendiente de configuración completa)

## 🚀 Beneficios de KMP

1. **Código Compartido**: ~80% del código de lógica de negocio es compartido
2. **Mantenimiento**: Un solo lugar para cambios en la lógica
3. **Consistencia**: Comportamiento idéntico en ambas plataformas
4. **Productividad**: Desarrollo más rápido para múltiples plataformas

## 🔄 Migración desde Android Puro

### Cambios Realizados

1. **Estructura**: Agregado módulo `shared/` para código KMP
2. **SoundPlayer**: Migrado a expect/actual pattern
3. **ViewModel**: Creado ViewModel compartido
4. **Dependencias**: Actualizadas para soportar KMP
5. **iOS**: Agregado proyecto iOS básico

### Archivos Modificados

- `build.gradle.kts` (raíz): Agregados plugins KMP
- `settings.gradle.kts`: Incluido módulo shared
- `app/build.gradle.kts`: Dependencia del módulo shared
- `MainActivity.kt`: Usa ViewModel compartido
- `SoundPlayer.kt`: Migrado a módulo shared

## 📝 Notas

- El archivo de sonido `claps.ogg` está ubicado en `app/src/main/res/raw/` para Android
- Para iOS, el archivo debe agregarse al bundle de la aplicación en Xcode
- La implementación Android usa `MediaPlayer` nativo con manejo automático de recursos
- La implementación iOS es actualmente un stub (ver "Problemas Conocidos")

## ⚠️ Problemas Conocidos

### Implementación iOS de Audio Incompleta

La reproducción de audio en iOS (`NativeSoundPlayer`) es actualmente un stub que no reproduce sonido real. Los pasos pendientes incluyen:

1. **Configurar AVFoundation**: Agregar el framework de `AVFoundation` a la configuración de KMP
2. **Implementar AVAudioPlayer**: Reemplazar el stub con una verdadera implementación usando `AVAudioPlayer`
3. **Gestionar el recurso de audio**: Asegurar que el archivo `claps.ogg` se incluya en el bundle de iOS

**Estado Actual**: La aplicación compila y ejecuta en iOS, pero la reproducción de audio no funciona. El botón responde normalmente, solo que sin sonido.

Para más información sobre cómo contribuir a esta funcionalidad, consulta la sección de Contribución.

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

