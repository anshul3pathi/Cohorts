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

        testInstrumentationRunner = "com.example.cohorts.CustomTestRunner"
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/AL2.0")
        exclude("META-INF/LGPL2.1")
        exclude("**/attach_hotspot_windows.dll")
        exclude("META-INF/licenses/**")
        exclude("META-INF/AL2.0")
        exclude("META-INF/LGPL2.1")
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
    implementation(Dependency.KOTLIN_STDLIB)
    implementation(Dependency.LEGACY_SUPPORT)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlin_version"]}")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Android Testing
    androidTestImplementation(Dependency.EXT_JUNIT)
    androidTestImplementation(Dependency.ESPRESSO_CORE)

    // Dependencies for Android instrumented unit tests
    androidTestImplementation(Dependency.JUNIT)
    androidTestImplementation(Dependency.KOTLIN_COROUTINES_TEST)

    // Unit Testing
    testImplementation(Dependency.JUNIT)
    testImplementation(Dependency.HAMCREST)
    testImplementation(Dependency.KOTLIN_COROUTINES_TEST)
    testImplementation(Dependency.MOCKITO_CORE)

    // AndroidX Test - JVM testing
    testImplementation(Dependency.TEST_EXT_JUNIT_KTX)
    testImplementation(Dependency.TEST_CORE_KTX)
    testImplementation(Dependency.ROBOELECTRIC)
    testImplementation(Dependency.ANDROIDX_ARCH_CORE_TESTING)
    androidTestImplementation(Dependency.ANDROID_ARCH_CORE_TESTING)

    // Hilt
    implementation(Dependency.HILT)
    kapt(Dependency.HILT_COMPILER)
    androidTestImplementation(Dependency.HILT_ANDROID_TESTING)
    kaptAndroidTest(Dependency.HILT_ANDROID_COMPILER)
    androidTestAnnotationProcessor(Dependency.HILT_ANDROID_COMPILER)

    // Navigation
    implementation(Dependency.NAVIGATION_FRAGMENT_KTX)
    implementation(Dependency.NAVIGATION_UI_KTX)

    // Firebase
    implementation(platform(Dependency.FIREBASE_BOM))
    implementation(Dependency.FIREBASE_UI_AUTH)
    implementation(Dependency.PLAY_SERVICES_AUTH)
    implementation(Dependency.FIREBASE_REALTIME_DATABASE_KTX)
    implementation(Dependency.FIREBASE_FIRESTORE_KTX)
    implementation(Dependency.FIREBASE_UI_DATABASE)
    implementation(Dependency.FIREBASE_UI_FIRESTORE)

    /* coroutines support for firebase operations */
    implementation(Dependency.FIREBASE_COROUTINES_PLAY_SERVICES)

    // Timber
    implementation(Dependency.TIMBER)

    // Jitsi
    implementation (Dependency.JITSI_MEET_SDK)
}