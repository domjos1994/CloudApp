plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "de.domjos.cloudapp.caldav"
    compileSdk = rootProject.extra["sdk_compile"] as Int

    defaultConfig {
        minSdk = rootProject.extra["sdk_min"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        with(substitute(module("org.hamcrest:hamcrest-core:1.3"))) {
            module("junit:junit:4.7")
        }
    }
    resolutionStrategy.eachDependency {
        if (requested.name == "groovy-all") {
            useTarget("${requested.group}:groovy:${requested.version}")
            because("prefer 'groovy' over 'groovy-all'")
        }
        if (requested.name == "log4j") {
            useTarget("org.slf4j:log4j-over-slf4j:1.7.10")
            because("prefer 'log4j-over-slf4j' 1.7.10 over any version of 'log4j'")
        }
    }
}

dependencies {
    implementation(project(":Database"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}