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

### Configuración Android

1. Abre el proyecto en Android Studio
2. Sincroniza el proyecto con Gradle
3. Ejecuta la aplicación en un emulador o dispositivo

```bash
# Desde la línea de comandos
./gradlew assembleDebug
```

### Configuración iOS

1. Abre `iosApp/iosApp.xcodeproj` en Xcode
2. Selecciona tu dispositivo o simulador
3. Ejecuta la aplicación

```bash
# Construir el framework compartido
./gradlew :shared:linkReleaseFrameworkIosArm64
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

### Ejemplo de expect/actual

```kotlin
// commonMain/SoundPlayer.kt
expect class PlatformSoundPlayer() : SoundPlayer

// androidMain/PlatformSoundPlayer.kt
actual class PlatformSoundPlayer : SoundPlayer {
    // Implementación Android
}

// iosMain/PlatformSoundPlayer.kt
actual class PlatformSoundPlayer : SoundPlayer {
    // Implementación iOS
}
```

## 📦 Dependencias

### Compartidas
- `kotlinx-coroutines-core`: Corrutinas para código asíncrono
- `kotlinx-coroutines-android`: Corrutinas para Android
- `kotlinx-coroutines-ios`: Corrutinas para iOS

### Android
- `androidx.compose`: UI moderna
- `androidx.activity`: Componentes de actividad
- `androidx.lifecycle`: Componentes de ciclo de vida

### iOS
- `SwiftUI`: Framework de UI nativo
- `AVFoundation`: Reproducción de audio

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

- El archivo de sonido `claps.ogg` debe estar en `app/src/main/res/raw/` para Android
- Para iOS, el archivo debe agregarse al bundle de la aplicación
- La implementación iOS usa `AVAudioPlayer` nativo
- La implementación Android usa `MediaPlayer` nativo

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

