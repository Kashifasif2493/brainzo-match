plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.brainzomatch.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.brainzomatch.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 100
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val keystoreFile = file("brainzo-release.jks")
        val storePass = System.getenv("KEYSTORE_PASSWORD")
        val keyPass = System.getenv("KEY_PASSWORD")
        val aliasEnv = System.getenv("KEY_ALIAS")
        val releaseRequested = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
        if (releaseRequested && (!keystoreFile.exists() || storePass.isNullOrBlank() || keyPass.isNullOrBlank() || aliasEnv.isNullOrBlank())) {
            throw GradleException("Release signing is not configured. Provide app/brainzo-release.jks and KEYSTORE_PASSWORD, KEY_PASSWORD, KEY_ALIAS.")
        }
        if (keystoreFile.exists() && !storePass.isNullOrBlank() && !keyPass.isNullOrBlank() && !aliasEnv.isNullOrBlank()) {
            create("release") {
                storeFile = keystoreFile
                storePassword = storePass
                keyAlias = aliasEnv
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Kotlin 1.9.23 / Compose Compiler 1.5.11 compatible with JDK 17 & API 34
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

// Room schema export location defined at project root level via KSP extension (outside defaultConfig)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // --- AndroidX Core & Lifecycle ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- Jetpack Compose BOM (2024.04.01) & UI ---
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Material Components (XML themes such as Theme.Material3.DayNight.NoActionBar) ---
    implementation("com.google.android.material:material:1.12.0")

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Room Database v2 (with Kotlin Coroutines Flow & KSP) ---
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // --- JSON Serialization (Gson utilized in ProGuard & TypeConverters) ---
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Kotlin Coroutines ---
    val coroutinesVersion = "1.8.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // --- Google Mobile Ads SDK (COPPA & Families Compliant) ---
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // --- Google Play Billing Library v6 ---
    implementation("com.android.billingclient:billing-ktx:6.2.1")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
