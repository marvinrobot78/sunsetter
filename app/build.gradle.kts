plugins {
    id("com.android.application")
}

android {
    namespace = "com.atkinson.sunsetter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.atkinson.sunsetter"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.wear:wear:1.4.0")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0")
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("org.shredzone.commons:commons-suncalc:3.11")
}
