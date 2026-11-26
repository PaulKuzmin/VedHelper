plugins {
    // AGP 9 включает поддержку Kotlin сам — плагин kotlin.android больше не нужен
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.alternadv.vedhelper"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.alternadv.vedhelper"
        minSdk = 24
        targetSdk = 37
        versionCode = 5
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Адрес API зависит от сборки: debug смотрит в локальный dev, release — в прод.
    // Переопределение для устройства: ./gradlew -PdevApiHost=http://192.168.1.10:8180 …
    val devApiHost = (project.findProperty("devApiHost") as String?) ?: "http://10.0.2.2:8180"

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$devApiHost\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://alternadv.com\"")
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            val keystorePath: String? = System.getenv("MYAPP_KEYSTORE") ?: project.findProperty("MYAPP_KEYSTORE") as String?
            storeFile = keystorePath?.let { file(it) }
            storePassword = System.getenv("MYAPP_KEYSTORE_PASSWORD") ?: project.findProperty("MYAPP_KEYSTORE_PASSWORD") as String?
            keyAlias = System.getenv("MYAPP_KEY_ALIAS") ?: project.findProperty("MYAPP_KEY_ALIAS") as String?
            keyPassword = System.getenv("MYAPP_KEYSTORE_PASSWORD") ?: project.findProperty("MYAPP_KEYSTORE_PASSWORD") as String?
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    // явная версия: иначе moshi-kotlin подтягивает kotlin-reflect 1.8.22 при stdlib 2.4.x
    implementation(libs.kotlin.reflect)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    implementation("com.google.android.material:material:1.12.0")

    implementation("com.itextpdf:itext7-core:9.2.0")

    implementation(platform("ru.rustore.sdk:bom:2026.10.01"))
    implementation("ru.rustore.sdk:appupdate")
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}