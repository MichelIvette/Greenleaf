
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")


}

android {
    namespace = "com.example.greenleaf_v100"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.greenleaf_v100"
        minSdk = 30
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //Livedata
    //implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.ktx)

    //RecyclerView
    implementation(libs.recyclerview)
    implementation(libs.glide)


    //FireBase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.firebase.firestore.ktx)
    implementation (libs.firebase.auth.ktx)

    implementation (libs.firebase.storage)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 1) Room runtime
    implementation("androidx.room:room-runtime:2.5.1")
    // 2) Extensiones Kotlin + Coroutines
    implementation("androidx.room:room-ktx:2.5.1")
    // 3) Processor para generar el código de Room
    kapt("androidx.room:room-compiler:2.5.1")

    // (opcional) si usas LiveData con Room
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")



}
