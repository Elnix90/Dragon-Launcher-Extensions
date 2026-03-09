plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.elnix.dragonlauncher.fonts"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.elnix.dragonlauncher.fonts"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "none")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    // Coroutines pour les appels asynchrones
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Retrofit pour les appels API Google Fonts
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
}
