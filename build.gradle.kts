buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}

// Global configuration for all subprojects
subprojects {
    apply(plugin = "com.android.application")
    apply(plugin = "org.jetbrains.kotlin.android")
}
