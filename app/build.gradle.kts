import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

val properties = Properties()
val propertiesFile = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

android {
    namespace = "cz.tomasjanicek.bp"
    compileSdk = 36

    defaultConfig {
        applicationId = "cz.tomasjanicek.bp"
        minSdk = 29
        targetSdk = 36
        // --- ZMĚNA ZDE ---
        // Načítáme verze z globálních proměnných definovaných v kořenovém build.gradle.kts
        versionCode = rootProject.extra["appVersionCode"] as Int
        versionName = rootProject.extra["appVersionName"] as String
        // --- KONEC ZMĚNY ---
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "MAPS_API_KEY", "\"${properties.getProperty("MAPS_API_KEY")}\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"${properties.getProperty("WEB_CLIENT_ID")}\"")

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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md" // Přidání pravidla pro vyloučení konfliktu
        }
    }
}

dependencies {
    // === Core a UI závislosti ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.coil.compose)
    implementation(libs.material)

    // === Hilt (Dependency Injection) ===
    implementation(libs.hilt.android)
    implementation(libs.hilt.compose)
    kapt(libs.hilt.kapt)

    // === Síťování (Retrofit & Moshi & Gson) ===
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.retrofit.okhtt3)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.converter.gson)
    implementation(libs.gson)

    // === Navigace ===
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ktx)
    implementation(libs.navigation.compose)

    // === Databáze (Room) & DataStore ===
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler.kapt)
    implementation(libs.androidx.datastore.preferences)

    // === Mapy & Lokace ===
    implementation(libs.googlemap)
    implementation(libs.googlemap.compose)
    implementation(libs.googlemap.foundation)
    implementation(libs.googlemap.utils)
    implementation(libs.googlemap.widgets)
    implementation(libs.googlemap.compose.utils)
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)

    // === ML Kit (Image Labeling) ===
    implementation(libs.image.labeling.common)
    implementation(libs.image.labeling.default.common)
    implementation(libs.image.labeling)
    implementation(libs.vision.common)

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))


    // === Ostatní utility ===
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation(libs.mpandroidchart)
    implementation(kotlin("script-runtime"))

    // === Testovací závislosti ===
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)

    // === Android Test (Instrumentované testy) ===
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.kapt)
    androidTestImplementation("io.mockk:mockk-android:1.14.6")

    // Credential Manager (Moderní auth)
    implementation("androidx.credentials:credentials:1.3.0") // Zkontroluj nejnovější verzi
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")

    // Google ID (pro specifické Google Sign-In options)
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Firebase Auth
    implementation("com.google.firebase:firebase-auth")


}

// For Hilt
kapt {
    correctErrorTypes = true
}
