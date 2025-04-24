// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties // Import Properties class
import java.io.FileInputStream // Import FileInputStream class

// Load secrets from secrets.properties into project.extra
val secrets: Properties = Properties()
val secretsPropertiesFile = rootProject.file("secrets.properties")

if (secretsPropertiesFile.exists()) {
    secretsPropertiesFile.inputStream().use { inputStream ->
        secrets.load(inputStream)
    }
}

// Make secrets available via project.extra (Kotlin DSL equivalent of Groovy's ext)
extra.set("secrets", secrets)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Firebase
    alias(libs.plugins.google.gms.google.services) apply false
}