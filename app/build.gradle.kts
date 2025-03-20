plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //alias(libs.plugins.google.gms.google.services)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin") version "2.7.3"  // Specify the plugin version explicitly
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.sciencequest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sciencequest"
        minSdk = 28
        targetSdk = 35
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

    viewBinding {
        enable = true
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation ("com.google.android.material:material:1.10.0")
    // Data Binding
    implementation ("androidx.databinding:databinding-runtime:7.1.0")



    implementation ("org.jetbrains.kotlin:kotlin-parcelize-runtime:1.8.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation ("com.squareup.picasso:picasso:2.8") // For image loading
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.database)
    implementation(libs.sceneform.ux)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.firebase.auth)
    implementation(libs.car.ui.lib)
    implementation(libs.androidx.gridlayout)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    //Space Imagery Exploration (EPIC API)
    implementation ("com.github.chrisbanes:photoview:2.1.4")  // For pinch-to-zoom functionality
    implementation("androidx.appcompat:appcompat:1.6.1") // Make sure it's the latest version

    //Solar System Explorer (Exoplanet Archive)
    implementation ("com.google.android.material:material:1.8.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")  // OkHttp for making requests

    implementation ("com.google.ar.sceneform:core:1.17.1")  // Sceneform core
    implementation ("com.google.ar.sceneform.ux:sceneform-ux:1.17.1")  // Sceneform UX
    implementation ("com.google.ar:core:1.32.0")  // Use the latest ARCore version


    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.3")

    // ExoPlayer dependency (latest version as of now)
    implementation ("androidx.media3:media3-exoplayer:1.1.0")

    // ExoPlayer UI dependency
    implementation ("androidx.media3:media3-ui:1.1.0")

    // ExoPlayer common utilities (optional, but recommended)
    implementation ("androidx.media3:media3-common:1.1.0")

    // ExoPlayer datasource (for custom data sources)
    implementation ("androidx.media3:media3-datasource:1.1.0")

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")

    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("com.google.android.material:material:1.4.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.0.0")


}