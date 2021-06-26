plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(Versions.COMPILE_SDK)
    buildToolsVersion(Versions.BUILD_TOOLS)

    defaultConfig {
        applicationId = "com.example.cohorts"
        minSdkVersion(Versions.MIN_SDK)
        targetSdkVersion(Versions.TARGET_SDK)
        versionCode = Versions.APP_VERSION
        versionName = Versions.APP_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
         getByName("release") {
                isMinifyEnabled = false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        val options = this
        options.jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
    }

}

dependencies {

    // Kotlin
    implementation(Dependency.KOTLIN_STDLIB)
    implementation(Dependency.CORE_KTX)

    // Timber
    implementation(Dependency.TIMBER)

    // UI
    implementation(Dependency.APP_COMPAT)
    implementation(Dependency.MATERIAL)
    implementation(Dependency.CONSTRAINT_LAYOUT)

    // ViewModel and LiveData
    implementation(Dependency.LIFECYCLE_EXTENSIONS)
    implementation(Dependency.LIFECYCLE_VIEW_MODEL_KTX)
    implementation(Dependency.FRAGMENT_KTX)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlin_version"]}")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // JUNIT Testing
    testImplementation(Dependency.JUNIT)
    androidTestImplementation(Dependency.EXT_JUNIT)
    androidTestImplementation(Dependency.ESPRESSO_CORE)

    // Hilt
    implementation(Dependency.HILT)
    kapt(Dependency.HILT_COMPILER)

    // Navigation
    implementation(Dependency.NAVIGATION_FRAGMENT_KTX)
    implementation(Dependency.NAVIGATION_UI_KTX)
//    implementation("androidx.navigation:navigation-fragment:2.4.0-alpha03")
//    implementation("androidx.navigation:navigation-ui:2.4.0-alpha03")

    // Firebase
    implementation(platform(Dependency.FIREBASE_BOM))
    implementation(Dependency.FIREBASE_UI_AUTH)
    implementation(Dependency.PLAY_SERVICES_AUTH)
    implementation(Dependency.FIREBASE_REALTIME_DATABASE_KTX)
    implementation(Dependency.FIREBASE_FIRESTORE_KTX)
//    implementation("com.firebaseui:firebase-ui-database:7.1.1")
    implementation(Dependency.FIREBASE_UI_FIRESTORE)

    // Jitsi
    implementation (Dependency.JITSI_MEET_SDK)
}