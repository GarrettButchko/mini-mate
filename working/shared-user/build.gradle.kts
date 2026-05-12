import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("co.touchlab.skie") version "0.10.10"
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildkonfig)
}

val localProperties = Properties()
val localPropFile = project.rootProject.file("local.properties")
if (localPropFile.exists()) {
    localProperties.load(FileInputStream(localPropFile))
}
val mapboxTokenStr = localProperties.getProperty("MAPBOX_ACCESS_TOKEN", "YOUR_MAPBOX_ACCESS_TOKEN")
val foursquareKeyStr = localProperties.getProperty("FOURSQUARE_API_KEY", "")

buildkonfig {
    packageName = "com.garrettbutchko.minimate"
    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "MAPBOX_ACCESS_TOKEN",
            mapboxTokenStr
        )
            buildConfigField(
                com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                "FOURSQUARE_API_KEY",
                foursquareKeyStr
            )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )


    iosTargets.forEach {
        it.binaries.framework {
            baseName = "shared_user"
            isStatic = true
            val bundleId = "com.garrettbutchko.minimate.user"
            freeCompilerArgs += "-Xbinary=bundleId=$bundleId"
            export(projects.shared)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared)

            implementation(libs.androidx.room.runtime)
            implementation("co.touchlab:kermit:2.0.2")
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor Client
            implementation("io.ktor:ktor-client-core:3.4.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")

            // Room SQLite
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Firebase Multiplatform
            implementation(libs.firebase.common)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.database)
            
            implementation(libs.qrcode.kotlin)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)


            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.4.1")
            implementation(libs.places)
            implementation(libs.maps.compose)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.4.1")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }
    }
}

android {
    namespace = "com.garrettbutchko.minimate.shared_user"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    "kspCommonMainMetadata"(libs.koin.compiler)
}
