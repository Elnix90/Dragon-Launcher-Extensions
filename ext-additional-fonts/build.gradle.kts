import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.android.application)
}

kotlin {
    jvmToolchain(17)
}

extensions.configure<ApplicationExtension> {
    namespace = "org.elnix.dragonlauncher.fonts"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.elnix.dragonlauncher.fonts"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
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
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs.keepDebugSymbols.add("**/*.so")
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }
}

tasks.register<Copy>("copyFontsCache") {
    from("${projectDir}/google-fonts-cache.json")
    into("${projectDir}/src/main/assets")
}

tasks.named("preBuild") {
    dependsOn("copyFontsCache")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    
    // Coroutines, asynchronous calls
    implementation(libs.kotlinx.coroutines.android)
}
