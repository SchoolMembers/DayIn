plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
//    kotlin("ksp")
}

android {
    namespace = "com.example.dayin"
    compileSdk = 34
    viewBinding.isEnabled = true

    defaultConfig {
        applicationId = "com.example.dayin"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.databinding:viewbinding:7.0.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    /*implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.0.0")
    implementation("androidx.room:room-ktx:2.6.1")*/

    implementation("com.kizitonwose.calendar:view:2.5.2")
    implementation("com.kizitonwose.calendar:compose:2.5.2")
}