// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.kotlin.dsl.*

buildscript {
    val kotlin_version by extra("1.5.10")
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.HILT}")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.NAVIGATION}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://github.com/jitsi/jitsi-maven-repository/raw/master/releases")
        }
        jcenter() // Warning: this repository is going to shut down soon
    }
}

tasks {
    val clean by registering(Delete::class)
    delete(rootProject.buildDir)
}