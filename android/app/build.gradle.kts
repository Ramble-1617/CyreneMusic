import com.android.build.api.dsl.ApplicationExtension
import java.io.FileInputStream
import java.util.Properties

val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

val releaseStoreFile = keystoreProperties["storeFile"]
    ?.toString()
    ?.takeIf { it.isNotBlank() }
    ?.let { rootProject.file(it) }

val hasReleaseSigning = releaseStoreFile?.isFile == true &&
    releaseStoreFile.length() > 0 &&
    keystoreProperties["storePassword"]?.toString()?.isNotBlank() == true &&
    keystoreProperties["keyAlias"]?.toString()?.isNotBlank() == true &&
    keystoreProperties["keyPassword"]?.toString()?.isNotBlank() == true

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

// Use the public AGP interface while retaining legacy mode for Flutter plugins.
extensions.configure<ApplicationExtension> {
    namespace = "com.cyrene.music"
    compileSdk = 37
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    // Kotlin JVM 17 is configured for all modules in android/build.gradle.kts.

    defaultConfig {
        applicationId = "com.cyrene.music"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
        manifestPlaceholders["appName"] = "Cyrene Music"
    }

    signingConfigs {
        create("release") {
            if (releaseStoreFile != null) {
                storeFile = releaseStoreFile
            }
            keystoreProperties["storePassword"]?.takeIf { it.toString().isNotBlank() }?.let {
                storePassword = it.toString()
            }
            keystoreProperties["keyAlias"]?.takeIf { it.toString().isNotBlank() }?.let {
                keyAlias = it.toString()
            }
            keystoreProperties["keyPassword"]?.takeIf { it.toString().isNotBlank() }?.let {
                keyPassword = it.toString()
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "Cyrene Music (Debug)"
        }
        getByName("release") {
            // 使用正式密钥；公开 Fork 的 CI 没有密钥时回退到 Android debug keystore。
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            manifestPlaceholders["appName"] = "Cyrene Music"
        }
    }
}

flutter {
    source = "../.."
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
}
