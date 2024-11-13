plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            keyAlias = System.getenv("keyAlias")
            storePassword = System.getenv("storePassword")
            keyPassword = System.getenv("keyPassword")
        }
    }
    namespace = "io.github.auag0.pgsharprouteexporter"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.auag0.pgsharprouteexporter"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
    }
    viewBinding {
        enable = true
    }
    packaging {
        resources {
            excludes.add("**/kotlin/**")
            excludes.add("kotlin-tooling-metadata.json")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.activity:activity-ktx:1.9.3")

    val glideVersion = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    ksp("com.github.bumptech.glide:ksp:$glideVersion")

    implementation("me.zhanghai.android.appiconloader:appiconloader-glide:1.5.0")

    val libsuVersion = "6.0.0"
    implementation("com.github.topjohnwu.libsu:core:$libsuVersion")
    implementation("com.github.topjohnwu.libsu:io:$libsuVersion")
}