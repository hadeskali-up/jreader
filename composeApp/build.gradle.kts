plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}
kotlin {
    androidTarget { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    sourceSets {
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.10.0")
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
            implementation("org.jsoup:jsoup:1.18.3")
        }
        commonMain.dependencies {
            implementation(compose.runtime); implementation(compose.foundation); implementation(compose.material3); implementation(compose.ui)
            implementation("io.ktor:ktor-client-core:3.0.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
        }
    }
}
android {
    namespace = "com.aliworld.jreader"
    compileSdk = 35
    defaultConfig { applicationId = "com.aliworld.jreader"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "2.0.0" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
