plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.food_delivery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.food_delivery"
        minSdk = 24
        targetSdk = 36
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

    buildFeatures {
        dataBinding= true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.airbnb.android:lottie:6.0.0")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("io.socket:socket.io-client:2.1.0")
    dependencies {
        // ... other dependencies
        implementation ("com.google.android.gms:play-services-location:21.0.1")
        implementation ("com.github.bumptech.glide:glide:5.0.5")

            implementation ("de.hdodenhof:circleimageview:3.1.0")
        }
    }






