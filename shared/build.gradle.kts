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
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
