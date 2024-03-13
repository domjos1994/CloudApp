// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kaptLibrary) apply false
}

buildscript {
    extra.apply {
        set("java_version", "19")
        set("java_jvm", JavaVersion.VERSION_19)
        set("sdk_compile", 34)
        set("sdk_min", 26)
        set("compose_version", "1.5.8")
    }
}