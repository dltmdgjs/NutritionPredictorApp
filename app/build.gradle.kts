plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.seunghun.nutritionpredictorapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.seunghun.nutritionpredictorapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // TFLite용 (중요)
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }

    // TFLite 메모리 매핑 안정화
    androidResources {
        noCompress += setOf("tflite")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // TensorFlow Lite (필수)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")

    // 달력, 파이차트
    implementation("com.kizitonwose.calendar:view:2.5.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}