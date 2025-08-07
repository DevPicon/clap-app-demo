// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

detekt {
    toolVersion = "1.23.5"
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        md.required.set(true)
        html.required.set(false)
        xml.required.set(false)
        sarif.required.set(false)
    }
}