buildscript {
    repositories {
        maven { url = uri("https://jitpack.io") }
        google() // Add this line for Google's Maven repository
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        // Add other dependencies for the build script if needed
    }
}

// Rest of your build.gradle file...

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

}

