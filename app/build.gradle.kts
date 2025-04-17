// Import this from Java cause then getLocalProperty does not work (*Gemini did it)
import java.util.Properties

fun getLocalProperty(key: String): String {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { properties.load(it) }
    }

    return properties.getProperty(key) ?: ""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services) // Google Services Gradle Plugin
}

android {
    namespace = "com.example.checkpoint"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.checkpoint"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read API Key from local.properties and expose it in BuildConfig
        buildConfigField("String", "RAWG_API_KEY", "\"${getLocalProperty("RAWG_API_KEY")}\"")    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    // Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase
    //noinspection BomWithoutPlatform
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    // implementation(libs.firebase.auth.ktx) The same for Kotlin (I think is already in .auth)

    // RAWG Api & Retrofit
    implementation(libs.retrofit) // Networking
    implementation(libs.converter.gson) // JSON <-> Kotlin
    implementation(libs.gson) // Core GSON library

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Architecture Componentes
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.logging.interceptor)

    // Glide for the images
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}