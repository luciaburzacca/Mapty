plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleGmsGoogleServices)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.mapty"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mapty"
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
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //implementation(libs.androidx.navigation.safe.args.gradle.plugin)

    implementation(libs.androidx.preference.ktx)
    implementation(libs.play.services.location)

    implementation (libs.firebase.database)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.osmdroid.android)
    implementation (libs.osmdroid.geopackage)
    implementation (libs.osmdroid.wms)

    implementation (libs.androidx.appcompat.v131)

    implementation (libs.play.services.auth)

    implementation (libs.kotlinx.coroutines.play.services)
    implementation (libs.kotlinx.coroutines.core)

    implementation (libs.glide)
    annotationProcessor (libs.compiler)



}