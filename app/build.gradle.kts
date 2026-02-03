import org.jetbrains.kotlin.gradle.dsl.JvmTarget
val gameVersion = "1.6.00.14"
val installerVersion = "14"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.github.sthenight.sfsinstaller"
    compileSdk {
        version = release(36)
    }


    defaultConfig {
        applicationId = "com.StefMorojna.SpaceflightSimulator"
        minSdk = 29
        targetSdk = 36
        versionCode = 782
        versionName = "$gameVersion-$installerVersion"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        defaultConfig.ndk.abiFilters("arm64-v8a", "armeabi-v7a")
    }

    signingConfigs {
        create("release") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

        buildTypes {
            all {
                buildConfigField("String","GAME_VERSION","\"$gameVersion\"")
            }
            debug {
                buildConfigField("Boolean", "IS_DEBUG", "true")
            }
            release {
                buildConfigField("Boolean", "IS_DEBUG", "false")
                isMinifyEnabled = true
                isShrinkResources = true
                signingConfig = signingConfigs.getByName("release")
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        compilerOptions.freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        compilerOptions.freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }

    buildFeatures {
        compose = true
        android.buildFeatures.buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.square.okio)
    implementation(libs.square.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.nav.compose)
    testImplementation(libs.junit)
    implementation(libs.androidx.compose.material3.windowsizeclass)
    implementation(libs.androidx.compose.material3.adaptivenavigationsuite)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.google.material)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}