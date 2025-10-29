#!/bin/bash

# Script de construcción para Clap App Demo - Kotlin Multiplatform

set -e

echo "🚀 Clap App Demo - Kotlin Multiplatform Build Script"
echo "=================================================="

# Función para mostrar ayuda
show_help() {
    echo "Uso: ./build.sh [OPCIÓN]"
    echo ""
    echo "Opciones:"
    echo "  android     Construir solo la aplicación Android"
    echo "  ios         Construir solo el framework iOS"
    echo "  shared      Construir solo el módulo compartido"
    echo "  all         Construir todo (Android + iOS)"
    echo "  clean       Limpiar todos los builds"
    echo "  test        Ejecutar tests"
    echo "  help        Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  ./build.sh android"
    echo "  ./build.sh ios"
    echo "  ./build.sh all"
}

# Función para construir Android
build_android() {
    echo "📱 Construyendo aplicación Android..."
    ./gradlew :app:assembleDebug
    echo "✅ Android build completado"
}

# Función para construir iOS
build_ios() {
    echo "🍎 Construyendo framework iOS..."
    ./gradlew :shared:linkReleaseFrameworkIosArm64
    ./gradlew :shared:linkReleaseFrameworkIosX64
    echo "✅ iOS framework construido"
}

# Función para construir módulo compartido
build_shared() {
    echo "🔧 Construyendo módulo compartido..."
    ./gradlew :shared:build
    echo "✅ Módulo compartido construido"
}

# Función para construir todo
build_all() {
    echo "🏗️ Construyendo todo el proyecto..."
    ./gradlew build
    echo "✅ Build completo finalizado"
}

# Función para limpiar
clean() {
    echo "🧹 Limpiando builds..."
    ./gradlew clean
    echo "✅ Limpieza completada"
}

# Función para tests
run_tests() {
    echo "🧪 Ejecutando tests..."
    ./gradlew test
    echo "✅ Tests completados"
}

# Verificar si se proporcionó un argumento
if [ $# -eq 0 ]; then
    echo "❌ Error: Debes especificar una opción"
    echo ""
    show_help
    exit 1
fi

# Procesar argumentos
case "$1" in
    "android")
        build_android
        ;;
    "ios")
        build_ios
        ;;
    "shared")
        build_shared
        ;;
    "all")
        build_all
        ;;
    "clean")
        clean
        ;;
    "test")
        run_tests
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        echo "❌ Error: Opción desconocida '$1'"
        echo ""
        show_help
        exit 1
        ;;
esac

echo ""
echo "🎉 ¡Proceso completado!"
