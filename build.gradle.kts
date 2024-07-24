// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kaptLibrary) apply false
    alias(libs.plugins.hiltLibrary) apply false
    alias(libs.plugins.serializationLibrary) apply false
    alias(libs.plugins.compose.compiler) apply false
}

buildscript {
    extra.apply {
        set("java_version", "17")
        set("java_jvm", JavaVersion.VERSION_17)
        set("sdk_compile", 34)
        set("sdk_min", 26)
        set("compose_version", "1.5.8")
        set("version", 17)
        set("version_name", "1.0.Beta.10")
        set("minify", false)
    }
}