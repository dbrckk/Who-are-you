plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.whoareyou.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.whoareyou.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        fun escapedBuildConfig(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

        val telemetryEndpoint = providers.gradleProperty("WHO_ARE_YOU_TELEMETRY_ENDPOINT").orNull.orEmpty()
        require(telemetryEndpoint.isBlank() || telemetryEndpoint.startsWith("https://")) {
            "WHO_ARE_YOU_TELEMETRY_ENDPOINT must use HTTPS when configured"
        }
        val admobAppId = providers.gradleProperty("WHO_ARE_YOU_ADMOB_APP_ID").orNull
            ?: "ca-app-pub-3940256099942544~3347511713"
        val admobInterstitialId = providers.gradleProperty("WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID").orNull
            ?: "ca-app-pub-3940256099942544/1033173712"

        buildConfigField("String", "TELEMETRY_ENDPOINT", escapedBuildConfig(telemetryEndpoint))
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", escapedBuildConfig(admobInterstitialId))
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
