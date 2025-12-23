import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
}

val properties = Properties()
val propertiesFile = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

android {
    namespace = "cz.tomasjanicek.bp"
    compileSdk = 35

    defaultConfig {
        applicationId = "cz.tomasjanicek.bp"
        minSdk = 29
        targetSdk = 35

        versionCode = rootProject.extra["appVersionCode"] as Int
        versionName = rootProject.extra["appVersionName"] as String

        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "MAPS_API_KEY", "\"${properties.getProperty("MAPS_API_KEY")}\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"${properties.getProperty("WEB_CLIENT_ID")}\"")

        // Ujistěte se, že tento Runner ve vašem projektu existuje (vytvořili jsme ho dříve)
        testInstrumentationRunner = "cz.tomasjanicek.bp.HiltTestRunner"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
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
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    // === Core & Lifecycle ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // === Compose UI ===
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material)

    // === Accompanist & Coil ===
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)

    // === Navigation ===
    implementation(libs.androidx.navigation.compose)

    // === Hilt (DI) ===
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material3)
    ksp(libs.hilt.compiler)

    // === Networking ===
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // === Database (Room) & DataStore ===
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // === Firebase & Google Auth ===
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    // === Credential Manager ===
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)

    // Google Drive REST API
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.http.client.gson)
    implementation(libs.google.http.client.android)

    // === Maps & Location ===
    implementation(libs.play.services.location)
    implementation(libs.google.maps.compose)

    // === Work Manager ===
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // === Utils ===
    implementation(libs.timber)
    implementation(libs.mpandroidchart)

    // ==========================================
    // TESTOVÁNÍ (UNIT)
    // ==========================================
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.turbine)

    // ==========================================
    // TESTOVÁNÍ (UI / INSTRUMENTED)
    // ==========================================
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // --- OPRAVA: ŘEŠENÍ KONFLIKTU VERZÍ A CHYBĚJÍCÍCH TŘÍD ---
    // Vynucujeme novější verze knihoven pro obě varianty (test i debug).
    // Tím předejdeme chybě "Duplicate class" a "NoClassDefFoundError: FileTestStorage".

    val testCoreVersion = "1.6.1"
    androidTestImplementation("androidx.test:core:$testCoreVersion")
    debugImplementation("androidx.test:core:$testCoreVersion")

    val testRunnerVersion = "1.6.2"
    androidTestImplementation("androidx.test:runner:$testRunnerVersion")
    debugImplementation("androidx.test:runner:$testRunnerVersion")

    // Monitor obsahuje chybějící FileTestStorage
    val testMonitorVersion = "1.7.2"
    androidTestImplementation("androidx.test:monitor:$testMonitorVersion")
    debugImplementation("androidx.test:monitor:$testMonitorVersion")

    // --- HILT TESTING ---
    // Používáme přímý odkaz, aby to fungovalo spolehlivě
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")
    // Potřebné pro Hilt v debug manifestu (pokud byste používal HiltTestActivity)
    debugImplementation("com.google.dagger:hilt-android-testing:2.51.1")

    androidTestImplementation(libs.mockk.android)

    // Debug nástroje
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}