// Top-level build.gradle.kts
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Add this for MPAndroidChart
    }
}
