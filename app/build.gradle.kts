plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

import java.util.Properties

val versionProps = Properties().apply {
    val f = rootProject.file("version.properties")
    if (f.isFile) {
        f.inputStream().use { load(it) }
    }
}

val appVersionCode = versionProps.getProperty("VERSION_CODE")?.toIntOrNull() ?: 1
val appVersionName = versionProps.getProperty("VERSION_NAME") ?: "1.0.0"

android {
    namespace = "com.motherledisa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.motherledisa"
        minSdk = 29
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseKeystorePathEnv = System.getenv("ANDROID_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
    val releaseKeystorePasswordEnv = System.getenv("ANDROID_KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
    val releaseKeyAliasEnv = System.getenv("ANDROID_KEY_ALIAS")?.takeIf { it.isNotBlank() }
    val releaseKeyPasswordEnv = System.getenv("ANDROID_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
    val hasReleaseSigning =
        releaseKeystorePathEnv != null &&
            releaseKeystorePasswordEnv != null &&
            releaseKeyAliasEnv != null &&
            releaseKeyPasswordEnv != null

    if (hasReleaseSigning) {
        val releaseKeystorePath = requireNotNull(releaseKeystorePathEnv)
        val releaseKeystorePassword = requireNotNull(releaseKeystorePasswordEnv)
        val releaseKeyAlias = requireNotNull(releaseKeyAliasEnv)
        val releaseKeyPassword = requireNotNull(releaseKeyPasswordEnv)

        signingConfigs {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.01.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // BLE (Nordic)
    implementation("no.nordicsemi.android:ble:2.11.0")
    implementation("no.nordicsemi.android:ble-ktx:2.11.0")
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Color Picker
    implementation("com.godaddy.android.colorpicker:compose-color-picker-android:0.7.0")

    // Reorderable (drag-and-drop lists)
    implementation("sh.calvin.reorderable:reorderable:3.0.0")

    // Utilities
    implementation("com.jakewharton.timber:timber:5.0.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
