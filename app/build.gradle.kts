import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    // Apply Firebase plugins only when google-services.json is present.
    // Uncomment after dropping the file into app/.
    // alias(libs.plugins.google.services)
    // alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun localProp(name: String, default: String = ""): String =
    localProperties.getProperty(name) ?: default

// Google's official AdMob TEST IDs. Used in debug builds and as fallbacks
// whenever the user has not supplied real IDs in local.properties.
// https://developers.google.com/admob/android/test-ads
object AdMobTestIds {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"
}

android {
    namespace = "com.snapdoc.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.snapdoc.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.snapdoc.app.SnapdocTestRunner"
        vectorDrawables { useSupportLibrary = true }

        // AdMob App ID is required as a manifest placeholder.
        manifestPlaceholders["admobAppId"] =
            localProp("ADMOB_APP_ID").ifBlank { AdMobTestIds.APP_ID }
    }

    val releaseSigning = signingConfigs.create("release") {
        val keystorePath = localProp("RELEASE_KEYSTORE_PATH")
        if (keystorePath.isNotBlank() && rootProject.file(keystorePath).exists()) {
            storeFile = rootProject.file(keystorePath)
            storePassword = localProp("RELEASE_KEYSTORE_PASSWORD")
            keyAlias = localProp("RELEASE_KEY_ALIAS")
            keyPassword = localProp("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false

            // Debug always uses Google's official test IDs.
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProp("GEMINI_API_KEY")}\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${AdMobTestIds.BANNER}\"")
            buildConfigField("String", "ADMOB_NATIVE_ID", "\"${AdMobTestIds.NATIVE}\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${AdMobTestIds.INTERSTITIAL}\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"${AdMobTestIds.REWARDED}\"")
            buildConfigField("boolean", "USING_TEST_AD_IDS", "true")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Only sign release if a keystore has been configured.
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }

            val bannerId = localProp("ADMOB_BANNER_ID").ifBlank { AdMobTestIds.BANNER }
            val nativeId = localProp("ADMOB_NATIVE_ID").ifBlank { AdMobTestIds.NATIVE }
            val interstitialId = localProp("ADMOB_INTERSTITIAL_ID").ifBlank { AdMobTestIds.INTERSTITIAL }
            val rewardedId = localProp("ADMOB_REWARDED_ID").ifBlank { AdMobTestIds.REWARDED }
            val usingTestAds = listOf(bannerId, nativeId, interstitialId, rewardedId)
                .any { it.startsWith("ca-app-pub-3940256099942544") }

            buildConfigField("String", "GEMINI_API_KEY", "\"${localProp("GEMINI_API_KEY")}\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$bannerId\"")
            buildConfigField("String", "ADMOB_NATIVE_ID", "\"$nativeId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitialId\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"$rewardedId\"")
            buildConfigField("boolean", "USING_TEST_AD_IDS", "$usingTestAds")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/LICENSE*",
            "/META-INF/NOTICE*",
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.androidx.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // Networking + serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Logging
    implementation(libs.timber)

    // ML Kit
    implementation(libs.mlkit.document.scanner)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.text.recognition.devanagari)

    // Ads + Billing
    implementation(libs.play.services.ads)
    implementation(libs.play.billing.ktx)

    // Firebase (still listed even when google-services.json is absent;
    // gates around init protect runtime).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Debug
    debugImplementation(libs.leakcanary.android)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.hilt.android)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
