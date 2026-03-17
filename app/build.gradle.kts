plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.aaosmaptest"
    compileSdk = 34 // Android 14

    defaultConfig {
        applicationId = "com.example.aaosmaptest"
        minSdk = 33 // Android 13/14 Automotive focus
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        // Declare properties property to enable Activity Embedding splits
        manifestPlaceholders["android.window.PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED"] = "true"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Window manager for Activity Embedding
    implementation("androidx.window:window:1.2.0")
    
    // Fragment library
    implementation("androidx.fragment:fragment-ktx:1.6.2")
}
