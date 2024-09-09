plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hiltLibrary)
    alias(libs.plugins.kaptLibrary)
    alias(libs.plugins.serializationLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "de.domjos.cloudapp2"
    compileSdk = rootProject.extra["sdk_compile"] as Int

    defaultConfig {
        applicationId = "de.domjos.cloudapp2"
        minSdk = rootProject.extra["sdk_min"] as Int
        targetSdk = rootProject.extra["sdk_compile"] as Int
        versionCode = rootProject.extra["version"] as Int
        versionName = rootProject.extra["version_name"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
        }
        jniLibs {
            excludes += "/META-INF/groovy/**"
            excludes += "/zoneinfo-global/**"
        }
    }

    configurations.all {
        this.exclude(group = "commons-logging")
        this.exclude(group = "org.hamcrest")
    }
}

dependencies {
    implementation(project(":Data"))
    implementation(project(":Database"))

    implementation(project(":AppBasics"))
    implementation(project(":CalendarFeature"))
    implementation(project(":ChatFeature"))
    implementation(project(":ContactFeature"))
    implementation(project(":DataFeature"))
    implementation(project(":NotificationFeature"))
    implementation(project(":NotesFeature"))
    implementation(project(":CalDav"))
    implementation(project(":CarDav"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences.core.jvm)
    implementation(libs.firebase.crashlytics)

    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.constraint)
    implementation(libs.accompanist.permissions)
    implementation(libs.compose.preference)
    implementation(libs.androidx.preference)
    implementation(libs.coil)
    implementation(libs.coil.svg)
    implementation(libs.icons.extended)

    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.material)
    implementation(project(":REST"))

    // splash
    implementation(libs.splash.screen)

    // glance
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.serialization.json)

    // hilt
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    // testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}