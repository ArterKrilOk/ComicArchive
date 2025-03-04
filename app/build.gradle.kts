import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.room)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

val majorVersion = 0
val minorVersion = 0
val patchVersion = 7

val appVersionName = "$majorVersion.$minorVersion.$patchVersion"

android {
    namespace = "space.pixelsg.comicarchive"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val localProperties = gradleLocalProperties(rootDir, providers)

            keyAlias = localProperties.getProperty("SIGNING_KEY_ALIAS") ?: "defaultAlias"
            keyPassword =
                localProperties.getProperty("SIGNING_KEY_PASSWORD") ?: "defaultKeyPassword"
            storeFile = file(
                localProperties.getProperty("SIGNING_STORE_FILE") ?: "default/path/to/keystore.jks"
            )
            storePassword =
                localProperties.getProperty("SIGNING_STORE_PASSWORD") ?: "defaultStorePassword"
        }
    }

    defaultConfig {
        applicationId = "space.pixelsg.comicarchive"
        minSdk = 24
        targetSdk = 35
        versionCode = majorVersion * 10000 + minorVersion * 100 + patchVersion
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.transition.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.fragment.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Teapot
    implementation(project(":teapot"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.compose)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel.navigation)

    implementation(libs.kotlinx.datetime)

    implementation(libs.reorderable)
    implementation(libs.ffmpeg.kit.full)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.libtorrent4j)
    implementation(libs.libtorrent4j.android.arm)
    implementation(libs.libtorrent4j.android.arm64)
    implementation(libs.libtorrent4j.android.x86)
    implementation(libs.libtorrent4j.android.x86.x4)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.zoomimage.view.coil3)
    implementation(libs.zoomimage.compose)

    implementation(kotlin("reflect"))
}