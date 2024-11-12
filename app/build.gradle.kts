plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.easyfilterporject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easyfilterporject"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Importando o Firebase BoM para versões automáticas
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Adicionando SDKs do Firebase
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-functions:20.1.0")
    implementation ("jp.co.cyberagent.android:gpuimage:2.1.0")
    // Firebase Authentication
    implementation ("com.google.firebase:firebase-auth:21.0.5")
    // Firebase Realtime Database
    implementation ("com.google.firebase:firebase-database:20.0.4")

    //filter
    implementation ("jp.co.cyberagent.android.gpuimage:gpuimage-library:2.1.0")

}


