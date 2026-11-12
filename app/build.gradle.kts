import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.calovelo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.calovelo.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val geminiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        val mapsKey = properties.getProperty("MAPS_API_KEY") ?: ""

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_1", "\"${properties.getProperty("GEMINI_BURNER_KEY_1") ?: ""}\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_2", "\"${properties.getProperty("GEMINI_BURNER_KEY_2") ?: ""}\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_3", "\"${properties.getProperty("GEMINI_BURNER_KEY_3") ?: ""}\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_4", "\"${properties.getProperty("GEMINI_BURNER_KEY_4") ?: ""}\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_5", "\"${properties.getProperty("GEMINI_BURNER_KEY_5") ?: ""}\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_6", "\"${properties.getProperty("GEMINI_BURNER_KEY_6") ?: ""}\"")
        buildConfigField("String", "GEMINI_BURNER_KEY_7", "\"${properties.getProperty("GEMINI_BURNER_KEY_7") ?: ""}\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true // BuildConfig.DEBUG gates the destructive-migration fallback
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    androidResources {
        noCompress += "tflite"
    }
}

kotlin {
    jvmToolchain(17)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt + Hilt-WorkManager
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // WorkManager, Health Connect, DataStore, Location, Coroutines
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.android)

    // Phase 3: CameraX, Coil, TFLite, Serialization
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.coil.compose)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.kotlinx.serialization.json)
    
    // Guava (Required for CameraX ListenableFuture)
    implementation("com.google.guava:guava:32.1.3-android")

    // Lottie for 3D animated Exercise Guides
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    // Google GenAI (Gemini Flash Vision)
    implementation(libs.google.ai.generativeai)

    // Google Maps Compose
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
