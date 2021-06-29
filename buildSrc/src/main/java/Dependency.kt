object Dependency {

    // KOTLIN
    const val KOTLIN_STDLIB_JDK7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.KOTLIN}"
    const val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"

    // UI
    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINT_LAYOUT}"
    const val CONSTRAINT_LAYOUT_COMPOSE = "androidx.constraintlayout:constraintlayout-compose:${Versions.CONSTRAINT_LAYOUT}"
    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"

    // VIEW MODEL AND LIVE DATA
    const val LIFECYCLE_EXTENSIONS = "androidx.lifecycle:lifecycle-extensions:${Versions.LIFECYCLE_EXTENSIONS}"
    const val LIFECYCLE_VIEW_MODEL_KTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT}"
    const val FRAGMENT_TEST = "androidx.fragment:fragment-testing:${Versions.FRAGMENT}"

    // NAVIGATION
    const val NAVIGATION_FRAGMENT_KTX = "androidx.navigation:navigation-fragment-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_UI_KTX = "androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_DYNAMIC_FEATURES_FRAGMENT = "androidx.navigation:navigation-dynamic-features-fragment:1.0.0"

    // CORE WITH KTX
    const val CORE_KTX = "androidx.core:core-ktx:${Versions.CORE}"

    // MOSHI
    const val MOSHI = "com.squareup.moshi:moshi:${Versions.MOSHI}"
    const val MOSHI_KOTLIN = "com.squareup.moshi:moshi-kotlin:${Versions.MOSHI}"

    // RETROFIT AND MOSHI
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT_MOSHI_CONVERTER = "com.squareup.retrofit2:converter-moshi:${Versions.RETROFIT}"

    // OKHTTP
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"

    // Timber
    const val TIMBER = "com.jakewharton.timber:timber:${Versions.TIMBER}"

    // RECYCLERVIEW
    const val RECYCLERVIEW = "androidx.recyclerview:recyclerview:${Versions.RECYCLERVIEW}"
    const val LEGACY_SUPPORT = "androidx.legacy:legacy-support-v4:${Versions.LEGACY_SUPPORT}"
    const val APP_COMPAT = "androidx.appcompat:appcompat:${Versions.APP_COMPAT}"

    // GLIDE
    const val GLIDE = "com.github.bumptech.glide:glide:${Versions.GLIDE}"

    // ROOM DB
    const val ROOM_RUNTIME =  "androidx.room:room-runtime:${Versions.ROOM}"
    const val ROOM_COMPILER = "androidx.room:room-compiler:${Versions.ROOM}"
    const val ROOM_KTX = "androidx.room:room-ktx:${Versions.ROOM}"
    const val ROOM_TESTING = "androidx.room:room-testing:${Versions.ROOM}"

    // HILT AND DAGGER
    const val HILT = "com.google.dagger:hilt-android:${Versions.HILT}"
    const val HILT_COMPILER = "com.google.dagger:hilt-compiler:${Versions.HILT}"
    const val HILT_ANDROID_TESTING = "com.google.dagger:hilt-android-testing:${Versions.HILT}"
    const val HILT_ANDROID_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    const val DAGGER = "com.google.dagger:dagger-compiler:${Versions.DAGGER}"
    const val DAGGER_COMPILER = "com.google.dagger:dagger:${Versions.DAGGER}"

    // Firebase
    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:${Versions.FIREBASE_BOM}"
    const val FIREBASE_UI_AUTH = "com.firebaseui:firebase-ui-auth:${Versions.FIREBASE_UI}"
    const val PLAY_SERVICES_AUTH = "com.google.android.gms:play-services-auth:${Versions.PLAY_SERVICES_AUTH}"
    const val FIREBASE_FIRESTORE_KTX = "com.google.firebase:firebase-firestore-ktx"
    const val FIREBASE_REALTIME_DATABASE_KTX = "com.google.firebase:firebase-database-ktx"
    const val FIREBASE_UI_FIRESTORE = "com.firebaseui:firebase-ui-firestore:${Versions.FIREBASE_UI}"
    const val FIREBASE_COROUTINES_PLAY_SERVICES = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.FIREBASE_COROUTINES}"
    const val FIREBASE_UI_DATABASE = "com.firebaseui:firebase-ui-database:${Versions.FIREBASE_UI_DATABASE}"

    // Jitsi Meet
    const val JITSI_MEET_SDK = "org.jitsi.react:jitsi-meet-sdk:${Versions.JITSI_MEET}"

    // ANDROID TESTS
    const val EXT_JUNIT = "androidx.test.ext:junit:${Versions.EXT_JUNIT}"
    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO}"

    // UNIT TESTING
    const val HAMCREST = "org.hamcrest:hamcrest-all:${Versions.HAMCREST}"
    const val KOTLIN_COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLIN_COROUTINES}"

    // ANDROIDX TEST - JVM TESTING
    const val TEST_EXT_JUNIT_KTX = "androidx.test.ext:junit-ktx:${Versions.ANDROIDX_TEST_EXT_KOTLIN_RUNNER}"
    const val TEST_CORE_KTX = "androidx.test:core-ktx:${Versions.ANDROIDX_TEST_CORE}"
    const val ROBOELECTRIC = "org.robolectric:robolectric:${Versions.ROBO_ELECTRIC}"
    const val ANDROIDX_ARCH_CORE_TESTING = "androidx.arch.core:core-testing:${Versions.ARCH_TESTING}"
    const val ANDROID_ARCH_CORE_TESTING = "android.arch.core:core-testing:${Versions.ANDROID_CORE_TESTING}"
    const val ANDROIDX_TEST_CORE = "androidx.test:core:${Versions.ANDROIDX_TEST_CORE}"

    // Android Instrumented and Unit Tests
    const val JUNIT = "junit:junit:${Versions.JUNIT}"

    // ANDROID INSTRUMENTED TESTS
    const val MOCKITO_CORE = "org.mockito:mockito-core:${Versions.MOCKITO}"
    const val DEXMAKER_MOCKITO = "com.linkedin.dexmaker:dexmaker-mockito:${Versions.DEX_MAKER}"
    const val ESPRESSO_CONTRIB = "androidx.test.espresso:espresso-contrib:${Versions.ESPRESSO}"

}