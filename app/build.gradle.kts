plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.cuatrodivinas.seekandsolve"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cuatrodivinas.seekandsolve"
        minSdk = 31
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
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation (libs.play.services.maps)
    implementation (libs.glide)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.database.ktx)
    annotationProcessor (libs.compiler)
    implementation (libs.osmdroid.android)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation (libs.play.services.auth)
    implementation (libs.firebase.auth.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.gson)
    implementation(libs.retrofit2.retrofit)
    implementation ("com.squareup.retrofit2:converter-gson:2.4.0")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.squareup.picasso)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase Auth
    implementation(libs.google.firebase.auth)

    // Firebase Realtime Database
    implementation(libs.firebase.database)

    // Firebase Storage
    implementation(libs.firebase.storage)
}