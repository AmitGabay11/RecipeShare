plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.recipeshare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.recipeshare"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core AndroidX libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose and UI libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material) // Material 2 for consistent components
    implementation(libs.androidx.material.icons.core) // Core Material icons
    implementation(libs.androidx.material.icons.extended) // Extended Material icons
    implementation(libs.androidx.material3) // Material 3 (optional)

    // Firebase
    implementation(platform(libs.firebase.bom)) // Firebase BoM
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)

    // Coil for image loading
    implementation(libs.coil.compose) // Add Coil for loading images

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.storage.ktx)

    implementation(libs.androidx.room.runtime) // Room runtime
    kapt("androidx.room:room-compiler:2.6.1")         // Annotation processor for Room
    implementation(libs.androidx.room.ktx)    // Coroutines support for Room



    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
