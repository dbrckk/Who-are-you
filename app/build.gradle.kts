plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val googleTestAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val googleTestAdMobInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val productionAdMobAppIdPattern = Regex("^ca-app-pub-\\d{16}~\\d{10}$")
val productionAdMobInterstitialIdPattern = Regex("^ca-app-pub-\\d{16}/\\d{10}$")

val requestedTasks = gradle.startParameter.taskNames
val playReleaseRequested = requestedTasks.any { task -> task.contains("playRelease", ignoreCase = true) }
val configuredAdMobAppId = providers.gradleProperty("WHO_ARE_YOU_ADMOB_APP_ID").orNull
val configuredAdMobInterstitialId = providers.gradleProperty("WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID").orNull
val uploadKeystorePath = providers.gradleProperty("WHO_ARE_YOU_UPLOAD_KEYSTORE_PATH").orNull
val uploadKeystorePassword = providers.gradleProperty("WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD").orNull
val uploadKeyAlias = providers.gradleProperty("WHO_ARE_YOU_UPLOAD_KEY_ALIAS").orNull
val uploadKeyPassword = providers.gradleProperty("WHO_ARE_YOU_UPLOAD_KEY_PASSWORD").orNull

if (playReleaseRequested) {
    require(
        configuredAdMobAppId != null &&
            productionAdMobAppIdPattern.matches(configuredAdMobAppId) &&
            configuredAdMobAppId != googleTestAdMobAppId
    ) {
        "playRelease requires a production WHO_ARE_YOU_ADMOB_APP_ID and rejects Google's test app ID"
    }
    require(
        configuredAdMobInterstitialId != null &&
            productionAdMobInterstitialIdPattern.matches(configuredAdMobInterstitialId) &&
            configuredAdMobInterstitialId != googleTestAdMobInterstitialId
    ) {
        "playRelease requires a production WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID and rejects Google's test interstitial ID"
    }
    require(!uploadKeystorePath.isNullOrBlank()) {
        "playRelease requires WHO_ARE_YOU_UPLOAD_KEYSTORE_PATH"
    }
    require(file(uploadKeystorePath).isFile) {
        "WHO_ARE_YOU_UPLOAD_KEYSTORE_PATH must point to an existing keystore file"
    }
    require(!uploadKeystorePassword.isNullOrBlank()) {
        "playRelease requires WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD"
    }
    require(!uploadKeyAlias.isNullOrBlank()) {
        "playRelease requires WHO_ARE_YOU_UPLOAD_KEY_ALIAS"
    }
    require(!uploadKeyPassword.isNullOrBlank()) {
        "playRelease requires WHO_ARE_YOU_UPLOAD_KEY_PASSWORD"
    }
}

android {
    namespace = "com.whoareyou.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.whoareyou.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        fun escapedBuildConfig(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

        val telemetryEndpoint = providers.gradleProperty("WHO_ARE_YOU_TELEMETRY_ENDPOINT").orNull.orEmpty()
        require(telemetryEndpoint.isBlank() || telemetryEndpoint.startsWith("https://")) {
            "WHO_ARE_YOU_TELEMETRY_ENDPOINT must use HTTPS when configured"
        }
        val admobAppId = configuredAdMobAppId ?: googleTestAdMobAppId
        val admobInterstitialId = configuredAdMobInterstitialId ?: googleTestAdMobInterstitialId

        buildConfigField("String", "TELEMETRY_ENDPOINT", escapedBuildConfig(telemetryEndpoint))
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", escapedBuildConfig(admobInterstitialId))
        buildConfigField("boolean", "EXTERNAL_SERVICES_ENABLED", "true")
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

    signingConfigs {
        if (playReleaseRequested) {
            create("playUpload") {
                storeFile = file(uploadKeystorePath!!)
                storePassword = uploadKeystorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("candidate") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("boolean", "EXTERNAL_SERVICES_ENABLED", "false")
        }
        create("playRelease") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            buildConfigField("boolean", "EXTERNAL_SERVICES_ENABLED", "true")
            if (playReleaseRequested) {
                signingConfig = signingConfigs.getByName("playUpload")
            }
        }
    }
}

tasks.register("playReleaseGuardProbe") {
    group = "verification"
    description = "Validates that strict Play release configuration can be evaluated."
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
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
