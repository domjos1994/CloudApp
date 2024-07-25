plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hiltLibrary)
    alias(libs.plugins.kaptLibrary)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "de.domjos.cloudapp2.appbasics"
    compileSdk = rootProject.extra["sdk_compile"] as Int

    defaultConfig {
        minSdk = rootProject.extra["sdk_min"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = rootProject.extra["minify"] as Boolean
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = rootProject.extra["java_jvm"] as JavaVersion
        targetCompatibility = rootProject.extra["java_jvm"] as JavaVersion
    }
    kotlinOptions {
        jvmTarget = rootProject.extra["java_version"] as String
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)

    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.icons.extended)
    implementation(libs.androidx.runtime.android)
    debugImplementation(libs.ui.tooling)

    // hilt
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}