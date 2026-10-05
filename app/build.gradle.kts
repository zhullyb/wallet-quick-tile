plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.walletquicktile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.walletquicktile"
        minSdk = 24
        targetSdk = 35
        versionCode = providers.gradleProperty("BUILD_VERSION_CODE")
            .orNull?.toIntOrNull() ?: 1
        versionName = providers.gradleProperty("BUILD_VERSION_NAME").orNull ?: "0.0.1"
    }

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
